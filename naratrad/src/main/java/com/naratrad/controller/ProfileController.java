package com.naratrad.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @GetMapping("/me")
    public String getMyProfile() {
        // Mengambil email dari token yang sedang login
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return "Halo! Kamu login sebagai: " + email;
    }
}