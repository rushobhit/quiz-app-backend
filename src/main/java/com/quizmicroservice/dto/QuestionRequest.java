package com.quizmicroservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class QuestionRequest {

    @NotBlank(message = "Question text is required.")
    @Size(max = 500, message = "Question text must not exceed 500 characters.")
    private String question;

    @NotEmpty(message = "Options are required.")
    @Size(min = 4, max = 4, message = "Exactly 4 options are required.")
    private List<
            @NotBlank(message = "Option value cannot be blank.")
            @Size(max = 200, message = "Each option must not exceed 200 characters.")
            String> options;

    @NotBlank(message = "Correct answer is required.")
    @Size(max = 200, message = "Correct answer must not exceed 200 characters.")
    private String correctAnswer;

    @NotBlank(message = "Category is required.")
    @Size(max = 100, message = "Category must not exceed 100 characters.")
    private String category;

    public QuestionRequest() {
    }

    public QuestionRequest(String question, List<String> options, String correctAnswer, String category) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.category = category;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean hasValidCorrectAnswer() {
        if (options == null || correctAnswer == null) {
            return false;
        }

        return options.stream()
                .filter(option -> option != null && !option.trim().isEmpty())
                .anyMatch(option -> option.trim().equals(correctAnswer.trim()));
    }
}