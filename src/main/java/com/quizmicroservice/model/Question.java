package com.quizmicroservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(nullable = false, length = 200)
    private String option1;

    @Column(nullable = false, length = 200)
    private String option2;

    @Column(nullable = false, length = 200)
    private String option3;

    @Column(nullable = false, length = 200)
    private String option4;

    @Column(name = "correct_option", nullable = false)
    private Integer correctOption;

    public Question() {
    }

    public Question(Quiz quiz,
                    String question,
                    String option1,
                    String option2,
                    String option3,
                    String option4,
                    Integer correctOption) {
        this.quiz = quiz;
        this.question = question != null ? question.trim() : null;
        this.option1 = option1 != null ? option1.trim() : null;
        this.option2 = option2 != null ? option2.trim() : null;
        this.option3 = option3 != null ? option3.trim() : null;
        this.option4 = option4 != null ? option4.trim() : null;
        this.correctOption = correctOption;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question != null ? question.trim() : null;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1 != null ? option1.trim() : null;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2 != null ? option2.trim() : null;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3 != null ? option3.trim() : null;
    }

    public String getOption4() {
        return option4;
    }

    public void setOption4(String option4) {
        this.option4 = option4 != null ? option4.trim() : null;
    }

    public Integer getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(Integer correctOption) {
        this.correctOption = correctOption;
    }
}