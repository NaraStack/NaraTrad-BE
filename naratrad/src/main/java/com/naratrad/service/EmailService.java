package com.naratrad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetPasswordEmail(String toEmail, String token) {
        String resetLink = "http://localhost:4200/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("NaraTrad - Reset Password Request");
        message.setText("Halo!\n\nKami menerima permintaan untuk reset password kamu.\n" +
                "Silakan gunakan token berikut:\n\n" + token +
                "\n\nAtau akses melalui link ini:\n" + resetLink +
                "\n\nLink ini akan kadaluwarsa dalam 15 menit.");

        mailSender.send(message);
    }
}