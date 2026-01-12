package com.naratrad.naratrad;

import com.naratrad.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test") // Memastikan menggunakan profil test (Supabase & Mail Config)
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    void testSendResetPasswordEmail_NoException() {
        // SKENARIO: Memastikan method pengiriman email tidak crash/error
        String testEmail = "tester@nara.com";
        String dummyToken = "abc-123-token-test";

        // Verifikasi bahwa pemanggilan sendResetPasswordEmail tidak melempar exception
        // Jika ada masalah koneksi SMTP, test ini akan gagal (Fail)
        assertDoesNotThrow(() -> {
            emailService.sendResetPasswordEmail(testEmail, dummyToken);
        }, "Pengiriman email harusnya tidak melempar error");
    }
}