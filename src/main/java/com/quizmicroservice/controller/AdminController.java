package com.quizmicroservice.controller;

import com.quizmicroservice.dto.ApiResponse;
import com.quizmicroservice.dto.QuestionRequest;
import com.quizmicroservice.service.AuthActivityService;
import com.quizmicroservice.service.QuestionService;
import com.quizmicroservice.service.ResultService;
import com.quizmicroservice.service.UserService;
import com.quizmicroservice.dto.PageResponse;

import com.quizmicroservice.dto.UpdateResultRequest;
import com.quizmicroservice.dto.UpdateStudentRequest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final UserService userService;
	private final AuthActivityService authActivityService;
    private final QuestionService questionService;
    private final ResultService resultService;

    public AdminController(
            QuestionService questionService,
            ResultService resultService,
            UserService userService,
            AuthActivityService authActivityService
    ) {
        this.questionService = questionService;
        this.resultService = resultService;
        this.userService = userService;
        this.authActivityService = authActivityService;
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/student-results/export-excel")
    public ResponseEntity<ByteArrayResource> exportResultsExcel()
            throws IOException {

        byte[] data = resultService.exportResultsExcel();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=student-results.xlsx"
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(new ByteArrayResource(data));
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/student-results/export")
    public ResponseEntity<ByteArrayResource> exportResultsCsv() {

        byte[] data = resultService.exportResultsCsv();

        ByteArrayResource resource =
                new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=student-results.csv"
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(data.length)
                .body(resource);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/student-results/{id}")
    public ResponseEntity<ApiResponse<Void>> updateStudentResult(
            @PathVariable Long id,
            @RequestBody UpdateResultRequest request
    ) {
        resultService.updateResult(id, request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Result updated successfully.",
                        null
                )
        );
        }
    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/students/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            @PathVariable Long id
    ) {
        userService.deleteStudent(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Student deleted successfully.",
                        null
                )
        );
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<PageResponse<?>>> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Questions fetched successfully.",
                        new PageResponse<>(
                                questionService.getQuestionsForAdmin(
                                        page,
                                        size,
                                        search
                                )
                        )
                )
        );
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/students/{id}")
    public ResponseEntity<ApiResponse<Void>> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentRequest request
    ) {
        userService.updateStudent(id, request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Student updated successfully.",
                        null
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/questions")
    public ResponseEntity<ApiResponse<Void>> createQuestion(
            @Valid @RequestBody QuestionRequest request
    ) {
        questionService.addQuestion(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                true,
                                "Question created successfully.",
                                null
                        )
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/questions/{id}")
    public ResponseEntity<ApiResponse<Void>> updateQuestion(
            @PathVariable Integer id,
            @Valid @RequestBody QuestionRequest request
    ) {
        questionService.updateQuestion(id, request);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Question updated successfully.",
                        null
                )
        );
        }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/questions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable Integer id
    ) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Question deleted successfully.",
                        null
                )
        );
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<PageResponse<?>>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Students fetched successfully.",
                        new PageResponse<>(
                                userService.getStudents(page, size)
                        )
                )
        );
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<PageResponse<?>>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Logs fetched successfully.",
                        new PageResponse<>(
                                authActivityService.getLogs(page, size)
                        )
                )
        );
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/student-results")
    public ResponseEntity<ApiResponse<PageResponse<?>>> getStudentResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Results fetched successfully.",
                        new PageResponse<>(
                                resultService.getResultsForAdmin(page, size)
                        )
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/student-results/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudentResult(
            @PathVariable Long id
    ) {
        resultService.deleteResultById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Result deleted successfully.",
                        null
                )
        );
   }
}