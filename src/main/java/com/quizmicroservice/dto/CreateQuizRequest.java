package com.quizmicroservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateQuizRequest {

    @NotBlank(message = "Subject is required.")
    @Size(max = 100, message = "Subject must not exceed 100 characters.")
    private String subject;

    @NotBlank(message = "Difficulty is required.")
    @Size(max = 50, message = "Difficulty must not exceed 50 characters.")
    private String difficulty;

    public CreateQuizRequest() {
    }

    public CreateQuizRequest(String subject, String difficulty) {
        this.setSubject(subject);
        this.setDifficulty(difficulty);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject != null ? subject.trim() : null;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty != null ? difficulty.trim() : null;
    }
}