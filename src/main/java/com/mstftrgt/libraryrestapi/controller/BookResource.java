package com.mstftrgt.libraryrestapi.controller;

import com.mstftrgt.libraryrestapi.model.Book;
import com.mstftrgt.libraryrestapi.repository.LibraryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/books")
public class BookResource {

    private final LibraryRepository libRepo;

    public BookResource(LibraryRepository libRepo) {
        this.libRepo = libRepo;
    }

    @GetMapping
    public ResponseEntity<List<Book>> findAllBooks(Pageable pageable) {

        Page<Book> page = libRepo.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "name"))
                )
        );

        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> findBookById(@PathVariable Integer id) {

        Optional<Book> book = libRepo.findById(id);

        if (book.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(book.get());
    }

    @PostMapping
    public ResponseEntity<Void> addBook(@RequestBody Book book, UriComponentsBuilder ucb) {
        Book savedBook = libRepo.save(book);

        URI locationOfNewBook = ucb
                .path("books/{id}")
                .buildAndExpand(savedBook.getId())
                .toUri();

        return ResponseEntity.created(locationOfNewBook).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateBook(@PathVariable Integer id, @RequestBody Book bookUpdateRequest) {
        Optional<Book> bookOriginal = libRepo.findById(id);

        if (bookOriginal.isEmpty())
            return ResponseEntity.notFound().build();

        Book bookUpdated = new Book(id, bookUpdateRequest.getName(), bookUpdateRequest.getAuthor(), bookOriginal.get().getCurrentOwner());
        libRepo.save(bookUpdated);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer id) {
        if (libRepo.existsById(id)) {
            libRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}
