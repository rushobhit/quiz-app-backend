package com.quizmicroservice.dto;

public class QuestionWrapper {

    private final Integer id;
    private final String title;
    private final String option1;
    private final String option2;
    private final String option3;
    private final String option4;

    public QuestionWrapper(
            Integer id,
            String title,
            String option1,
            String option2,
            String option3,
            String option4
    ) {
        this.id = id;
        this.title = title;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption3() {
        return option3;
    }

    public String getOption4() {
        return option4;
    }
}