package com.quizmicroservice.dto;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequestDto {

    @NotBlank(message = "identifierType is required.")
    private String identifierType;

    @NotBlank(message = "identifier is required.")
    private String identifier;

    @NotBlank(message = "dob is required.")
    private String dob;

    public ForgotPasswordRequestDto() {
    }

    public ForgotPasswordRequestDto(String identifierType, String identifier, String dob) {
        this.identifierType = identifierType;
        this.identifier = identifier;
        this.dob = dob;
    }

    public String getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(String identifierType) {
        this.identifierType = identifierType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }
}