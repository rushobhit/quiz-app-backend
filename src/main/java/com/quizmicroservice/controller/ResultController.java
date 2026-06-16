package com.quizmicroservice.controller;

import com.quizmicroservice.dto.StudentResultDto;
import com.quizmicroservice.model.Result;
import com.quizmicroservice.service.ResultService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result saveResult(@RequestBody Result result) {
        return resultService.saveResult(result);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<StudentResultDto> getAllResultsForAdmin() {
        return resultService.getAllResultsForAdmin();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResult(@PathVariable Long id) {
        resultService.deleteResultById(id);
    }
}