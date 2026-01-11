package com.naratrad.service;

import com.naratrad.dto.ChangePasswordRequest;
import com.naratrad.dto.UpdateProfileRequest;
import com.naratrad.dto.UserResponse;
import com.naratrad.entity.User;
import com.naratrad.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service to manage user profiles (update name, email, password)
 */
@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get user profile based on email (from JWT token)
     */
    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .lastLogin(user.getLastLogin())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Update user profile (full name and email)
     */
    public UserResponse updateProfile(String currentEmail, UpdateProfileRequest request) {
        // 1. Search user based on logged in email
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check if the new email is already used by another user
        if (!request.getEmail().equals(currentEmail)) {
            userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                throw new RuntimeException("Email is already in use by another user");
            });
        }

        // 3. Update data
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        // 4. Save to database
        User updatedUser = userRepository.save(user);

        // 5. Return response
        return UserResponse.builder()
                .id(updatedUser.getId())
                .email(updatedUser.getEmail())
                .fullName(updatedUser.getFullName())
                .role(updatedUser.getRole())
                .lastLogin(updatedUser.getLastLogin())
                .isActive(updatedUser.isActive())
                .createdAt(updatedUser.getCreatedAt())
                .build();
    }

    /**
     * Change user password
     * Requires old password validation for security
     */
    public void changePassword(String email, ChangePasswordRequest request) {
        // 1. Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Old password validation
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password does not match");
        }

        // 3. Encode new password and update
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // 4. Save to database
        userRepository.save(user);
    }
}