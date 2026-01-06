package com.naratrad.controller;

import com.naratrad.dto.*;
import com.naratrad.entity.User;
import com.naratrad.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        // Handle validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            error -> error.getField(),
                            error -> error.getDefaultMessage()
                    ));
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            User user = authService.register(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User berhasil didaftarkan");
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        // Handle validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            error -> error.getField(),
                            error -> error.getDefaultMessage()
                    ));
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized - Token tidak valid");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String email = authentication.getName();
            UserResponse user = authService.getCurrentUser(email);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String result = authService.forgotPassword(request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            String result = authService.resetPassword(request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint khusus untuk create admin user (ONE-TIME USE)
     */
    // @PostMapping("/create-admin")
    // public ResponseEntity<?> createAdmin(@Valid @RequestBody CreateAdminRequest request, BindingResult bindingResult) {
    //     // Handle validation errors
    //     if (bindingResult.hasErrors()) {
    //         Map<String, String> errors = bindingResult.getFieldErrors().stream()
    //                 .collect(Collectors.toMap(
    //                         error -> error.getField(),
    //                         error -> error.getDefaultMessage()
    //                 ));
    //         return ResponseEntity.badRequest().body(errors);
    //     }

    //     try {
    //         User admin = authService.createAdmin(request);
    //         Map<String, Object> response = new HashMap<>();
    //         response.put("message", "Admin berhasil dibuat!");
    //         response.put("email", admin.getEmail());
    //         response.put("fullName", admin.getFullName());
    //         response.put("role", admin.getRole());
    //         response.put("warning", "PENTING: Segera hapus/disable endpoint /create-admin setelah selesai!");
    //         return ResponseEntity.status(HttpStatus.CREATED).body(response);
    //     } catch (RuntimeException e) {
    //         Map<String, String> error = new HashMap<>();
    //         error.put("error", e.getMessage());
    //         return ResponseEntity.badRequest().body(error);
    //     }
    // }
}