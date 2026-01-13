package com.naratrad.service;

import com.naratrad.dto.CreateAdminRequest;
import com.naratrad.dto.LoginRequest;
import com.naratrad.dto.LoginResponse;
import com.naratrad.dto.UserResponse;
import com.naratrad.entity.User;
import com.naratrad.entity.Role;
import com.naratrad.dto.RegisterRequest;
import com.naratrad.repository.UserRepository;
import com.naratrad.dto.ForgotPasswordRequest;
import com.naratrad.dto.ResetPasswordRequest;
import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Error: Email sudah terdaftar!");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password salah!");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .build())
                .build();
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));

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

    @Value("${admin.secret.key:NARATRAD_ADMIN_SECRET_2026}")
    private String adminSecretKey;


    public User createAdmin(CreateAdminRequest request) {
        if (!adminSecretKey.equals(request.getSecretKey())) {
            throw new RuntimeException("Error: Secret key tidak valid! Tidak bisa create admin.");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Error: Email sudah terdaftar!");
        }

        User admin = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.ADMIN) // Set role sebagai ADMIN
                .isActive(true)
                .build();

        return userRepository.save(admin);
    }

    @Autowired
    private EmailService emailService;

    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan!"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        try {
            emailService.sendResetPasswordEmail(user.getEmail(), token);
        } catch (jakarta.mail.MessagingException e) {
            System.err.println("Gagal mengirim email: " + e.getMessage());

            throw new RuntimeException("Gagal mengirim email reset password. Silakan coba lagi nanti.");
        }

        return "Instruksi reset password telah dikirim ke email anda.";
    }

    public String resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token reset tidak valid!"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token sudah kadaluwarsa!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "Password berhasil diubah! Silakan login kembali.";
    }
}