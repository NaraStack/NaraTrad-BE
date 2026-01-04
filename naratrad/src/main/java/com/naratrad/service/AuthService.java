package com.naratrad.service;

import com.naratrad.dto.LoginRequest;
import com.naratrad.entity.User;
import com.naratrad.entity.Role;
import com.naratrad.dto.RegisterRequest;
import com.naratrad.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        // 1. Cek apakah email sudah ada
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Error: Email sudah terdaftar!");
        }

        // 2. Map DTO ke Entity & Encrypt Password
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER) // Default sebagai USER
                .isActive(true)
                .build();

        // 3. Simpan ke Supabase
        return userRepository.save(user);
    }

    @Autowired
    private JwtService jwtService;

    public String login(LoginRequest request) {
        // 1. Cari user berdasarkan email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan!"));

        // 2. Cek apakah password cocok
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password salah!");
        }

        // 3. Jika cocok, buatkan token
        return jwtService.generateToken(user.getEmail());
    }
}