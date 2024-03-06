package com.mstftrgt.libraryrestapi.repository;

import com.mstftrgt.libraryrestapi.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryRepository extends JpaRepository<Book, Integer> {

    Page<Book> findByCurrentOwner(String currentOwner, PageRequest pageRequest);

    Book findByCurrentOwnerAndId(String currentOwner, Integer id);
}
