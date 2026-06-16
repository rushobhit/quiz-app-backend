package com.quizmicroservice.controller;

import com.quizmicroservice.model.User;
import com.quizmicroservice.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public record UserResponse(
            Long id,
            String firstName,
            String lastName,
            String username,
            String email,
            String role,
            String status,
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
            String permanentPincode
    ) {}

    public record CreateAdminRequest(
            @NotBlank String firstName,
            String lastName,
            @NotBlank String username,
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User is not authenticated"));
        }

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        UserResponse response = new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getFatherName(),
                user.getMotherName(),
                user.getDob() != null ? user.getDob().toString() : null,
                user.getInstitute(),
                user.getCurrentAddressLine1(),
                user.getCurrentAddressLine2(),
                user.getCurrentCity(),
                user.getCurrentState(),
                user.getCurrentPincode(),
                user.getPermanentAddressLine1(),
                user.getPermanentAddressLine2(),
                user.getPermanentCity(),
                user.getPermanentState(),
                user.getPermanentPincode()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admins")
    public ResponseEntity<?> createAdmin(@RequestBody @Valid CreateAdminRequest request) {
        try {
            User savedUser = userService.createAdmin(
                    request.firstName(),
                    request.lastName(),
                    request.username(),
                    request.email(),
                    request.password()
            );

            UserResponse response = new UserResponse(
                    savedUser.getId(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getRole(),
                    savedUser.getStatus(),
                    savedUser.getFatherName(),
                    savedUser.getMotherName(),
                    savedUser.getDob() != null ? savedUser.getDob().toString() : null,
                    savedUser.getInstitute(),
                    savedUser.getCurrentAddressLine1(),
                    savedUser.getCurrentAddressLine2(),
                    savedUser.getCurrentCity(),
                    savedUser.getCurrentState(),
                    savedUser.getCurrentPincode(),
                    savedUser.getPermanentAddressLine1(),
                    savedUser.getPermanentAddressLine2(),
                    savedUser.getPermanentCity(),
                    savedUser.getPermanentState(),
                    savedUser.getPermanentPincode()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> createPasswordResetToken(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        User user = userService.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        String token = userService.createPasswordResetToken(user);

        return ResponseEntity.ok(Map.of(
                "message", "Password reset token created successfully",
                "token", token
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");

            userService.resetPassword(token, newPassword);

            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/reset-password/validate")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        boolean valid = userService.isPasswordResetTokenValid(token);

        if (!valid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid or expired reset token"));
        }

        return ResponseEntity.ok(Map.of("message", "Reset token is valid"));
    }
}