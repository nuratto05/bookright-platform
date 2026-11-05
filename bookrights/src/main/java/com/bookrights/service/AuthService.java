package com.bookrights.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import com.bookrights.Dto.RegisterRequest;
import com.bookrights.Dto.LoginRequest;
import com.bookrights.model.User;
import com.bookrights.repository.UserRepository;
import com.bookrights.repository.WalletRepository;
import com.bookrights.security.JwtUtil;
import com.bookrights.model.CustomUserDetails;
import com.bookrights.model.Wallet;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final WalletRepository walletRepository;
	private final JwtUtil jwtService;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;

	public AuthService(UserRepository userRepository, WalletRepository walletRepository, JwtUtil jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.walletRepository = walletRepository;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
		this.passwordEncoder = passwordEncoder;
		
	}

	@Transactional
	public void register(RegisterRequest request) {

		if (request == null) {
			throw new IllegalArgumentException("Request body is missing");
		}

		if (request.getUsername() == null || request.getUsername().isBlank()) {
			throw new IllegalArgumentException("Username is required");
		}

		if (request.getEmail() == null || request.getEmail().isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}

		if (request.getPassword() == null || request.getPassword().isBlank()) {
			throw new IllegalArgumentException("Password is required");
		}

		if (request.getName() == null || request.getName().isBlank()) {
			throw new IllegalArgumentException("Name is required");
		}

		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "User with email already exists");
		}

		if (userRepository.findByUsername(request.getUsername()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
		}

		String hashedPassword = passwordEncoder.encode(request.getPassword());

		User user = new User(request.getUsername(), request.getName(), request.getEmail(), hashedPassword);
		Wallet wallet = new Wallet(user, 1000.0);
		userRepository.save(user);
		walletRepository.save(wallet);
	}

    public String login(LoginRequest request) {

		if (request == null) {
			throw new IllegalArgumentException("Request body is missing");
		}

		if (request.getUsername() == null || request.getUsername().isBlank()) {
			throw new IllegalArgumentException("Username is required");
		}

		if (request.getPassword() == null || request.getPassword().isBlank()) {
			throw new IllegalArgumentException("Password is required");
		}

		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
		return jwtService.generateToken(customUserDetails.getUser());
	}

}