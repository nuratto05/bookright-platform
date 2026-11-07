package com.bookrights.model;

import jakarta.persistence.Table;

import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
@Table(name = "Users")
public class User {
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="username", nullable=false,unique = true)
	private String username;
	
	@Column(name="password", nullable=false)
	private String password;

	@Column(name="name", nullable=false)
	private String name;

	@Column(name="email", nullable=false, unique = true)
	private String email;

    @OneToMany(mappedBy = "user")
    private List<Book> books = new ArrayList<>();
	
	public User() {}

	public User(String username, String name, String email, String password) {
		this.username = username;
		this.name = name;
		this.email = email;
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
