package com.bookrights.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateBookRequest {
	
	@NotBlank(message = "Book name cannot be empty")
	private String bookName;

	@NotBlank(message = "Author cannot be empty")
	private String author;

	@NotBlank(message = "ISBN cannot be empty")
	private String isbn;

	@NotBlank(message = "Category cannot be empty")
	private String category;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
	private double price;

    public CreateBookRequest() {

    }
	
	public CreateBookRequest(String bookName, String author, String isbn, String category, double price) {
		super();
		this.bookName = bookName;
		this.author = author;
		this.isbn = isbn;
		this.category = category;
		this.price = price;
	}

	public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}