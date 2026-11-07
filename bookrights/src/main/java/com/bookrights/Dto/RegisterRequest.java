package com.bookrights.Dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {
	
	@NotBlank (message = "Name cannot be empty")
	private String name;

	@NotBlank (message = "Username cannot be empty")
	private String username;

	@NotBlank (message = "Password cannot be empty")
	private String password;

	@NotBlank (message = "Email cannot be empty")
	private String email;
	
	public RegisterRequest() {
		
	}

	public RegisterRequest(String name, String username, String password, String email) {
		this.name = name;
		this.username = username;
		this.password = password;
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}
	
	

}