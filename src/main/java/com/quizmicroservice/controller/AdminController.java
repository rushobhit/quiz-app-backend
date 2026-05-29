package com.quizmicroservice.controller;

import com.quizmicroservice.dto.QuestionRequest;
import com.quizmicroservice.dto.StudentResultDto;
import com.quizmicroservice.service.QuestionService;
import com.quizmicroservice.service.ResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173") // adjust if needed
public class AdminController {

    private final QuestionService questionService;
    private final ResultService resultService;

    public AdminController(QuestionService questionService,
                           ResultService resultService) {
        this.questionService = questionService;
        this.resultService = resultService;
    }

    // GET /api/admin/questions
    @GetMapping("/questions")
    public ResponseEntity<List<QuestionRequest>> getAllQuestions() {
        List<QuestionRequest> questions = questionService.getAllForAdmin();
        return ResponseEntity.ok(questions);
    }

    // PUT /api/admin/questions/{id}
    @PutMapping("/questions/{id}")
    public ResponseEntity<Void> updateQuestion(
            @PathVariable Integer id,
            @RequestBody QuestionRequest request
    ) {
        questionService.updateQuestion(id, request);
        return ResponseEntity.noContent().build();
    }

    // GET /api/admin/student-results
    @GetMapping("/student-results")
    public ResponseEntity<List<StudentResultDto>> getStudentResults() {
        List<StudentResultDto> results = resultService.getAllResultsForAdmin();
        return ResponseEntity.ok(results);
    }
}