package com.quizmicroservice.controller;

import com.quizmicroservice.dto.ForgotPasswordRequestDto;
import com.quizmicroservice.dto.ResetPasswordRequestDto;
import com.quizmicroservice.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/password")
@CrossOrigin(origins = "http://localhost:5173")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequestDto request
    ) {
        try {
            passwordResetService.sendResetLinkByDetails(
                    request.getIdentifierType(),
                    request.getIdentifier(),
                    request.getDob()
            );
        } catch (Exception ignored) {
            // Generic response to avoid account enumeration
        }

        return ResponseEntity.ok(
                Map.of("message", "If the details match our records, a password reset link has been sent.")
        );
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody @Valid ResetPasswordRequestDto request
    ) {
        passwordResetService.resetPassword(
                request.getToken(),
                request.getNewPassword()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Password reset successfully."));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String token) {
        boolean valid = passwordResetService.isValidToken(token == null ? null : token.trim());

        if (!valid) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "valid", false,
                            "message", "Invalid or expired reset token."
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "Reset token is valid."
        ));
    }
}