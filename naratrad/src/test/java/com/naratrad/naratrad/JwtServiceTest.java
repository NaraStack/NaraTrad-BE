package com.naratrad.naratrad;

import com.naratrad.entity.Role;
import com.naratrad.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void testJwtLifecycle_GenerateAndExtract() {
        // 1. Data Dummy
        String email = "tester@nara.com";
        Role role = Role.USER;

        // 2. Eksekusi: Generate Token
        String token = jwtService.generateToken(email, role);

        // 3. Verifikasi: Token tidak boleh kosong
        assertNotNull(token);

        // 4. Eksekusi: Ekstrak data dari token yang baru dibuat
        String extractedEmail = jwtService.extractEmail(token);
        String extractedRole = jwtService.extractRole(token);

        // 5. Verifikasi: Data yang diekstrak harus sama dengan data asli
        assertEquals(email, extractedEmail, "Email yang diekstrak salah!");
        assertEquals(role.name(), extractedRole, "Role yang diekstrak salah!");

        // 6. Verifikasi: Validitas Token
        assertTrue(jwtService.isTokenValid(token), "Token harusnya valid");
    }

    @Test
    void testInvalidToken() {
        // Skenario: Memberikan token asal-asalan
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.randomData";

        // Verifikasi: Harus mengembalikan false sesuai catch block di JwtService
        assertFalse(jwtService.isTokenValid(invalidToken), "Token acak tidak boleh dianggap valid");
    }
}