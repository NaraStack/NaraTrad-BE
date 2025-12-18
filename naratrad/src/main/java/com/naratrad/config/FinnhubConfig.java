package com.naratrad.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class FinnhubConfig {

    // Bean ini digunakan oleh Service untuk melakukan HTTP Call ke Finnhub
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Konfigurasi Swagger UI (SpringDoc) agar judul di dokumentasi sesuai nama project
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NaraTrad Portfolio API")
                        .version("1.0")
                        .description("API untuk mengelola portofolio saham dengan harga real-time dari Finnhub."));
    }
}