package com.quizmicroservice.dto;

public record QuestionWrapper(
        Integer id,
        String question,
        String option1,
        String option2,
        String option3,
        String option4
) {
    public QuestionWrapper {
        question = question != null ? question.trim() : null;
        option1 = option1 != null ? option1.trim() : null;
        option2 = option2 != null ? option2.trim() : null;
        option3 = option3 != null ? option3.trim() : null;
        option4 = option4 != null ? option4.trim() : null;
    }
}