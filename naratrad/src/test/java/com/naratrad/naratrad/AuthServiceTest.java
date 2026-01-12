package com.naratrad.naratrad;

import com.naratrad.dto.LoginRequest;
import com.naratrad.dto.LoginResponse;
import com.naratrad.dto.RegisterRequest;
import com.naratrad.entity.User;
import com.naratrad.repository.UserRepository;
import com.naratrad.repository.PortfolioRepository;
import com.naratrad.repository.WatchlistRepository;
import com.naratrad.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String TEST_EMAIL = "junit-auth@nara.com";

    @BeforeEach
    void cleanUp() {
        userRepository.findByEmail(TEST_EMAIL).ifPresent(user -> {
            portfolioRepository.deleteByUser(user);
            watchlistRepository.deleteByUser(user);
            userRepository.delete(user);
        });
    }

    @Test
    void testFullAuthFlow_RealDB() {
        RegisterRequest regRequest = new RegisterRequest();
        regRequest.setEmail(TEST_EMAIL);
        regRequest.setPassword("Rahasia123");
        regRequest.setFullName("JUnit Tester");

        User savedUser = authService.register(regRequest);

        assertNotNull(savedUser.getId());
        assertEquals(TEST_EMAIL, savedUser.getEmail());
        assertEquals("JUnit Tester", savedUser.getFullName());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword("Rahasia123");

        LoginResponse loginResponse = authService.login(loginRequest);

        assertNotNull(loginResponse.getToken());
        assertEquals(TEST_EMAIL, loginResponse.getUser().getEmail());
    }

    @Test
    void testLogin_WrongPassword_ShouldFail() {
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail(TEST_EMAIL);
        reg.setPassword("Benar123");
        reg.setFullName("JUnit Tester");
        authService.register(reg);

        LoginRequest login = new LoginRequest();
        login.setEmail(TEST_EMAIL);
        login.setPassword("Salah123");

        assertThrows(RuntimeException.class, () -> authService.login(login));
    }
}