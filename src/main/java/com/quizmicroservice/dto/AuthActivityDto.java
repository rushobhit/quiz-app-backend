package com.quizmicroservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthActivityDto {

    private Long userId;

    private String fullName;

    @Email(message = "Please provide a valid email.")
    private String email;

    @NotBlank(message = "Role is required.")
    private String role;

    @NotBlank(message = "Action is required.")
    private String action;

    private String message;

    private String deviceInfo;

    public AuthActivityDto() {
    }

    public AuthActivityDto(Long userId,
                           String fullName,
                           String email,
                           String role,
                           String action,
                           String message,
                           String deviceInfo) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.action = action;
        this.message = message;
        this.deviceInfo = deviceInfo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}