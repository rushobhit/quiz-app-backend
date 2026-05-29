package com.quizmicroservice.service;

import com.quizmicroservice.model.User;
import com.quizmicroservice.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Simple in-memory reset-token store: token -> (userId, expiry)
    private static class PasswordResetEntry {
        Long userId;
        Instant expiresAt;

        PasswordResetEntry(Long userId, Instant expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, PasswordResetEntry> resetTokens = new ConcurrentHashMap<>();
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(30);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createStudent(String firstName,
                              String lastName,
                              String username,
                              String email,
                              String rawPassword) {

        String normalizedEmail = email.trim().toLowerCase();
        String trimmedUsername = username.trim();

        userRepository.findByEmail(normalizedEmail).ifPresent(u -> {
            throw new IllegalArgumentException("Email is already registered.");
        });

        userRepository.findByUsername(trimmedUsername).ifPresent(u -> {
            throw new IllegalArgumentException("Username is already taken.");
        });

        User user = new User();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName != null ? lastName.trim() : "");
        user.setUsername(trimmedUsername);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole("STUDENT");

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .orElse(null);
    }

    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Create a one-time password reset token for the given user.
     * Stores it in-memory with a 30-minute expiry and returns the token string.
     */
    public String createPasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(RESET_TOKEN_TTL);

        resetTokens.put(token, new PasswordResetEntry(user.getId(), expiresAt));
        return token;
    }

    /**
     * Validate token and fetch user.
     */
    public User validatePasswordResetToken(String token) {
        PasswordResetEntry entry = resetTokens.get(token);
        if (entry == null) {
            return null;
        }

        if (Instant.now().isAfter(entry.expiresAt)) {
            resetTokens.remove(token);
            return null;
        }

        return userRepository.findById(entry.userId).orElse(null);
    }

    /**
     * Clear a token once used.
     */
    public void consumePasswordResetToken(String token) {
        resetTokens.remove(token);
    }

    /**
     * Update a user's password (hashes with BCrypt and saves).
     */
    public void updatePassword(User user, String rawPassword) {
        String encoded = passwordEncoder.encode(rawPassword);
        user.setPasswordHash(encoded);
        userRepository.save(user);
    }
}