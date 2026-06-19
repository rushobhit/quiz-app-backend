package com.quizmicroservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "results")
public class Result {
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "quiz_id")
	private Quiz quiz;
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_name", length = 100)
    private String studentName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "subject", nullable = false, length = 100)
    private String subject;

    @Column(name = "difficulty", nullable = false, length = 50)
    private String difficulty;

    @Column(name = "quiz_title", nullable = false, length = 150)
    private String quizTitle;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "total", nullable = false)
    private Integer total;

    @Column(name = "percentage", nullable = false)
    private Double percentage;

    @Column(name = "time_taken", length = 20)
    private String timeTaken;

    @Column(name = "attempted_questions")
    private Integer attemptedQuestions;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    public Result() {
    }

    public Result(String studentName,
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
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
}