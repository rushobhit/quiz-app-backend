package com.quizmicroservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotUsernameRequestDto {

    @NotBlank(message = "email is required.")
    @Email(message = "email must be valid.")
    private String email;

    @NotBlank(message = "dob is required.")
    private String dob;

    public ForgotUsernameRequestDto() {
    }

    public ForgotUsernameRequestDto(String email, String dob) {
        this.email = email;
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }
}