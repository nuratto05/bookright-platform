package com.bookrights.Dto;

import java.util.Optional;

public class UpdateBookRequest {
	
    private Optional<String> bookName = Optional.empty();
    private Optional<Double> price = Optional.empty();
    private Optional<String> category = Optional.empty();

	public UpdateBookRequest() {

	}
    
	public UpdateBookRequest(Optional<String> bookName, Optional<Double> price, Optional<String> category) {
		super();
		this.bookName = bookName;
		this.price = price;
		this.category = category;
	}

	public Optional<String> getBookName() {
		return bookName;
	}

	public Optional<Double> getPrice() {
		return price;
	}

	public Optional<String> getCategory() {
		return category;
	}
	
	
}