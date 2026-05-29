package com.quizmicroservice.controller;

import com.quizmicroservice.model.User;
import com.quizmicroservice.service.EmailService;
import com.quizmicroservice.service.OtpService;
import com.quizmicroservice.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final EmailService emailService;
    private final OtpService otpService;
    private final UserService userService;

    public AuthController(EmailService emailService,
                          OtpService otpService,
                          UserService userService) {
        this.emailService = emailService;
        this.otpService = otpService;
        this.userService = userService;
    }

    // ===== DTOs =====

    public record SignupOtpRequest(
            @NotBlank @Email String email
    ) {}

    public record VerifySignupOtpRequest(
            @NotBlank @Email String email,
            @NotBlank String otp
    ) {}

    public record StudentSignupDetailsRequest(
            @NotBlank String firstName,
            String lastName,
            @NotBlank String username,
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotBlank String role
    ) {}

    public record SimpleUserDto(
            String firstName,
            String lastName,
            String username,
            String email
    ) {}

    public record LoginResponse(
            String token,
            String role,
            SimpleUserDto user
    ) {}

    public record MessageResponse(String message) {}

    public record EmailRequest(
            @NotBlank @Email String email
    ) {}

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank String newPassword
    ) {}

    // ===== Existing endpoints (signup/login) =====
    // ... (unchanged code here: send-student-signup-otp, verify-student-signup-otp,
    //     student/signup-details, login) ...

    @PostMapping("/send-student-signup-otp")
    public ResponseEntity<MessageResponse> sendStudentSignupOtp(
            @RequestBody @Valid SignupOtpRequest request
    ) {
        String normalizedEmail = request.email().trim().toLowerCase();

        String otp = emailService.generateOtp();
        otpService.storeOtp(normalizedEmail, otp);
        emailService.sendSignupOtp(normalizedEmail, otp);

        return ResponseEntity.ok(
                new MessageResponse(
                        "OTP sent to " + normalizedEmail +
                        ". If you do not see an email, check spam or wait a few minutes."
                )
        );
    }

    @PostMapping("/verify-student-signup-otp")
    public ResponseEntity<MessageResponse> verifyStudentSignupOtp(
            @RequestBody @Valid VerifySignupOtpRequest request
    ) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String trimmedOtp = request.otp().trim();

        boolean valid = otpService.verifyOtp(normalizedEmail, trimmedOtp);

        if (!valid) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Invalid or expired OTP."));
        }

        return ResponseEntity.ok(
                new MessageResponse("OTP verified successfully.")
        );
    }

    @PostMapping("/student/signup-details")
    public ResponseEntity<MessageResponse> createStudentAccount(
            @RequestBody @Valid StudentSignupDetailsRequest request
    ) {
        try {
            userService.createStudent(
                    request.firstName(),
                    request.lastName(),
                    request.username(),
                    request.email(),
                    request.password()
            );

            return ResponseEntity.ok(
                    new MessageResponse("Student account created successfully.")
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody @Valid LoginRequest request
    ) {
        String email = request.email().trim().toLowerCase();
        String rawPassword = request.password();
        String requestedRole = request.role().trim().toUpperCase(); // STUDENT / ADMIN

        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Invalid credentials."));
        }

        if (!user.getRole().equalsIgnoreCase(requestedRole)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Invalid role for this account."));
        }

        if (!userService.matchesPassword(rawPassword, user.getPasswordHash())) {
           return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Invalid credentials."));
        }

        // TODO: replace with real JWT generation. For now, a dummy token.
        String fakeToken = "dummy-token-" + user.getId();

        SimpleUserDto userDto = new SimpleUserDto(
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail()
        );

        return ResponseEntity.ok(
                new LoginResponse(fakeToken, user.getRole(), userDto)
        );
    }

    // ===== New endpoints for Forgot Username / Password =====

    @PostMapping("/forgot-username")
    public ResponseEntity<MessageResponse> forgotUsername(
            @RequestBody @Valid EmailRequest request
    ) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userService.findByEmail(normalizedEmail);
        if (user != null) {
            String subject = "Your Quiz App username";
            String body = "Hello " + (user.getFirstName() != null ? user.getFirstName() : "") +
                    ",\n\nYour username is: " + user.getUsername() +
                    "\n\nIf you did not request this, you can ignore this email.";

            emailService.sendSimpleEmail(normalizedEmail, subject, body);
        }

        return ResponseEntity.ok(
                new MessageResponse(
                        "If an account exists with this email, your username has been sent."
                )
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @RequestBody @Valid EmailRequest request
    ) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userService.findByEmail(normalizedEmail);
        if (user != null) {
            String token = userService.createPasswordResetToken(user);
            String resetUrl = "http://localhost:5173/reset-password?token=" + token;

            String subject = "Reset your Quiz App password";
            String body = "Hello " + (user.getFirstName() != null ? user.getFirstName() : "") +
                    ",\n\nWe received a request to reset your password." +
                    "\nClick the link below to set a new password:" +
                    "\n" + resetUrl +
                    "\n\nIf you did not request this, you can ignore this email.";

            emailService.sendSimpleEmail(normalizedEmail, subject, body);
        }

        return ResponseEntity.ok(
                new MessageResponse(
                        "If an account exists with this email, a password reset link has been sent."
                )
        );
    }

    // ===== Reset password (called from ResetPasswordPage) =====

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request
    ) {
        String token = request.token().trim();
        String newPassword = request.newPassword().trim();

        User user = userService.validatePasswordResetToken(token);
        if (user == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Invalid or expired reset token."));
        }

        userService.updatePassword(user, newPassword);
        userService.consumePasswordResetToken(token);

        return ResponseEntity.ok(
                new MessageResponse("Password has been reset successfully.")
        );
    }
}