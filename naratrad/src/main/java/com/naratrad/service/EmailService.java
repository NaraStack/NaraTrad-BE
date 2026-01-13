package com.naratrad.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetPasswordEmail(String toEmail, String token) throws MessagingException {
        String resetLink = "https://nara-trad-fe.vercel.app/reset-password?token=" + token;

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        String htmlContent = String.format(
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
                        "</html>", token, resetLink);

        helper.setTo(toEmail);
        helper.setSubject("NaraTrad - Reset Password Request");
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }
}