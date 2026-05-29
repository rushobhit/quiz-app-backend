package com.quizmicroservice.controller;

import com.quizmicroservice.dto.CreateQuizRequest;
import com.quizmicroservice.dto.QuestionWrapper;
import com.quizmicroservice.dto.UserResponse;
import com.quizmicroservice.service.QuizService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quizzes")
@Validated
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    public ResponseEntity<String> createQuiz(@Valid @RequestBody CreateQuizRequest request) {
        quizService.createQuiz(
                request.getCategory().trim(),
                request.getNumQ(),
                request.getTitle().trim()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Quiz created successfully.");
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(
            @PathVariable @Positive(message = "Quiz id must be positive.") Integer id
    ) {
        return ResponseEntity.ok(quizService.getQuizQuestions(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Integer> submitQuiz(
            @PathVariable @Positive(message = "Quiz id must be positive.") Integer id,
            @RequestBody @NotEmpty(message = "Responses cannot be empty.") List<@Valid UserResponse> responses
    ) {
        return ResponseEntity.ok(quizService.calculateScore(id, responses));
    }
}