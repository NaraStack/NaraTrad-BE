package com.naratrad.controller;

import com.naratrad.dto.ChangePasswordRequest;
import com.naratrad.dto.SuccessResponse;
import com.naratrad.dto.UpdateProfileRequest;
import com.naratrad.dto.UserResponse;
import com.naratrad.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller untuk mengelola profile user
 * Endpoints:
 * - GET /api/profile - Get profile user yang sedang login
 * - PUT /api/profile - Update profile (name, email)
 * - PUT /api/profile/password - Change password
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    /**
     * GET /api/profile
     * Get current user profile info
     */
    @GetMapping
    public ResponseEntity<UserResponse> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponse profile = profileService.getProfile(email);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/profile
     * Update user profile (fullName and email)
     * Request body: { "fullName": "...", "email": "..." }
     */
    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponse updatedProfile = profileService.updateProfile(currentEmail, request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * PUT /api/profile/password
     * Change user password
     * Request body: { "oldPassword": "...", "newPassword": "..." }
     */
    @PutMapping("/password")
    public ResponseEntity<SuccessResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        profileService.changePassword(email, request);
        return ResponseEntity.ok(new SuccessResponse("Password changed successfully"));
    }
}