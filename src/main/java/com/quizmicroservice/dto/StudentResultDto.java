package com.quizmicroservice.dto;

import java.time.LocalDateTime;

public class StudentResultDto {

    private Long id;
    private String studentName;
    private String email;
    private String subject;
    private String difficulty;
    private String quizTitle;
    private Integer score;
    private Integer total;
    private Double percentage;
    private String timeTaken;
    private Integer attemptedQuestions;
    private LocalDateTime submittedAt;

    public StudentResultDto() {
    }

    public StudentResultDto(Long id,
                            String studentName,
                            String email,
                            String subject,
                            String difficulty,
                            String quizTitle,
                            Integer score,
                            Integer total,
                            Double percentage,
                            String timeTaken,
                            Integer attemptedQuestions,
                            LocalDateTime submittedAt) {
        this.id = id;
        this.studentName = studentName != null ? studentName.trim() : null;
        this.email = email != null ? email.trim().toLowerCase() : null;
        this.subject = subject != null ? subject.trim() : null;
        this.difficulty = difficulty != null ? difficulty.trim() : null;
        this.quizTitle = quizTitle != null ? quizTitle.trim() : null;
        this.score = score;
        this.total = total;
        this.percentage = percentage;
        this.timeTaken = timeTaken != null ? timeTaken.trim() : null;
        this.attemptedQuestions = attemptedQuestions;
        this.submittedAt = submittedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName != null ? studentName.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
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

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle != null ? quizTitle.trim() : null;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public String getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(String timeTaken) {
        this.timeTaken = timeTaken != null ? timeTaken.trim() : null;
    }

    public Integer getAttemptedQuestions() {
        return attemptedQuestions;
    }

    public void setAttemptedQuestions(Integer attemptedQuestions) {
        this.attemptedQuestions = attemptedQuestions;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}