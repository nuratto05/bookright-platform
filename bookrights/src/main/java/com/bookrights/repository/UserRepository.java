package com.bookrights.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookrights.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByUsername(String username);
    public Optional<User> findByEmail(String email);
}
