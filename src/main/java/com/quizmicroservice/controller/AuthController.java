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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final EmailService emailService;
    private final OtpService otpService;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthActivityService authActivityService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public AuthController(
            EmailService emailService,
            OtpService otpService,
            UserService userService,
            JwtService jwtService,
            AuthActivityService authActivityService
    ) {
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

        if ("BLOCKED".equalsIgnoreCase(user.getStatus())) {

            authActivityService.logFailedLogin(
                    user.getEmail(),
                    httpServletRequest,
                    "Login attempt on blocked account."
            );

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account has been blocked. Please contact administrator."
            );
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

    private String buildFullName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? null : fullName;
    }
}