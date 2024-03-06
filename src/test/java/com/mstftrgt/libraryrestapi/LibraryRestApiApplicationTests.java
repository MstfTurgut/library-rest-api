package com.mstftrgt.libraryrestapi;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.mstftrgt.libraryrestapi.model.Book;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryRestApiApplicationTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void contextLoads() {}


    @Test
    void shouldReturnAllBooksWhenListIsRequested() {

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("mary", "test123")
                .getForEntity("/books", String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnASortedPageOfBooks() {

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("mary", "test123")
                .getForEntity("/books?page=0&size=1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(1);

        String name = documentContext.read("$[0].name");
        assertThat(name).isEqualTo("1984");
    }

    @Test
    void shouldReturnABookByIdIfExists() {

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("mary", "test123")
                .getForEntity("/books/1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        String name = documentContext.read("$.name");

        assertThat(name).isEqualTo("Book1");
    }

    @Test
    void shouldNotReturnABookByIdIfNotExists() {

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("mary", "test123")
                .getForEntity("/books/999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void shouldAllowManagersToAddBook() {

        Book newBook = new Book("NewBook", "NewAuthor");

        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("susan", "test123")
                .postForEntity("/books", newBook, Void.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewBook = createResponse.getHeaders().getLocation();

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("susan", "test123")
                .getForEntity(locationOfNewBook, String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        String name = documentContext.read("$.name");

        assertThat(id).isNotNull();
        assertThat(name).isEqualTo(newBook.getName());
    }

    @Test
    void shouldNotAllowStudentsToAddBook() {

        Book newBook = new Book("BookName", "BookAuthor");

        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("mary", "test123")
                .postForEntity("/books", newBook, Void.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    void shouldAllowManagersToUpdateBook() {
        Book bookUpdate = new Book("UpdatedName", "UpdatedAuthor");
        HttpEntity<Book> request = new HttpEntity<>(bookUpdate);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("susan", "test123")
                .exchange("/books/2", HttpMethod.PUT, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("susan", "test123")
                .getForEntity("/books/2", String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Number id = documentContext.read("$.id");
        String name = documentContext.read("$.name");
        String author = documentContext.read("$.author");

        assertThat(id).isEqualTo(2);
        assertThat(name).isEqualTo("UpdatedName");
        assertThat(author).isEqualTo("UpdatedAuthor");

    }

    @Test
    void shouldNotUpdateABookThatDoesNotExists() {
        Book unknownBook = new Book("UpdatedName", "UpdatedAuthor");
        HttpEntity<Book> request = new HttpEntity<>(unknownBook);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("susan", "test123")
                .exchange("/books/999", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotAllowStudentsToUpdateBook() {
        Book bookUpdate = new Book("UpdatedName", "UpdatedAuthor");
        HttpEntity<Book> request = new HttpEntity<>(bookUpdate);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("mary", "test123")
                .exchange("/books/3", HttpMethod.PUT, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldAllowManagersToRemoveBook() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("susan", "test123")
                .exchange("/books/9", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("susan", "test123")
                .getForEntity("/books/9", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotAllowStudentsToRemoveBook() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("mary", "test123")
                .exchange("/books/8", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldNotDeleteABookThatDoesNotExist() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("susan", "test123")
                .exchange("/books/9999", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldAllowStudentsToCheckoutABook() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("mary", "test123")
                .exchange("/books/5/checkout", HttpMethod.PUT, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("mary", "test123")
                .getForEntity("/books/5", String.class);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        String currentOwner = documentContext.read("$.currentOwner");

        assertThat(currentOwner).isEqualTo("mary");
    }

    @Test
    void shouldNotAllowAStudentToCheckoutABookThatIsCurrentlyOwnedByAnotherStudent() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("john", "test123")
                .exchange("/books/4/checkout", HttpMethod.PUT, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotCheckoutABookThatDoesNotExist() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("mary", "test123")
                .exchange("/books/9999/checkout", HttpMethod.PUT, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldAllowManagersToReturnAllBooksThatOwnedByAnyStudent() {
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("susan", "test123")
                .getForEntity("/students/borrowedBooks/mary", String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        JSONArray read = documentContext.read("$..name");

        assertThat(read).containsExactlyInAnyOrder("Book4", "Book6");
    }

    @Test
    void shouldNotAllowStudentsToReturnAllBooksThatOwnedByAnyStudent() {
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("john", "test123")
                .getForEntity("/students/borrowedBooks/mary", String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    @Test
    void shouldAllowStudentsToReviewTheirBorrowedBooks() {

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("mary", "test123")
                .getForEntity("/me/borrowedBooks", String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        JSONArray read = documentContext.read("$..name");

        assertThat(read).containsExactlyInAnyOrder("Book4", "Book5", "Book6");
    }

    @Test
    void shouldAllowStudentsToReturnTheirBorrowedBooks() {

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("mary", "test123")
                .exchange("/me/borrowedBooks/6/return", HttpMethod.PUT, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("mary", "test123")
                .getForEntity("/books/6", String.class);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        String currentOwner = documentContext.read("$.currentOwner");

        assertThat(currentOwner).isEqualTo(null);
    }
}