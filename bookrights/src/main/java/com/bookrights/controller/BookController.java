package com.bookrights.controller;

import com.bookrights.repository.BookRepository;

import com.bookrights.model.Book;
import com.bookrights.model.CustomUserDetails;
import com.bookrights.service.BookService;

import com.bookrights.Dto.CreateBookRequest;
import com.bookrights.Dto.UpdateBookRequest;
import com.bookrights.Dto.BookResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;

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
    public ResponseEntity<?> createBook(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody CreateBookRequest request) {

        Optional<Book> bookOptional = bookRepository.findByIsbn(request.getIsbn());

        if (bookOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Book with ISBN exists");
		}

        try {
            BookResponse newBook = bookService.createBook(request, user);

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
	public ResponseEntity<?> updateBook(@AuthenticationPrincipal CustomUserDetails user, @RequestBody UpdateBookRequest request, @PathVariable("isbn") String isbn) {
		
		if (isbn == null || isbn.isBlank()) {
	        return ResponseEntity.badRequest().body("ISBN cannot be null or empty");
	    }
		
		try {
            BookResponse updatedBook = bookService.updateBook(isbn, request, user);
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
	public ResponseEntity<?> deleteBook(@AuthenticationPrincipal CustomUserDetails user, @PathVariable("isbn") String isbn) {		
		
		if (isbn == null || isbn.isBlank()) {
	        return ResponseEntity.badRequest().body("ISBN cannot be null or empty");
	    }
		
		try {
            bookService.deleteBook(isbn, user);
            return ResponseEntity.ok("Book removed successfully");

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

    @GetMapping("/api/books")
	public ResponseEntity<Page<BookResponse>> getBooks(@AuthenticationPrincipal CustomUserDetails user, Pageable pageable) {

	    Page<BookResponse> books = bookService.getBooks(pageable);
	    
	    return ResponseEntity.ok(books);
	}

    @GetMapping("/api/book/{isbn}")
	public ResponseEntity<BookResponse> getBook(@AuthenticationPrincipal CustomUserDetails user, @PathVariable("isbn") String isbn) {

	    BookResponse book = bookService.getBook(isbn);
	    
	    return ResponseEntity.ok(book);
	}

}
