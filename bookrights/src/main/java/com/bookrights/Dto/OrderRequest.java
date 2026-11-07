package com.bookrights.Dto;

import jakarta.validation.constraints.NotNull;

public class OrderRequest {

    @NotNull (message = "Book ID cannot be empty")
    private Long bookId;

    public OrderRequest() {

    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
}
