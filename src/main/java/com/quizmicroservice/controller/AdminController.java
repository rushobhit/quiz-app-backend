package com.quizmicroservice.controller;

import com.quizmicroservice.dto.QuestionRequest;
import com.quizmicroservice.dto.StudentResultDto;
import com.quizmicroservice.service.QuestionService;
import com.quizmicroservice.service.ResultService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final QuestionService questionService;
    private final ResultService resultService;

    public AdminController(QuestionService questionService,
                           ResultService resultService) {
        this.questionService = questionService;
        this.resultService = resultService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/questions")
    public ResponseEntity<List<QuestionRequest>> getAllQuestions() {
        List<QuestionRequest> questions = questionService.getAllForAdmin();
        return ResponseEntity.ok(questions);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/questions")
    public ResponseEntity<Void> createQuestion(
            @Valid @RequestBody QuestionRequest request
    ) {
        questionService.addQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/questions/{id}")
    public ResponseEntity<Void> updateQuestion(
            @PathVariable Integer id,
            @Valid @RequestBody QuestionRequest request
    ) {
        questionService.updateQuestion(id, request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Integer id
    ) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/student-results")
    public ResponseEntity<List<StudentResultDto>> getStudentResults() {
        List<StudentResultDto> results = resultService.getAllResultsForAdmin();
        return ResponseEntity.ok(results);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/student-results/{id}")
    public ResponseEntity<Void> deleteStudentResult(
            @PathVariable Long id
    ) {
        resultService.deleteResultById(id);
        return ResponseEntity.noContent().build();
    }
}