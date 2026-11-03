package com.bookrights.controller;

import com.bookrights.Dto.LoginRequest;
import com.bookrights.Dto.RegisterRequest;
import com.bookrights.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

@RestController 
public class UserController {
    
    private final AuthService authService;
	private final AuthenticationManager authenticationManager;

	public UserController(AuthService authService, AuthenticationManager authenticationManager) {
		this.authService = authService;
		this.authenticationManager = authenticationManager;
	}

    @GetMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request, HttpServletResponse response) {

        try {
            String token = authService.login(request);
            
            Cookie cookie = new Cookie("JWT", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 10);

            response.addCookie(cookie);

            return ResponseEntity.ok().body("Login Succesful");
            
        }catch (BadCredentialsException e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");

        }catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(e.getMessage());

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

		try {

            authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");

        }catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            
        }catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
        }
    }
    
}
