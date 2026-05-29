package com.quizmicroservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateQuizRequest {

    @NotBlank(message = "Category is required.")
    @Size(max = 100, message = "Category must not exceed 100 characters.")
    private String category;

    @Min(value = 1, message = "Number of questions must be at least 1.")
    @Max(value = 20, message = "Number of questions must not exceed 20.")
    private int numQ;

    @NotBlank(message = "Title is required.")
    @Size(max = 150, message = "Title must not exceed 150 characters.")
    private String title;

    public CreateQuizRequest() {
    }

    public CreateQuizRequest(String category, int numQ, String title) {
        this.category = category;
        this.numQ = numQ;
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getNumQ() {
        return numQ;
    }

    public void setNumQ(int numQ) {
        this.numQ = numQ;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}