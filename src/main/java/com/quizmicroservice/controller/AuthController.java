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

    @Value("${quiz.oauth.google.client-id:}")
    private String googleClientId;

    @Value("${quiz.oauth.google.client-secret:}")
    private String googleClientSecret;

    @Value("${quiz.oauth.github.client-id:}")
    private String githubClientId;

    @Value("${quiz.oauth.github.client-secret:}")
    private String githubClientSecret;

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

    @PostMapping("/forgot-username")
    public ResponseEntity<MessageResponse> forgotUsername(
            @RequestBody @Valid ForgotUsernameByDetailsRequest request
    ) {
        String normalizedEmail = request.email().trim().toLowerCase();
        User user = userService.findByEmail(normalizedEmail);
        if (user == null || user.getDob() == null || !user.getDob().toString().equals(request.dob().trim())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid email or date of birth."));
        }

        String username = user.getUsername();
        emailService.sendSimpleEmail(
                normalizedEmail,
                "Quiz App - Username Recovery",
                "Your username is: " + username
        );

        return ResponseEntity.ok(
                new MessageResponse("Your username is: " + username)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody @Valid ForgotPasswordByDetailsRequest request
    ) {
        String identifier = request.identifier().trim();
        String dob = request.dob().trim();
        User user = userService.findByEmailOrUsername(identifier);

        if (user == null || user.getDob() == null || !user.getDob().toString().equals(dob)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid credentials or date of birth."));
        }

        // Generate a stateless password reset token (valid for 10 minutes)
        String resetToken = jwtService.generateToken(
                java.util.Map.of("purpose", "password_reset", "email", user.getEmail()),
                user.getEmail()
        );

        return ResponseEntity.ok(java.util.Map.of(
                "message", "Details verified successfully.",
                "token", resetToken
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request
    ) {
        try {
            String token = request.token().trim();
            String username = jwtService.extractUsername(token);
            String purpose = jwtService.extractClaim(token, claims -> claims.get("purpose", String.class));

            if (!"password_reset".equals(purpose)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Invalid token purpose."));
            }

            if (jwtService.isTokenExpired(token)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Reset token has expired."));
            }

            User user = userService.findByEmail(username);
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("User not found."));
            }

            userService.updatePassword(user, request.newPassword());
            return ResponseEntity.ok(new MessageResponse("Password reset successfully."));
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid reset token: " + ex.getMessage()));
        }
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody java.util.Map<String, String> payload, HttpServletRequest httpServletRequest) {
        String code = payload.get("code");
        String redirectUri = payload.get("redirectUri");

        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Authorization code is required."));
        }

        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

            java.util.Map<String, String> tokenParams = new java.util.HashMap<>();
            tokenParams.put("code", code);
            tokenParams.put("client_id", googleClientId);
            tokenParams.put("client_secret", googleClientSecret);
            tokenParams.put("redirect_uri", redirectUri);
            tokenParams.put("grant_type", "authorization_code");

            java.util.Map<?, ?> tokenResponse = restTemplate.postForObject(
                    "https://oauth2.googleapis.com/token",
                    tokenParams,
                    java.util.Map.class
            );

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                return ResponseEntity.badRequest().body(new MessageResponse("Failed to retrieve access token from Google."));
            }

            String accessToken = (String) tokenResponse.get("access_token");

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(accessToken);
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<java.util.Map> userInfoResponse = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    java.util.Map.class
            );

            java.util.Map<?, ?> userInfo = userInfoResponse.getBody();
            if (userInfo == null || !userInfo.containsKey("email")) {
                return ResponseEntity.badRequest().body(new MessageResponse("Failed to retrieve user email from Google."));
            }

            String email = ((String) userInfo.get("email")).trim().toLowerCase();
            String givenName = (String) userInfo.get("given_name");
            String familyName = (String) userInfo.get("family_name");

            User user = userService.findByEmail(email);
            if (user == null) {
                String username = email.split("@")[0] + "_google";
                if (userService.findByUsername(username) != null) {
                    username = username + "_" + new java.util.Random().nextInt(1000);
                }

                user = userService.createStudent(
                        givenName != null ? givenName : "Google",
                        familyName != null ? familyName : "User",
                        username,
                        email,
                        java.util.UUID.randomUUID().toString(),
                        "Google Father",
                        "Google Mother",
                        "2000-01-01",
                        "Google Institute",
                        "Google Street", "", "Google City", "Google State", "100000",
                        "Google Street", "", "Google City", "Google State", "100000"
                );
            }

            String token = jwtService.generateToken(user.getEmail(), user.getRole());

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
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new MessageResponse("Google authentication error: " + ex.getMessage()));
        }
    }

    @PostMapping("/github-login")
    public ResponseEntity<?> githubLogin(@RequestBody java.util.Map<String, String> payload, HttpServletRequest httpServletRequest) {
        String code = payload.get("code");

        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Authorization code is required."));
        }

        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

            java.util.Map<String, String> tokenParams = new java.util.HashMap<>();
            tokenParams.put("code", code);
            tokenParams.put("client_id", githubClientId);
            tokenParams.put("client_secret", githubClientSecret);

            org.springframework.http.HttpHeaders tokenHeaders = new org.springframework.http.HttpHeaders();
            tokenHeaders.setAccept(java.util.List.of(org.springframework.http.MediaType.APPLICATION_JSON));
            org.springframework.http.HttpEntity<java.util.Map<String, String>> tokenRequest = new org.springframework.http.HttpEntity<>(tokenParams, tokenHeaders);

            java.util.Map<?, ?> tokenResponse = restTemplate.postForObject(
                    "https://github.com/login/oauth/access_token",
                    tokenRequest,
                    java.util.Map.class
            );

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                return ResponseEntity.badRequest().body(new MessageResponse("Failed to retrieve access token from GitHub."));
            }

            String accessToken = (String) tokenResponse.get("access_token");

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("User-Agent", "QuizApp-Backend");
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<java.util.Map> userProfileResponse = restTemplate.exchange(
                    "https://api.github.com/user",
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    java.util.Map.class
            );

            java.util.Map<?, ?> profile = userProfileResponse.getBody();
            if (profile == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Failed to retrieve profile details from GitHub."));
            }

            String githubEmail = (String) profile.get("email");
            String name = (String) profile.get("name");
            String username = (String) profile.get("login");

            if (githubEmail == null || githubEmail.isBlank()) {
                org.springframework.http.ResponseEntity<java.util.List> userEmailsResponse = restTemplate.exchange(
                        "https://api.github.com/user/emails",
                        org.springframework.http.HttpMethod.GET,
                        entity,
                        java.util.List.class
                );

                java.util.List<?> emailsList = userEmailsResponse.getBody();
                if (emailsList != null) {
                    for (Object obj : emailsList) {
                        if (obj instanceof java.util.Map) {
                            java.util.Map<?, ?> emailMap = (java.util.Map<?, ?>) obj;
                            Boolean primary = (Boolean) emailMap.get("primary");
                            Boolean verified = (Boolean) emailMap.get("verified");
                            if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                                githubEmail = (String) emailMap.get("email");
                                break;
                            }
                        }
                    }
                }
            }

            if (githubEmail == null || githubEmail.isBlank()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Failed to retrieve verified primary email from GitHub."));
            }

            String email = githubEmail.trim().toLowerCase();
            User user = userService.findByEmail(email);
            if (user == null) {
                String resolvedUsername = username.toLowerCase() + "_github";
                if (userService.findByUsername(resolvedUsername) != null) {
                    resolvedUsername = resolvedUsername + "_" + new java.util.Random().nextInt(1000);
                }

                String firstName = name != null && name.contains(" ") ? name.split(" ")[0] : (name != null ? name : "GitHub");
                String lastName = name != null && name.contains(" ") ? name.substring(name.indexOf(" ") + 1) : "User";

                user = userService.createStudent(
                        firstName,
                        lastName,
                        resolvedUsername,
                        email,
                        java.util.UUID.randomUUID().toString(),
                        "GitHub Father",
                        "GitHub Mother",
                        "2000-01-01",
                        "GitHub Institute",
                        "GitHub Street", "", "GitHub City", "GitHub State", "100000",
                        "GitHub Street", "", "GitHub City", "GitHub State", "100000"
                );
            }

            String token = jwtService.generateToken(user.getEmail(), user.getRole());

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
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new MessageResponse("GitHub authentication error: " + ex.getMessage()));
        }
    }

    @PostMapping("/social-login-mock")
    public ResponseEntity<?> socialLoginMock(@RequestBody java.util.Map<String, String> payload, HttpServletRequest httpServletRequest) {
        String email = payload.get("email");
        String name = payload.get("name");
        String provider = payload.get("provider");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email is required."));
        }

        email = email.trim().toLowerCase();
        User user = userService.findByEmail(email);

        if (user == null) {
            String username = email.split("@")[0] + "_" + (provider != null ? provider.toLowerCase() : "mock");
            if (userService.findByUsername(username) != null) {
                username = username + "_" + new java.util.Random().nextInt(1000);
            }

            String firstName = name != null && name.contains(" ") ? name.split(" ")[0] : (name != null ? name : "Mock");
            String lastName = name != null && name.contains(" ") ? name.substring(name.indexOf(" ") + 1) : "User";

            user = userService.createStudent(
                    firstName,
                    lastName,
                    username,
                    email,
                    java.util.UUID.randomUUID().toString(),
                    "Mock Father",
                    "Mock Mother",
                    "2000-01-01",
                    "Mock Institute",
                    "Mock Street", "", "Mock City", "Mock State", "100000",
                    "Mock Street", "", "Mock City", "Mock State", "100000"
            );
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

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

    private String buildFullName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? null : fullName;
    }
}