package com.naratrad.config;

import com.naratrad.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Digunakan untuk mengacak password di AuthService
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Matikan CSRF karena kita menggunakan stateless JWT
                .cors(cors -> cors.configure(http))
                .csrf(csrf -> csrf.disable())

                // 2. Atur izin akses (siapa boleh akses apa)
                .authorizeHttpRequests(auth -> auth
                        // Endpoint Auth dan Swagger tidak perlu login
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Endpoint selain itu wajib membawa JWT Token yang valid
                        .anyRequest().authenticated()
                )

                // 3. Set session menjadi STATELESS (karena pakai JWT, kita tidak butuh session di server)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Pasang "Satpam" (JwtFilter) sebelum filter autentikasi standar Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}