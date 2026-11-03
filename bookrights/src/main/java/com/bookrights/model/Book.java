package com.bookrights.model;

import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
@Table(name = "Books")
public class Book {
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="Author", nullable=false)
	private String author;
	
	@Column(name="Book_Name", nullable=false)
	private String bookName;
	
	@Column(name="Isbn", unique=true, nullable=false)
	private String isbn;
	
	@Column(name="Price")
	private double price;
	
	@Column(name="Category", nullable=false)
	private String category;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "User_Id")
	private User user;
	
	public Book() {}
	
	public Book( String author, String bookName, String isbn, double price, String category, User user) {
		this.author = author;
		this.bookName = bookName;
		this.isbn = isbn;
		this.price = price;
		this.category = category;
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
