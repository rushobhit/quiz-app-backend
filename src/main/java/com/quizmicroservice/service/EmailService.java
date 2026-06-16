package com.quizmicroservice.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.mail.from:no-reply@quizapp.com}")
    private String fromAddress;

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return Integer.toString(otp);
    }

    public void sendSignupOtp(String toEmail, String otp) {
        String normalizedEmail = normalizeEmail(toEmail);
        String trimmedOtp = trimToNull(otp);

        if (normalizedEmail == null) {
            throw new IllegalArgumentException("Recipient email is required.");
        }

        if (trimmedOtp == null) {
            throw new IllegalArgumentException("OTP is required.");
        }

        String subject = "Your Quiz App signup OTP";
        String body = "Your OTP for student signup is: " + trimmedOtp +
                "\nIt is valid for " + otpExpiryMinutes + " minutes.";

        sendSimpleEmail(normalizedEmail, subject, body);
    }

    public void sendSimpleEmail(String toEmail, String subject, String text) {
        String normalizedEmail = normalizeEmail(toEmail);
        String trimmedSubject = subject != null ? subject.trim() : "";
        String trimmedText = text != null ? text.trim() : "";

        if (normalizedEmail == null) {
            throw new IllegalArgumentException("Recipient email is required.");
        }

        if (trimmedSubject.isBlank()) {
            throw new IllegalArgumentException("Email subject is required.");
        }

        if (trimmedText.isBlank()) {
            throw new IllegalArgumentException("Email body is required.");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress, "Quiz App Support");
            helper.setTo(normalizedEmail);
            helper.setSubject(trimmedSubject);
            helper.setText(trimmedText, false);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to send email.", ex);
        }
    }

    private String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}