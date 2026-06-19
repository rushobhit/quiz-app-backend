package com.quizmicroservice.controller;

import com.quizmicroservice.dto.ApiResponse;
import com.quizmicroservice.dto.CreateQuizRequest;
import com.quizmicroservice.dto.QuestionWrapper;
import com.quizmicroservice.dto.QuizMetaResponse;
import com.quizmicroservice.dto.UserResponse;
import com.quizmicroservice.service.QuestionService;
import com.quizmicroservice.service.QuizService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

	private final QuestionService questionService;
    private final QuizService quizService;
    
    public QuizController(
            QuizService quizService,
            QuestionService questionService
    ) {
        this.quizService = quizService;
        this.questionService = questionService;
    }
    
    public record CreateQuizResponse(
            Integer id,
            String subject,
            String difficulty
    ) {}

    public record QuizSubmissionRequest(
            @NotEmpty(message = "Responses cannot be empty.")
            List<@Valid UserResponse> responses,

            String timeTaken,
            String quizTitle,

            @NotNull(message = "Attempted questions is required.")
            @Positive(message = "Attempted questions must be positive.")
            Integer attemptedQuestions,

            @NotNull(message = "Total questions is required.")
            @Positive(message = "Total questions must be positive.")
            Integer totalQuestions,

            Integer reviewedQuestions,
            String subject,
            String difficulty
    ) {}

    public record QuizSubmissionResponse(
            Integer quizId,
            Integer score,
            Integer attemptedQuestions,
            Integer totalQuestions,
            Integer reviewedQuestions,
            String timeTaken,
            String quizTitle,
            String subject,
            String difficulty
    ) {}

    @PostMapping
    public ResponseEntity<CreateQuizResponse> createQuiz(
            @Valid @RequestBody CreateQuizRequest request
    ) {
        String subject = request.getSubject().trim();
        String difficulty = request.getDifficulty().trim().toUpperCase();

        Integer quizId = quizService.createQuiz(subject, difficulty);

        CreateQuizResponse response = new CreateQuizResponse(
                quizId,
                subject,
                difficulty
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(
            @PathVariable @Positive(message = "Quiz id must be positive.") Integer id
    ) {
        return ResponseEntity.ok(quizService.getQuizQuestions(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<QuizSubmissionResponse> submitQuiz(
            @PathVariable @Positive(message = "Quiz id must be positive.") Integer id,
            @Valid @RequestBody QuizSubmissionRequest submission
    ) {
        int score = quizService.calculateScore(id, submission.responses(), submission.timeTaken());

        QuizSubmissionResponse response = new QuizSubmissionResponse(
                id,
                score,
                submission.attemptedQuestions(),
                submission.totalQuestions(),
                submission.reviewedQuestions(),
                submission.timeTaken(),
                submission.quizTitle(),
                submission.subject(),
                submission.difficulty()
        );

        return ResponseEntity.ok(response);
    }

@GetMapping("/meta")
public ResponseEntity<ApiResponse<QuizMetaResponse>> getQuizMeta(
        @RequestParam String subject,
        @RequestParam String difficulty
) {
    return ResponseEntity.ok(
            new ApiResponse<>(
                    true,
                    "Quiz metadata fetched successfully.",
                    questionService.getQuizMeta(
                            subject,
                            difficulty
                    )
            )
    );
}
}