package com.mstftrgt.libraryrestapi.controller;

import com.mstftrgt.libraryrestapi.model.Book;
import com.mstftrgt.libraryrestapi.repository.LibraryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
public class CheckoutResource {

    private final LibraryRepository libRepo;

    public CheckoutResource(LibraryRepository libRepo) {
        this.libRepo = libRepo;
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
