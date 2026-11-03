package com.bookrights.service;

import com.bookrights.repository.BookRepository;
import com.bookrights.repository.UserRepository;

import com.bookrights.model.Book;
import com.bookrights.model.User;
import com.bookrights.model.CustomUserDetails;

import com.bookrights.Dto.CreateBookRequest;
import com.bookrights.Dto.UpdateBookRequest;


import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.NoSuchElementException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.bookrights.Dto.BookResponse;


@Service
public class BookService {
    
    private BookRepository bookRepository;
	private UserRepository userRepository;

    public BookService(BookRepository bookRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
		this.userRepository = userRepository;
    }

    @Cacheable(value = "book", key = "#isbn")
    public BookResponse getBook(String isbn) {

    	Book book = bookRepository.findByIsbn(isbn).orElseThrow(() -> new NoSuchElementException ("Book not found"));
		return new BookResponse(book);
        
    }
    
    @Cacheable(value = "books")
    public Page<BookResponse> getBooks(Pageable pageable) {

        Page<Book> books = bookRepository.findAll(pageable);
        return books.map(book -> new BookResponse(book));

    }
    
    public BookResponse createBook(CreateBookRequest request, CustomUserDetails userDetails) {
    	
    	User user = userRepository.findById(userDetails.getUser().getId()).orElseThrow(() -> new NoSuchElementException ("User not found"));
    	
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

    @CacheEvict(value = "book", allEntries = true)
    public BookResponse updateBook(String isbn, UpdateBookRequest request, CustomUserDetails userDetails) {
    	
        Book book = bookRepository.findByIsbn(isbn).orElseThrow(() -> new NoSuchElementException ("Book not found"));
        
        User user = userRepository.findById(userDetails.getUser().getId()).orElseThrow(() -> new NoSuchElementException ("User not found"));
        
        verifyBookOwnership(book, user);

        request.getBookName().ifPresent(name -> book.setBookName(name));
        request.getPrice().ifPresent(price -> book.setPrice(price));
        request.getCategory().ifPresent(category -> book.setCategory(category));

        bookRepository.save(book);

        return new BookResponse(book);
    }

    @CacheEvict(value = "book", allEntries = true)
    public void deleteBook(String isbn, CustomUserDetails userDetails) {
		Book book = bookRepository.findByIsbn(isbn).orElseThrow(() -> new NoSuchElementException ("Book not found"));
		
		User user = userRepository.findById(userDetails.getUser().getId()).orElseThrow(() -> new NoSuchElementException ("User not found"));

		verifyBookOwnership(book, user);

		bookRepository.delete(book);
	}

    private void verifyBookOwnership(Book book, User user) {
        if (!book.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to access this book");
        }
    }
}
