package com.bookrights.service;

import com.bookrights.Dto.UpdateBookRequest;
import com.bookrights.model.Book;
import com.bookrights.model.CustomUserDetails;
import com.bookrights.model.User;
import com.bookrights.repository.BookRepository;
import com.bookrights.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookServiceTest {

    private BookRepository bookRepository;
    private UserRepository userRepository;
    private BookService service;
    private User owner;
    private User intruder;
    private Book book;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        userRepository = mock(UserRepository.class);
        service = new BookService(bookRepository, userRepository);
        owner = new User("owner", "Owner", "owner@test.local", "password");
        owner.setId(1L);
        intruder = new User("intruder", "Intruder", "intruder@test.local", "password");
        intruder.setId(2L);
        book = new Book("Author", "Title", "ISBN-1", 25.0, "Fiction", owner);
        book.setId(10L);
    }

    @Test
    void updateRejectsUserWhoDoesNotOwnBook() {
        when(bookRepository.findByIdWithLock(10L)).thenReturn(Optional.of(book));
        when(userRepository.findById(2L)).thenReturn(Optional.of(intruder));

        UpdateBookRequest request = new UpdateBookRequest(
                Optional.of("Stolen title"), Optional.empty(), Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> service.updateBook(10L, request, new CustomUserDetails(intruder)));

        assertEquals("You are not allowed to access this book", error.getMessage());
        assertEquals("Title", book.getBookName());
        verify(bookRepository, never()).save(book);
    }

    @Test
    void deleteRejectsUserWhoDoesNotOwnBook() {
        when(bookRepository.findByIdWithLock(10L)).thenReturn(Optional.of(book));
        when(userRepository.findById(2L)).thenReturn(Optional.of(intruder));

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> service.deleteBook(10L, new CustomUserDetails(intruder)));

        assertEquals("You are not allowed to access this book", error.getMessage());
        verify(bookRepository, never()).delete(book);
    }

    @Test
    void negativeUpdatePriceThrowsBeforeSaveButIsEntityValidationOnly() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> book.setPrice(-1.0));

        assertEquals("Price cannot be negative", error.getMessage());
        assertEquals(25.0, book.getPrice());
    }
}
