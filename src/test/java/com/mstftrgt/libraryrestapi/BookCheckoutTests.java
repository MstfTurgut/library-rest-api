package com.mstftrgt.libraryrestapi;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookCheckoutTests {

    @Autowired
    TestRestTemplate restTemplate;


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
    void shouldNotAllowStudentsToCheckoutABookThatDoesNotExist() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("mary", "test123")
                .exchange("/books/9999/checkout", HttpMethod.PUT, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldAllowManagersToReviewAllBooksThatOwnedByAnyStudent() {
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

