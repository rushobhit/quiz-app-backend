package com.quizmicroservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequestDto {

    @NotBlank(message = "token is required.")
    private String token;

    @NotBlank(message = "newPassword is required.")
    @Size(min = 8, message = "newPassword must be at least 8 characters long.")
    private String newPassword;

    public ResetPasswordRequestDto() {
    }

    public ResetPasswordRequestDto(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}