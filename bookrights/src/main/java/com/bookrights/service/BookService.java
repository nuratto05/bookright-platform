package com.bookrights.service;

import com.bookrights.repository.BookRepository;
import com.bookrights.repository.UserRepository;

import com.bookrights.model.Book;
import com.bookrights.model.User;

import com.bookrights.Dto.CreateBookRequest;
import com.bookrights.Dto.UpdateBookRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.bookrights.Dto.BookResponse;

import org.springframework.stereotype.Service;

@Service
public class BookService {
    
    private BookRepository bookRepository;
	private UserRepository userRepository;

    public BookService(BookRepository bookRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
		this.userRepository = userRepository;
    }

    public Book getBook(String isbn) {
    	Book book = bookRepository.findByIsbn(isbn).orElseThrow(() -> new RuntimeException("Book not found"));

		return book;
    }

    public BookResponse createBook(CreateBookRequest request, int userId) {
    	
    	User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
    	
        Book createBook = new Book(
            request .getAuthor(),
            request.getBookName(),
            request.getIsbn(),
            request.getPrice(),
            request.getCategory(),
            user
        );
        
        bookRepository.save(createBook);
        return new BookResponse(createBook);
    }

    public BookResponse updateBook(String isbn, UpdateBookRequest request, int userId) {
    	
        Book book = bookRepository.findByIsbn(isbn).orElseThrow(() -> new RuntimeException("Book not found"));
        
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        verifyBookOwnership(book, user);

        request.getBookName().ifPresent(name -> book.setBookName(name));
        request.getPrice().ifPresent(price -> book.setPrice(price));
        request.getCategory().ifPresent(category -> book.setCategory(category));

        bookRepository.save(book);

        return new BookResponse(book);
    }

    public ResponseEntity<String> deleteBook(int userId, String isbn) {
		Book book = bookRepository.findByIsbn(isbn).orElseThrow(() -> new RuntimeException("Book not found"));
		
		User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found"));

		verifyBookOwnership(book, user);

		bookRepository.delete(book);
		
		return ResponseEntity.ok("Book Removed");
	}

    private void verifyBookOwnership(Book book, User user) {
        if (!book.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You are not allowed to access this book");
        }
    }
}
