package com.bookrights.repository;

import com.bookrights.model.Book;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query ("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithLock(@Param("id") Long id);
    
}
