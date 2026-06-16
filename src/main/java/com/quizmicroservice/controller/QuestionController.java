package com.quizmicroservice.controller;

import com.quizmicroservice.dto.QuestionRequest;
import com.quizmicroservice.model.Question;
import com.quizmicroservice.service.QuestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
@Validated
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<List<Question>> getQuestions(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String difficulty
    ) {
        String subj = subject != null ? subject.trim() : null;
        String diff = difficulty != null ? difficulty.trim() : null;

        boolean hasSubject = subj != null && !subj.isBlank();
        boolean hasDifficulty = diff != null && !diff.isBlank();

        if (!hasSubject && !hasDifficulty) {
            return ResponseEntity.ok(questionService.getAllQuestions());
        }

        if (hasDifficulty && !hasSubject) {
            return ResponseEntity.badRequest().build();
        }

        if (hasSubject && !hasDifficulty) {
            return ResponseEntity.ok(questionService.getQuestionsBySubject(subj));
        }

        return ResponseEntity.ok(
                questionService.getQuestionsBySubjectAndDifficulty(subj, diff)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<String> addQuestion(
            @Valid @RequestBody QuestionRequest request
    ) {
        questionService.addQuestion(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Question added successfully.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuestion(
            @PathVariable @Positive(message = "Question id must be positive.") Integer id
    ) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok("Question deleted successfully.");
    }
}