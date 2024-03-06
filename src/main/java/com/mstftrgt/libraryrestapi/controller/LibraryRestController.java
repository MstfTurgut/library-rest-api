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
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
public class LibraryRestController {
    private final LibraryRepository libRepo;

    public LibraryRestController(LibraryRepository libRepo) {
        this.libRepo = libRepo;
    }

    @GetMapping("/books")
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

    @GetMapping("/books/{id}")
    public ResponseEntity<Book> findBookById(@PathVariable Integer id) {

        Optional<Book> book = libRepo.findById(id);

        if (book.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(book.get());
    }

    @PostMapping("/books")
    public ResponseEntity<Void> addBook(@RequestBody Book book, UriComponentsBuilder ucb) {
        Book savedBook = libRepo.save(book);

        URI locationOfNewBook = ucb
                .path("books/{id}")
                .buildAndExpand(savedBook.getId())
                .toUri();

        return ResponseEntity.created(locationOfNewBook).build();
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<Void> updateBook(@PathVariable Integer id, @RequestBody Book bookUpdateRequest) {
        Optional<Book> bookOriginal = libRepo.findById(id);

        if (bookOriginal.isEmpty())
            return ResponseEntity.notFound().build();

        Book bookUpdated = new Book(id, bookUpdateRequest.getName(), bookUpdateRequest.getAuthor(), bookOriginal.get().getCurrentOwner());
        libRepo.save(bookUpdated);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer id) {
        if (libRepo.existsById(id)) {
            libRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/books/{id}/checkout")
    public ResponseEntity<Void> checkoutBook(@PathVariable Integer id, Principal principal) {
        Optional<Book> bookOptional = libRepo.findById(id);

        if (bookOptional.isEmpty())
            return ResponseEntity.notFound().build();

        Book book = bookOptional.get();

        if (book.getCurrentOwner() != null)
            return ResponseEntity.badRequest().build();

        book.setCurrentOwner(principal.getName());
        libRepo.save(book);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/students/borrowedBooks/{username}")
    public ResponseEntity<List<Book>> findAllBorrowedBooksByAnyStudent(@PathVariable String username, Pageable pageable) {

        Page<Book> page = libRepo.findByCurrentOwner(username, PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "name"))
        ));

        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/me/borrowedBooks")
    public ResponseEntity<List<Book>> findMyBorrowedBooks(Principal principal, Pageable pageable) {

        Page<Book> page = libRepo.findByCurrentOwner(principal.getName(), PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "name"))
        ));

        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/me/borrowedBooks/{id}/return")
    public ResponseEntity<Void> returnMyBook(@PathVariable Integer id, Principal principal) {

        Book ownedBook = libRepo.findByCurrentOwnerAndId(principal.getName(), id);

        if (ownedBook == null) return ResponseEntity.notFound().build();

        ownedBook.setCurrentOwner(null);
        libRepo.save(ownedBook);

        return ResponseEntity.noContent().build();
    }
}
