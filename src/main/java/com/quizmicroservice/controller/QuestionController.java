package com.quizmicroservice.controller;

import com.quizmicroservice.dto.QuestionRequest;
import com.quizmicroservice.model.Question;
import com.quizmicroservice.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(required = false) String category
    ) {
        if (category == null || category.trim().isEmpty()) {
            return ResponseEntity.ok(questionService.getAllQuestions());
        }

        return ResponseEntity.ok(
                questionService.getQuestionsByCategory(category.trim())
        );
    }

    @PostMapping
    public ResponseEntity<String> addQuestion(@Valid @RequestBody QuestionRequest request) {
        questionService.addQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Question added successfully.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable int id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok("Question deleted successfully.");
    }
}