package com.naratrad.repository;

import com.naratrad.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Mencari user berdasarkan email (penting untuk login)
    Optional<User> findByEmail(String email);
}