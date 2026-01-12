package com.naratrad.naratrad;

import com.naratrad.entity.Portfolio;
import com.naratrad.entity.User;
import com.naratrad.repository.UserRepository;
import com.naratrad.repository.PortfolioRepository;
import com.naratrad.service.PortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PortfolioServiceTest {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    private final String TEST_EMAIL = "mara.azwa@gmail.com";

    @BeforeEach
    void setUp() {
        // 1. Pastikan User Tester ada di Supabase
        if (userRepository.findByEmail(TEST_EMAIL).isEmpty()) {
            User user = User.builder()
                    .email(TEST_EMAIL)
                    .password("Ammara123")
                    .fullName("Ammara Azwadiena")
                    .build();
            userRepository.save(user);
        }

        User user = userRepository.findByEmail(TEST_EMAIL).get();
        portfolioRepository.deleteByUser(user);
    }

    @Test
    void testAddOrUpdateStock_WeightedAverageCalculation() {

        Portfolio buy1 = new Portfolio();
        buy1.setSymbol("AAPL");
        buy1.setQuantity(10);
        buy1.setPurchasePrice(150.0);
        portfolioService.addOrUpdateStock(TEST_EMAIL, buy1);

        // Pembelian 2: 10 lembar AAPL di harga 200.0
        Portfolio buy2 = new Portfolio();
        buy2.setSymbol("AAPL");
        buy2.setQuantity(10);
        buy2.setPurchasePrice(200.0);

        // Eksekusi pembelian kedua (akan mentrigger logika weighted average)
        Portfolio result = portfolioService.addOrUpdateStock(TEST_EMAIL, buy2);

        // VERIFIKASI LOGIKA:
        // Total Qty: 10 + 10 = 20
        // Avg Price: ((10 * 150) + (10 * 200)) / 20 = (1500 + 2000) / 20 = 175.0
        assertEquals(20, result.getQuantity());
        assertEquals(175.0, result.getPurchasePrice(), "Perhitungan Weighted Average Price salah!");
    }

    @Test
    void testAddStock_AutoFetchPrice() {
        // SKENARIO: Beli saham tanpa memasukkan harga (Auto-fetch Finnhub)
        Portfolio stock = new Portfolio();
        stock.setSymbol("MSFT");
        stock.setQuantity(5);
        stock.setPurchasePrice(0.0); // Harga 0 akan memicu getLivePrice()

        Portfolio result = portfolioService.addOrUpdateStock(TEST_EMAIL, stock);

        // VERIFIKASI:
        assertNotNull(result.getPurchasePrice());
        assertTrue(result.getPurchasePrice() > 0, "Gagal mengambil harga live dari Finnhub");
    }
}