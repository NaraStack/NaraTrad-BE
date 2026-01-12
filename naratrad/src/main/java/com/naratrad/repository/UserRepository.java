package com.naratrad.repository;

import com.naratrad.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
    long countByIsActiveTrue();
    List<User> findTop5ByOrderByCreatedAtDesc();
    long countByCreatedAtAfter(LocalDateTime date);
    long countByLastLoginAfter(LocalDateTime date);
}