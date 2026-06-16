package com.quizmicroservice.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "quiz",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subject", "difficulty"})
)
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false, length = 50)
    private String difficulty;

    @OneToMany(
            mappedBy = "quiz",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Question> questions = new ArrayList<>();

    public Quiz() {
    }

    public Quiz(String subject, String difficulty) {
        this.subject = subject != null ? subject.trim() : null;
        this.difficulty = difficulty != null ? difficulty.trim() : null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = new ArrayList<>();
        if (questions != null) {
            for (Question question : questions) {
                addQuestion(question);
            }
        }
    }

    public void addQuestion(Question question) {
        if (question == null) {
            return;
        }
        if (questions == null) {
            questions = new ArrayList<>();
        }
        questions.add(question);
        question.setQuiz(this);
    }

    public void removeQuestion(Question question) {
        if (question == null || questions == null) {
            return;
        }
        questions.remove(question);
        question.setQuiz(null);
    }
}