package com.naratrad.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Generate encrypted password untuk admin
        String rawPassword = "admin123"; // Ganti dengan password yang kamu mau
        String encryptedPassword = encoder.encode(rawPassword);

        System.out.println("Raw Password: " + rawPassword);
        System.out.println("Encrypted Password: " + encryptedPassword);
        System.out.println("\nCopy encrypted password di atas untuk insert ke database!");
    }
}