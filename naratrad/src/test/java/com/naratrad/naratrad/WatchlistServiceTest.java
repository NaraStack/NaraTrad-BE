package com.naratrad.naratrad;

import com.naratrad.dto.WatchlistRequestDTO;
import com.naratrad.dto.WatchlistResponseDTO;
import com.naratrad.dto.WatchlistUpdateDTO;
import com.naratrad.entity.User;
import com.naratrad.repository.UserRepository;
import com.naratrad.repository.WatchlistRepository;
import com.naratrad.service.WatchlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // Memastikan koneksi ke Supabase via application-test.properties
class WatchlistServiceTest {

    @Autowired
    private WatchlistService watchlistService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WatchlistRepository watchlistRepository;

    private final String TEST_EMAIL = "junit-watchlist@nara.com";

    @BeforeEach
    void setUp() {
        // 1. Pastikan User Tester tersedia di database Supabase
        if (userRepository.findByEmail(TEST_EMAIL).isEmpty()) {
            User user = User.builder()
                    .email(TEST_EMAIL)
                    .password("password123")
                    .fullName("JUnit Watchlist Tester")
                    .role(com.naratrad.entity.Role.USER)
                    .isActive(true)
                    .build();
            userRepository.save(user);
        }

        // 2. Bersihkan watchlist lama agar tidak terjadi error 'Symbol already exists'
        User user = userRepository.findByEmail(TEST_EMAIL).get();
        watchlistRepository.deleteByUser(user);
    }

    @Test
    void testWatchlistFlow_RealDB() {
        WatchlistRequestDTO addDto = new WatchlistRequestDTO();
        addDto.setSymbol("BBCA");
        addDto.setTargetPrice(10000.0);

        WatchlistResponseDTO saved = watchlistService.addToWatchlist(TEST_EMAIL, addDto);

        assertNotNull(saved.getId());
        assertEquals("BBCA", saved.getSymbol());

        assertTrue(saved.getPrice() > 0, "Finnhub API harusnya mengembalikan harga > 0");

        WatchlistUpdateDTO updateDto = new WatchlistUpdateDTO();
        updateDto.setTargetPrice(12500.0);

        WatchlistResponseDTO updated = watchlistService.updateTargetPriceBySymbol(TEST_EMAIL, "BBCA", updateDto);

        assertEquals(12500.0, updated.getTargetPrice(), "Target price di database tidak berubah!");
    }

    @Test
    void testAddDuplicateSymbol_ShouldFail() {
        WatchlistRequestDTO dto = new WatchlistRequestDTO();
        dto.setSymbol("BBCA");
        dto.setTargetPrice(10000.0);
        watchlistService.addToWatchlist(TEST_EMAIL, dto);


        assertThrows(RuntimeException.class, () -> watchlistService.addToWatchlist(TEST_EMAIL, dto));
    }
}