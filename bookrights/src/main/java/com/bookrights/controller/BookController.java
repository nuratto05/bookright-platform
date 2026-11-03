package com.bookrights.controller;

import com.bookrights.repository.BookRepository;

import com.bookrights.model.Book;

import com.bookrights.service.BookService;

import com.bookrights.Dto.CreateBookRequest;
import com.bookrights.Dto.UpdateBookRequest;
import com.bookrights.Dto.BookResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class BookController {
    
    private final BookRepository bookRepository;
    private final BookService bookService;

    public BookController(BookRepository bookRepository, BookService bookService) {
        this.bookRepository = bookRepository;
		this.bookService = bookService;
    }

    @PostMapping("/api/createBook")
    public ResponseEntity<?> createBook(@RequestBody CreateBookRequest request) {

        Optional<Book> bookOptional = bookRepository.findByIsbn(request.getIsbn());

        if (bookOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Book with ISBN exists");
		}

        try {
            BookResponse newBook = bookService.createBook(request, 0000);

            return ResponseEntity.status(HttpStatus.CREATED).body(newBook);

        } catch (RuntimeException e) {

            if (e.getMessage().equals("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }

            if (e.getMessage().equals("You are not allowed to access this book")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @PutMapping("/api/updateBook/{isbn}")
	public ResponseEntity<?> updateBook(@RequestBody UpdateBookRequest request, @PathVariable("isbn") String isbn) {
		
		if (isbn == null || isbn.isBlank()) {
	        return ResponseEntity.badRequest().body("ISBN cannot be null or empty");
	    }
		
		try {
            BookResponse updatedBook = bookService.updateBook(isbn, request, 0000);

            return ResponseEntity.ok(updatedBook);

        } catch (RuntimeException e) {

            if (e.getMessage().equals("User not found") || "Book not found".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }

            if (e.getMessage().equals("You are not allowed to access this book")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
	}

    @DeleteMapping("/api/deleteBook/{isbn}")
	public ResponseEntity<?> deleteBook(@PathVariable("isbn") String isbn) {		
		
		if (isbn == null || isbn.isBlank()) {
	        return ResponseEntity.badRequest().body("ISBN cannot be null or empty");
	    }
		
		try {
            return bookService.deleteBook(0000, isbn);

        } catch (RuntimeException e) {

            if (e.getMessage().equals("User not found") || "Book not found".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }

            if (e.getMessage().equals("You are not allowed to access this book")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
	}

}
