package com.quizmicroservice.dto;

public class QuizMetaResponse {

    private String subject;
    private String difficulty;
    private long totalQuestions;
    private int totalTimeMinutes;

    public QuizMetaResponse(
            String subject,
            String difficulty,
            long totalQuestions,
            int totalTimeMinutes
    ) {
        this.subject = subject;
        this.difficulty = difficulty;
        this.totalQuestions = totalQuestions;
        this.totalTimeMinutes = totalTimeMinutes;
    }

    public String getSubject() {
        return subject;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public long getTotalQuestions() {
        return totalQuestions;
    }

    public int getTotalTimeMinutes() {
        return totalTimeMinutes;
    }
}