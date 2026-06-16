package com.quizmicroservice.controller;

import com.quizmicroservice.dto.AuthActivityDto;
import com.quizmicroservice.model.User;
import com.quizmicroservice.security.JwtService;
import com.quizmicroservice.service.AuthActivityService;
import com.quizmicroservice.service.EmailService;
import com.quizmicroservice.service.OtpService;
import com.quizmicroservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final EmailService emailService;
    private final OtpService otpService;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthActivityService authActivityService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public AuthController(EmailService emailService,
                          OtpService otpService,
                          UserService userService,
                          JwtService jwtService,
                          AuthActivityService authActivityService) {
        this.emailService = emailService;
        this.otpService = otpService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.authActivityService = authActivityService;
    }

    public record SignupOtpRequest(
            @NotBlank @Email String email
    ) {}

    public record VerifySignupOtpRequest(
            @NotBlank @Email String email,
            @NotBlank String otp
    ) {}

    public record AddressRequest(
            @NotBlank String line1,
            String line2,
            @NotBlank String city,
            @NotBlank String state,
            @NotBlank String pincode
    ) {}

    public record StudentSignupDetailsRequest(
            @NotBlank String firstName,
            String lastName,
            @NotBlank String username,
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotBlank String fatherName,
            @NotBlank String motherName,
            @NotBlank String dob,
            @NotBlank String institute,
            @NotNull @Valid AddressRequest currentAddress,
            @NotNull @Valid AddressRequest permanentAddress
    ) {}

    public record LoginRequest(
            @NotBlank String emailOrUsername,
            @NotBlank String password,
            @NotBlank String role
    ) {}

    public record EmailRequest(
            @NotBlank @Email String email
    ) {}

    public record ForgotUsernameByDetailsRequest(
            @NotBlank @Email String email,
            @NotBlank String dob
    ) {}

    public record ForgotPasswordByDetailsRequest(
            @NotBlank String identifierType,
            @NotBlank String identifier,
            @NotBlank String dob
    ) {}

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank String newPassword
    ) {}

    public record LogoutRequest(
            String email,
            String role
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

    @PostMapping("/send-student-signup-otp")
    public ResponseEntity<MessageResponse> sendStudentSignupOtp(
            @RequestBody @Valid SignupOtpRequest request
    ) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User existingUser = userService.findByEmail(normalizedEmail);
        if (existingUser != null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("An account already exists with this email."));
        }

        String otp = emailService.generateOtp();
        otpService.storeOtp(normalizedEmail, otp);
        emailService.sendSignupOtp(normalizedEmail, otp);

        return ResponseEntity.ok(
                new MessageResponse("OTP sent successfully to your Gmail.")
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
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid or expired OTP."));
        }

        otpService.markVerified(normalizedEmail);

        return ResponseEntity.ok(
                new MessageResponse("OTP verified successfully.")
        );
    }

    @PostMapping("/student/signup-details")
    public ResponseEntity<MessageResponse> createStudentAccount(
            @RequestBody @Valid StudentSignupDetailsRequest request
    ) {
        String email = request.email().trim().toLowerCase();
        String username = request.username().trim();

        if (!otpService.isVerified(email)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Please verify your email with OTP before creating an account."));
        }

        try {
            userService.createStudent(
                    request.firstName().trim(),
                    request.lastName() != null ? request.lastName().trim() : null,
                    username,
                    email,
                    request.password(),
                    request.fatherName().trim(),
                    request.motherName().trim(),
                    request.dob().trim(),
                    request.institute().trim(),
                    request.currentAddress().line1().trim(),
                    request.currentAddress().line2() != null ? request.currentAddress().line2().trim() : null,
                    request.currentAddress().city().trim(),
                    request.currentAddress().state().trim(),
                    request.currentAddress().pincode().trim(),
                    request.permanentAddress().line1().trim(),
                    request.permanentAddress().line2() != null ? request.permanentAddress().line2().trim() : null,
                    request.permanentAddress().city().trim(),
                    request.permanentAddress().state().trim(),
                    request.permanentAddress().pincode().trim()
            );

            otpService.clearOtp(email);

            return ResponseEntity.ok(
                    new MessageResponse("Student account created successfully.")
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request,
                                   HttpServletRequest httpServletRequest) {
        String emailOrUsername = request.emailOrUsername().trim();
        String rawPassword = request.password();
        String requestedRole = request.role().trim().toUpperCase();

        User user = userService.findByEmailOrUsername(emailOrUsername);

        if (user == null) {
            authActivityService.logFailedLogin(
                    emailOrUsername,
                    httpServletRequest,
                    "Login failed: user not found."
            );

            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid credentials."));
        }

        if (!user.getRole().equalsIgnoreCase(requestedRole)) {
            authActivityService.logFailedLogin(
                    user.getEmail(),
                    httpServletRequest,
                    "Login failed: invalid role."
            );

            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid role for this account."));
        }

        if (!userService.matchesPassword(rawPassword, user.getPasswordHash())) {
            authActivityService.logFailedLogin(
                    user.getEmail(),
                    httpServletRequest,
                    "Login failed: incorrect password."
            );

            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid credentials."));
        }

        String token = jwtService.generateToken(
                user.getEmail().trim().toLowerCase(),
                user.getRole().trim().toUpperCase()
        );

        authActivityService.logLoginSuccess(
                user.getId(),
                user.getEmail(),
                buildFullName(user),
                httpServletRequest
        );

        SimpleUserDto userDto = new SimpleUserDto(
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail()
        );

        return ResponseEntity.ok(new LoginResponse(token, user.getRole(), userDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestBody(required = false) LogoutRequest request,
                                                  HttpServletRequest httpServletRequest) {
        if (request != null && request.email() != null && !request.email().isBlank()) {
            String email = request.email().trim().toLowerCase();
            User user = userService.findByEmail(email);

            if (user != null) {
                authActivityService.logLogoutSuccess(
                        user.getId(),
                        user.getEmail(),
                        buildFullName(user),
                        httpServletRequest
                );
            }
        }

        return ResponseEntity.ok(new MessageResponse("Logged out successfully."));
    }

    @PostMapping("/login-log")
    public ResponseEntity<MessageResponse> createLoginLog(
            @RequestBody(required = false) AuthActivityDto request,
            HttpServletRequest httpServletRequest
    ) {
        if (request == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Request body is required."));
        }

        String fullName = request.getFullName() != null ? request.getFullName().trim() : null;
        String deviceInfo = request.getDeviceInfo() != null ? request.getDeviceInfo().trim() : null;

        if (request.getUserId() != null) {
            User user = userService.findById(request.getUserId());

            if (user != null) {
                authActivityService.logLoginSuccess(
                        user.getId(),
                        user.getEmail(),
                        fullName != null && !fullName.isBlank() ? fullName : buildFullName(user),
                        httpServletRequest
                );

                return ResponseEntity.ok(new MessageResponse("Log recorded successfully."));
            }
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            User user = userService.findByEmail(request.getEmail());

            if (user != null) {
                authActivityService.logLoginSuccess(
                        user.getId(),
                        user.getEmail(),
                        fullName != null && !fullName.isBlank() ? fullName : buildFullName(user),
                        httpServletRequest
                );

                return ResponseEntity.ok(new MessageResponse("Log recorded successfully."));
            }
        }

        authActivityService.logFailedLogin(
                request.getEmail() != null ? request.getEmail() : "unknown",
                httpServletRequest,
                "Login activity recorded from device: " + (deviceInfo != null ? deviceInfo : "Unknown device")
        );

        return ResponseEntity.ok(new MessageResponse("Log recorded successfully."));
    }

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
                new MessageResponse("If an account exists with this email, your username has been sent.")
        );
    }

    @PostMapping("/forgot-username-by-details")
    public ResponseEntity<MessageResponse> forgotUsernameByDetails(
            @RequestBody @Valid ForgotUsernameByDetailsRequest request
    ) {
        String normalizedEmail = request.email().trim().toLowerCase();
        LocalDate dob;

        try {
            dob = parseDob(request.dob());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(ex.getMessage()));
        }

        User user = userService.findByEmail(normalizedEmail);
        if (user != null
                && user.getDob() != null
                && user.getDob().equals(dob)) {

            String subject = "Your Quiz App username";
            String body = "Hello " + (user.getFirstName() != null ? user.getFirstName() : "") +
                    ",\n\nYour username is: " + user.getUsername() +
                    "\n\nIf you did not request this, you can ignore this email.";

            emailService.sendSimpleEmail(normalizedEmail, subject, body);
        }

        return ResponseEntity.ok(
                new MessageResponse("If the provided details match an account, your username has been sent.")
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
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            String subject = "Reset your Quiz App password";
            String body = "Hello " + (user.getFirstName() != null ? user.getFirstName() : "") +
                    ",\n\nWe received a request to reset your password." +
                    "\nClick the link below to set a new password:" +
                    "\n" + resetUrl +
                    "\n\nIf you did not request this, you can ignore this email.";

            emailService.sendSimpleEmail(normalizedEmail, subject, body);
        }

        return ResponseEntity.ok(
                new MessageResponse("If an account exists with this email, a password reset link has been sent.")
        );
    }

    @PostMapping("/forgot-password-by-details")
    public ResponseEntity<MessageResponse> forgotPasswordByDetails(
            @RequestBody @Valid ForgotPasswordByDetailsRequest request
    ) {
        String identifierType = request.identifierType().trim().toLowerCase();
        String identifier = request.identifier().trim();
        LocalDate dob;

        try {
            dob = parseDob(request.dob());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(ex.getMessage()));
        }

        User user = null;

        if ("email".equals(identifierType)) {
            user = userService.findByEmail(identifier.toLowerCase());
        } else if ("username".equals(identifierType)) {
            user = userService.findByUsername(identifier);
        } else {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("identifierType must be either email or username."));
        }

        if (user != null
                && user.getDob() != null
                && user.getDob().equals(dob)) {

            String token = userService.createPasswordResetToken(user);
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            String subject = "Reset your Quiz App password";
            String body = "Hello " + (user.getFirstName() != null ? user.getFirstName() : "") +
                    ",\n\nWe received a request to reset your password." +
                    "\nClick the link below to set a new password:" +
                    "\n" + resetUrl +
                    "\n\nIf you did not request this, you can ignore this email.";

            emailService.sendSimpleEmail(user.getEmail(), subject, body);
        }

        return ResponseEntity.ok(
                new MessageResponse("If the provided details match an account, a password reset link has been sent.")
        );
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        User user = userService.validatePasswordResetToken(token.trim());

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid or expired reset token."));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Reset token is valid.",
                "email", user.getEmail()
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request
    ) {
        String token = request.token().trim();
        String newPassword = request.newPassword().trim();

        User user = userService.validatePasswordResetToken(token);
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid or expired reset token."));
        }

        try {
            userService.updatePassword(user, newPassword);
            userService.consumePasswordResetToken(token);

            return ResponseEntity.ok(
                    new MessageResponse("Password has been reset successfully.")
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(ex.getMessage()));
        }
    }

    private String buildFullName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? null : fullName;
    }

    private LocalDate parseDob(String dob) {
        String trimmedDob = dob != null ? dob.trim() : null;
        if (trimmedDob == null || trimmedDob.isBlank()) {
            throw new IllegalArgumentException("Date of birth is required.");
        }

        try {
            return LocalDate.parse(trimmedDob);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date of birth format. Use yyyy-MM-dd.");
        }
    }
}