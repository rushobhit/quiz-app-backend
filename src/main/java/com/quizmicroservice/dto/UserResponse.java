package com.quizmicroservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UserResponse {

    @NotNull(message = "Question id is required.")
    private Integer id;

    @Min(value = 1, message = "Selected option must be between 1 and 4.")
    @Max(value = 4, message = "Selected option must be between 1 and 4.")
    private Integer selectedOption;

    public UserResponse() {
    }

    public UserResponse(Integer id, Integer selectedOption) {
        this.id = id;
        this.selectedOption = selectedOption;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(Integer selectedOption) {
        this.selectedOption = selectedOption;
    }
}