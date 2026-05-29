package com.quizmicroservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "results")
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // basic student info
    private String studentName;

    private String email;

    // which quiz this result belongs to
    private String quizTitle;

    // scoring
    private Integer score;

    private Integer total;

    private Double percentage;

    // when the quiz was submitted
    private LocalDateTime submittedAt;

    // constructors
    public Result() {
    }

    public Result(String studentName,
                  String email,
                  String quizTitle,
                  Integer score,
                  Integer total,
                  Double percentage,
                  LocalDateTime submittedAt) {
        this.studentName = studentName;
        this.email = email;
        this.quizTitle = quizTitle;
        this.score = score;
        this.total = total;
        this.percentage = percentage;
        this.submittedAt = submittedAt;
    }

    // getters and setters

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
        this.studentName = studentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
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

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}