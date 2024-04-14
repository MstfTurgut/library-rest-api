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
class BookControllerTests {

    @Autowired
    TestRestTemplate restTemplate;

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

}