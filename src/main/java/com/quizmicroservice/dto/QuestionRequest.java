package com.quizmicroservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.stream.Collectors;

public class QuestionRequest {

    private Integer id;

    @NotBlank(message = "Subject is required.")
    @Size(max = 100, message = "Subject must not exceed 100 characters.")
    private String subject;

    @NotBlank(message = "Difficulty is required.")
    @Size(max = 50, message = "Difficulty must not exceed 50 characters.")
    private String difficulty;

    @NotBlank(message = "Question text is required.")
    @Size(max = 500, message = "Question text must not exceed 500 characters.")
    private String question;

    @NotEmpty(message = "Options are required.")
    @Size(min = 4, max = 4, message = "Exactly 4 options are required.")
    private List<
            @NotBlank(message = "Option value cannot be blank.")
            @Size(max = 200, message = "Each option must not exceed 200 characters.")
            String> options;

    @NotNull(message = "Correct option is required.")
    @Min(value = 1, message = "Correct option must be between 1 and 4.")
    @Max(value = 4, message = "Correct option must be between 1 and 4.")
    private Integer correctOption;

    public QuestionRequest() {
    }

    public QuestionRequest(Integer id,
                           String subject,
                           String difficulty,
                           String question,
                           List<String> options,
                           Integer correctOption) {
        this.id = id;
        this.setSubject(subject);
        this.setDifficulty(difficulty);
        this.setQuestion(question);
        this.setOptions(options);
        this.correctOption = correctOption;
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
        this.difficulty = difficulty != null ? difficulty.trim().toUpperCase() : null;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question != null ? question.trim() : null;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options == null
                ? null
                : options.stream()
                .map(opt -> opt != null ? opt.trim() : null)
                .collect(Collectors.toList());
    }

    public Integer getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(Integer correctOption) {
        this.correctOption = correctOption;
    }

    public boolean hasValidCorrectOption() {
        if (options == null || options.size() != 4 || correctOption == null) {
            return false;
        }

        if (correctOption < 1 || correctOption > 4) {
            return false;
        }

        return options.stream().allMatch(opt -> opt != null && !opt.isBlank());
    }
}