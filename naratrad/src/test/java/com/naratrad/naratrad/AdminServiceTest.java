package com.naratrad.naratrad;

import com.naratrad.dto.AdminDashboardDTO;
import com.naratrad.entity.Role;
import com.naratrad.entity.User;
import com.naratrad.repository.UserRepository;
import com.naratrad.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // Memastikan koneksi ke database Supabase melalui application-test.properties
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    private final String ADMIN_EMAIL = "admin-junit@nara.com";

    @BeforeEach
    void setUp() {
        // Pastikan setidaknya ada satu user admin untuk divalidasi datanya di database
        if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
            User admin = User.builder()
                    .email(ADMIN_EMAIL)
                    .password("admin123")
                    .fullName("JUnit Admin")
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
        }
    }

    @Test
    void testGetDetailedStats_RealDB() {
        // Eksekusi: Mengambil statistik dashboard admin dari data real di Supabase
        AdminDashboardDTO stats = adminService.getDetailedStats();

        // Verifikasi Metrik User
        assertNotNull(stats);
        assertTrue(stats.getTotalUsers() > 0, "Total users di database tidak boleh nol");

        // Verifikasi Data Pertumbuhan User (Chart Data)
        List<Map<String, Object>> growthData = stats.getUserGrowthData();
        assertNotNull(growthData);
        assertFalse(growthData.isEmpty(), "Data pertumbuhan user harus tersedia");

        // Verifikasi Metrik Sistem (Log API)
        // Jika kamu sudah pernah menjalankan test sebelumnya, totalApiCalls harusnya > 0
        assertNotNull(stats.getAvgResponseTime());

        System.out.println("Total Users di Supabase: " + stats.getTotalUsers());
        System.out.println("Avg Response Time API: " + stats.getAvgResponseTime() + " ms");
    }
}