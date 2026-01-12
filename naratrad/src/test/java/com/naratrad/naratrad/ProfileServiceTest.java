package com.naratrad.naratrad;

import com.naratrad.dto.ChangePasswordRequest;
import com.naratrad.dto.UpdateProfileRequest;
import com.naratrad.dto.UserResponse;
import com.naratrad.entity.Role;
import com.naratrad.entity.User;
import com.naratrad.repository.UserRepository;
import com.naratrad.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String TEST_EMAIL = "profile-tester@nara.com";

    @BeforeEach
    void setUp() {
        // Membersihkan data lama agar hasil test konsisten di Supabase
        userRepository.findByEmail(TEST_EMAIL).ifPresent(user -> userRepository.delete(user));
        userRepository.findByEmail("new-email@nara.com").ifPresent(user -> userRepository.delete(user));

        // Membuat user awal untuk bahan pengujian
        User user = User.builder()
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode("Lama123"))
                .fullName("User Lama")
                .role(Role.USER)
                .isActive(true)
                .build();
        userRepository.save(user);
    }

    @Test
    void testUpdateProfile_Success() {
        // Skenario: Mengubah nama dan email user
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("User Baru");
        request.setEmail("new-email@nara.com");

        UserResponse response = profileService.updateProfile(TEST_EMAIL, request);

        // Verifikasi di database asli
        assertEquals("User Baru", response.getFullName());
        assertEquals("new-email@nara.com", response.getEmail());
        assertNotNull(userRepository.findByEmail("new-email@nara.com").orElse(null));
    }

    @Test
    void testChangePassword_Success() {
        // Skenario: Ganti password dengan validasi password lama
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("Lama123");
        request.setNewPassword("Baru123");

        // Pastikan tidak melempar exception saat password lama cocok
        assertDoesNotThrow(() -> profileService.changePassword(TEST_EMAIL, request));

        // Verifikasi password baru sudah ter-encode dan bisa dipakai login
        User updatedUser = userRepository.findByEmail(TEST_EMAIL).get();
        assertTrue(passwordEncoder.matches("Baru123", updatedUser.getPassword()));
    }

    @Test
    void testChangePassword_WrongOldPassword_ShouldFail() {
        // Skenario: Ganti password tapi password lama salah
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("SalahPass");
        request.setNewPassword("Baru123");

        // Sesuai ProfileService.java:94, harus melempar RuntimeException
        assertThrows(RuntimeException.class, () -> profileService.changePassword(TEST_EMAIL, request));
    }
}