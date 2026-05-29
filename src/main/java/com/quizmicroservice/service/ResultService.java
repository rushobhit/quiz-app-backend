package com.quizmicroservice.service;

import com.quizmicroservice.dto.StudentResultDto;
import com.quizmicroservice.model.Result;
import com.quizmicroservice.repository.ResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResultService {

    private final ResultRepository resultRepository;

    public ResultService(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    // for admin dashboard
    public List<StudentResultDto> getAllResultsForAdmin() {
        List<Result> results = resultRepository.findAll();
        return results.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ✅ new method: used by QuizService to store a result
    public Result saveResult(Result result) {
        return resultRepository.save(result);
    }

    private StudentResultDto mapToDto(Result result) {
        StudentResultDto dto = new StudentResultDto();
        dto.setId(result.getId());
        dto.setStudentName(result.getStudentName());
        dto.setEmail(result.getEmail());
        dto.setQuizTitle(result.getQuizTitle());
        dto.setScore(result.getScore());
        dto.setTotal(result.getTotal());
        dto.setPercentage(result.getPercentage());
        dto.setSubmittedAt(result.getSubmittedAt());
        return dto;
    }
}