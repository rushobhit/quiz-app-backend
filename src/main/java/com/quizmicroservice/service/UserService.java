package com.quizmicroservice.service;

import com.quizmicroservice.model.User;
import com.quizmicroservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,20}$");
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static class PasswordResetEntry {
        final Long userId;
        final Instant expiresAt;

        PasswordResetEntry(Long userId, Instant expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, PasswordResetEntry> resetTokens = new ConcurrentHashMap<>();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createStudent(String firstName,
                              String lastName,
                              String username,
                              String email,
                              String rawPassword,
                              String fatherName,
                              String motherName,
                              String dob,
                              String institute,
                              String currentAddressLine1,
                              String currentAddressLine2,
                              String currentCity,
                              String currentState,
                              String currentPincode,
                              String permanentAddressLine1,
                              String permanentAddressLine2,
                              String permanentCity,
                              String permanentState,
                              String permanentPincode) {

        String trimmedFirstName = trimToNull(firstName);
        String trimmedLastName = trimToEmpty(lastName);
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);
        String trimmedPassword = trimToNull(rawPassword);

        String trimmedFatherName = trimToNull(fatherName);
        String trimmedMotherName = trimToNull(motherName);
        String trimmedInstitute = trimToNull(institute);

        String trimmedCurrentAddressLine1 = trimToNull(currentAddressLine1);
        String trimmedCurrentAddressLine2 = trimToEmpty(currentAddressLine2);
        String trimmedCurrentCity = trimToNull(currentCity);
        String trimmedCurrentState = trimToNull(currentState);
        String trimmedCurrentPincode = trimToNull(currentPincode);

        String trimmedPermanentAddressLine1 = trimToNull(permanentAddressLine1);
        String trimmedPermanentAddressLine2 = trimToEmpty(permanentAddressLine2);
        String trimmedPermanentCity = trimToNull(permanentCity);
        String trimmedPermanentState = trimToNull(permanentState);
        String trimmedPermanentPincode = trimToNull(permanentPincode);

        if (trimmedFirstName == null) {
            throw new IllegalArgumentException("First name is required.");
        }

        if (normalizedUsername == null) {
            throw new IllegalArgumentException("Username is required.");
        }

        validateUsername(normalizedUsername);

        if (normalizedEmail == null) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (trimmedPassword == null) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (trimmedPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }

        if (trimmedFatherName == null) {
            throw new IllegalArgumentException("Father name is required.");
        }

        if (trimmedMotherName == null) {
            throw new IllegalArgumentException("Mother name is required.");
        }

        if (trimmedInstitute == null) {
            throw new IllegalArgumentException("Institute is required.");
        }

        if (trimmedCurrentAddressLine1 == null || trimmedCurrentCity == null ||
                trimmedCurrentState == null || trimmedCurrentPincode == null) {
            throw new IllegalArgumentException("Current address is incomplete.");
        }

        if (trimmedPermanentAddressLine1 == null || trimmedPermanentCity == null ||
                trimmedPermanentState == null || trimmedPermanentPincode == null) {
            throw new IllegalArgumentException("Permanent address is incomplete.");
        }

        if (existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        if (existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        LocalDate parsedDob = parseDob(dob);

        User user = new User();
        user.setFirstName(trimmedFirstName);
        user.setLastName(trimmedLastName);
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(trimmedPassword));
        user.setRole("STUDENT");
        user.setStatus("ACTIVE");

        user.setFatherName(trimmedFatherName);
        user.setMotherName(trimmedMotherName);
        user.setDob(parsedDob);
        user.setInstitute(trimmedInstitute);

        user.setCurrentAddressLine1(trimmedCurrentAddressLine1);
        user.setCurrentAddressLine2(trimmedCurrentAddressLine2);
        user.setCurrentCity(trimmedCurrentCity);
        user.setCurrentState(trimmedCurrentState);
        user.setCurrentPincode(trimmedCurrentPincode);

        user.setPermanentAddressLine1(trimmedPermanentAddressLine1);
        user.setPermanentAddressLine2(trimmedPermanentAddressLine2);
        user.setPermanentCity(trimmedPermanentCity);
        user.setPermanentState(trimmedPermanentState);
        user.setPermanentPincode(trimmedPermanentPincode);

        return userRepository.save(user);
    }

    public User createAdmin(String firstName,
                            String lastName,
                            String username,
                            String email,
                            String rawPassword) {

        String trimmedFirstName = trimToNull(firstName);
        String trimmedLastName = trimToEmpty(lastName);
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);
        String trimmedPassword = trimToNull(rawPassword);

        if (trimmedFirstName == null) {
            throw new IllegalArgumentException("First name is required.");
        }

        if (normalizedUsername == null) {
            throw new IllegalArgumentException("Username is required.");
        }

        validateUsername(normalizedUsername);

        if (normalizedEmail == null) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (trimmedPassword == null) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (trimmedPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }

        if (existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        if (existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        User user = new User();
        user.setFirstName(trimmedFirstName);
        user.setLastName(trimmedLastName);
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(trimmedPassword));
        user.setRole("ADMIN");
        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }
        return userRepository.save(user);
    }

    public User findById(Long id) {
        if (id == null) {
            return null;
        }
        return userRepository.findById(id).orElse(null);
    }

    public User findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
    }

    public User findByUsername(String username) {
        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername == null) {
            return null;
        }
        return userRepository.findByUsernameIgnoreCase(normalizedUsername).orElse(null);
    }

    public User findByEmailOrUsername(String emailOrUsername) {
        String normalizedValue = trimToNull(emailOrUsername);
        if (normalizedValue == null) {
            return null;
        }

        return userRepository
                .findTopByEmailIgnoreCaseOrUsernameIgnoreCase(normalizedValue, normalizedValue.toLowerCase())
                .orElse(null);
    }

    public boolean existsByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return false;
        }
        return userRepository.existsByEmailIgnoreCase(normalizedEmail);
    }

    public boolean existsByUsername(String username) {
        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername == null) {
            return false;
        }
        return userRepository.existsByUsernameIgnoreCase(normalizedUsername);
    }

    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        String trimmedPassword = trimToNull(rawPassword);

        if (trimmedPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }

        return passwordEncoder.matches(trimmedPassword, encodedPassword);
    }

    public String createPasswordResetToken(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User must not be null for reset token.");
        }

        cleanupExpiredResetTokens();
        resetTokens.entrySet().removeIf(entry -> entry.getValue().userId.equals(user.getId()));

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(RESET_TOKEN_TTL);

        resetTokens.put(token, new PasswordResetEntry(user.getId(), expiresAt));
        return token;
    }

    public User validatePasswordResetToken(String token) {
        String trimmedToken = trimToNull(token);
        if (trimmedToken == null) {
            return null;
        }

        PasswordResetEntry entry = resetTokens.get(trimmedToken);
        if (entry == null) {
            return null;
        }

        if (Instant.now().isAfter(entry.expiresAt)) {
            resetTokens.remove(trimmedToken);
            return null;
        }

        return userRepository.findById(entry.userId).orElse(null);
    }

    public boolean isPasswordResetTokenValid(String token) {
        return validatePasswordResetToken(token) != null;
    }

    public void consumePasswordResetToken(String token) {
        String trimmedToken = trimToNull(token);
        if (trimmedToken == null) {
            return;
        }
        resetTokens.remove(trimmedToken);
    }

    public void updatePassword(User user, String rawPassword) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        String trimmedPassword = trimToNull(rawPassword);
        if (trimmedPassword == null) {
            throw new IllegalArgumentException("New password is required.");
        }

        if (trimmedPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }

        user.setPasswordHash(passwordEncoder.encode(trimmedPassword));
        userRepository.save(user);
    }

    public void resetPassword(String token, String newPassword) {
        User user = validatePasswordResetToken(token);

        if (user == null) {
            throw new IllegalArgumentException("Invalid or expired reset token.");
        }

        updatePassword(user, newPassword);
        consumePasswordResetToken(token);
    }

    public void cleanupExpiredResetTokens() {
        Instant now = Instant.now();
        resetTokens.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt));
    }

    private void validateUsername(String username) {
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Username must be 4 to 20 characters long and can contain only letters, numbers, and underscore."
            );
        }
    }

    private LocalDate parseDob(String dob) {
        String trimmedDob = trimToNull(dob);
        if (trimmedDob == null) {
            throw new IllegalArgumentException("Date of birth is required.");
        }

        try {
            return LocalDate.parse(trimmedDob);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date of birth format. Use yyyy-MM-dd.");
        }
    }

    private String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private String normalizeUsername(String username) {
        String trimmed = trimToNull(username);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}