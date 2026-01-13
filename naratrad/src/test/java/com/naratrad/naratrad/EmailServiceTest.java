package com.naratrad.naratrad;

import com.naratrad.dto.ForgotPasswordRequest;
import com.naratrad.dto.ResetPasswordRequest;
import com.naratrad.entity.User;
import com.naratrad.repository.UserRepository;
import com.naratrad.service.AuthService;
import com.naratrad.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // Pastikan file env lokal sudah terisi
class EmailServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Test Full Flow: Forgot Password (HTTP API) & Reset Password")
    void testForgotAndResetPasswordFlow() {
        String targetEmail = "junit-auth@nara.com";
        String newPassword = "Admin123";

        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest();
        forgotRequest.setEmail(targetEmail);

        assertDoesNotThrow(() -> {
            String response = authService.forgotPassword(forgotRequest);
            assertEquals("Instruksi reset password telah dikirim ke email anda.", response);
        }, "Pengiriman email via HTTP API gagal atau timeout");

        User user = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di DB"));

        String realToken = user.getResetToken();
        assertNotNull(realToken, "Token tidak masuk ke database");
        System.out.println("DEBUG: Token asli dari DB adalah -> " + realToken);

        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setToken(realToken);
        resetRequest.setNewPassword(newPassword);

        String resetResult = authService.resetPassword(resetRequest);
        assertEquals("Password berhasil diubah! Silakan login kembali.", resetResult);

        User updatedUser = userRepository.findByEmail(targetEmail).get();
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
        assertNull(updatedUser.getResetToken(), "Token harusnya dihapus (null) setelah dipakai");
    }
}