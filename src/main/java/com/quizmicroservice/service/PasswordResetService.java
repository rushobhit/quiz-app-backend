package com.quizmicroservice.service;

import com.quizmicroservice.exception.ResourceNotFoundException;
import com.quizmicroservice.model.PasswordResetToken;
import com.quizmicroservice.model.User;
import com.quizmicroservice.repository.PasswordResetTokenRepository;
import com.quizmicroservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.base-url:http://localhost:5173/quiz-app-frontend}")
    private String frontendBaseUrl;

    @Value("${app.mail.from:no-reply@quizapp.com}")
    private String fromEmail;

    @Value("${app.reset-password.expiry-minutes:15}")
    private long expiryMinutes;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository passwordResetTokenRepository,
                                JavaMailSender mailSender,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    public void sendResetLink(String email) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));

        createAndSendResetToken(user);
    }

    public void sendResetLinkByDetails(String identifierType, String identifier, String dob) {
        String normalizedIdentifierType = identifierType == null ? "" : identifierType.trim().toLowerCase();
        String normalizedIdentifier = identifier == null ? "" : identifier.trim();
        String normalizedDob = dob == null ? "" : dob.trim();

        User user;

        switch (normalizedIdentifierType) {
            case "email" -> user = userRepository.findByEmailIgnoreCase(normalizedIdentifier.toLowerCase())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found."));
            case "username" -> user = userRepository.findByUsernameIgnoreCase(normalizedIdentifier)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found."));
            default -> throw new IllegalArgumentException("identifierType must be either 'email' or 'username'.");
        }

        validateDob(user, normalizedDob);
        createAndSendResetToken(user);
    }

    public void resetPassword(String token, String newPassword) {
        String normalizedToken = token == null ? "" : token.trim();
        String normalizedPassword = newPassword == null ? "" : newPassword.trim();

        if (normalizedToken.isBlank()) {
            throw new IllegalArgumentException("Password reset token is required.");
        }

        if (normalizedPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required.");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(normalizedToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid password reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(normalizedPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    public boolean isValidToken(String token) {
        String normalizedToken = token == null ? "" : token.trim();

        if (normalizedToken.isBlank()) {
            return false;
        }

        return passwordResetTokenRepository.findByToken(normalizedToken)
                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    private void createAndSendResetToken(User user) {
        passwordResetTokenRepository.findByUser(user)
                .ifPresent(passwordResetTokenRepository::delete);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(expiryMinutes));

        passwordResetTokenRepository.save(resetToken);

        String resetLink = buildResetLink(token);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Password Reset Request");
        message.setText(
                "Hello " + (user.getFirstName() != null && !user.getFirstName().isBlank() ? user.getFirstName().trim() : "User") + ",\n\n" +
                "Click the link below to reset your password:\n" +
                resetLink + "\n\n" +
                "This link will expire in " + expiryMinutes + " minutes.\n\n" +
                "If you did not request this, please ignore this email."
        );

        mailSender.send(message);
    }

    private String buildResetLink(String token) {
        String baseUrl = frontendBaseUrl == null ? "" : frontendBaseUrl.trim();
        if (baseUrl.isBlank()) {
            throw new IllegalStateException("Frontend base URL is not configured.");
        }

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl + "/reset-password?token=" + token;
    }

    private void validateDob(User user, String dob) {
        LocalDate providedDob;

        try {
            providedDob = LocalDate.parse(dob);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date of birth format. Use yyyy-MM-dd.");
        }

        if (user.getDob() == null || !user.getDob().equals(providedDob)) {
            throw new IllegalArgumentException("Provided details do not match our records.");
        }
    }
}