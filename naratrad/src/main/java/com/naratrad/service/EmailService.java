package com.naratrad.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class EmailService {

    @Value("${MAILTRAP_API_TOKEN}")
    private String apiToken;

    @Value("${MAILTRAP_INBOX_ID}")
    private String inboxId;

    public void sendResetPasswordEmail(String toEmail, String token) {
        String resetLink = "https://nara-trad-fe.vercel.app/reset-password?token=" + token;
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://sandbox.api.mailtrap.io/api/send/" + inboxId;

        Map<String, Object> body = new HashMap<>();
        body.put("to", List.of(Map.of("email", toEmail)));
        body.put("from", Map.of("email", "hello@naratrad.com", "name", "NaraTrad"));
        body.put("subject", "NaraTrad - Reset Password Request");
        body.put("html", String.format(
                "<html>" +
                        "<body>" +
                        "<h3>Halo!</h3>" +
                        "<p>Kami menerima permintaan untuk reset password kamu.</p>" +
                        "<p>Silakan gunakan token berikut: <b>%s</b></p>" +
                        "<p>Atau klik tombol di bawah ini untuk reset password:</p>" +
                        "<a href='%s' target='_blank' style='padding: 10px 20px; background-color: #4F46E5; color: white; text-decoration: none; border-radius: 5px; display: inline-block;'>" +
                        "Reset Password" +
                        "</a>" +
                        "<p>Link ini akan kadaluwarsa dalam 15 menit.</p>" +
                        "<br><p>Salam,<br>Team NaraTrad</p>" +
                        "</body>" +
                        "</html>", token, resetLink));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Gagal kirim email via HTTP API: " + e.getMessage());
        }
    }
}