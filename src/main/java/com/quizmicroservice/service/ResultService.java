package com.quizmicroservice.service;

import com.quizmicroservice.dto.StudentResultDto;
import com.quizmicroservice.model.Result;
import com.quizmicroservice.repository.ResultRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ResultService {

    private final ResultRepository resultRepository;

    public ResultService(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public List<StudentResultDto> getAllResultsForAdmin() {
        return resultRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public Result saveResult(Result result) {
        return resultRepository.save(result);
    }

    public void deleteResultById(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Result id is required.");
        }

        if (!resultRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Student result not found with id: " + id
            );
        }

        resultRepository.deleteById(id);
    }

    private StudentResultDto mapToDto(Result result) {
        StudentResultDto dto = new StudentResultDto();
        dto.setTimeTaken(result.getTimeTaken());
        dto.setAttemptedQuestions(result.getAttemptedQuestions());
        dto.setId(result.getId());
        dto.setStudentName(result.getStudentName());
        dto.setEmail(result.getEmail());
        dto.setSubject(result.getSubject());
        dto.setDifficulty(result.getDifficulty());
        dto.setQuizTitle(result.getQuizTitle());
        dto.setScore(result.getScore());
        dto.setTotal(result.getTotal());
        dto.setPercentage(result.getPercentage());
        dto.setSubmittedAt(result.getSubmittedAt());
        return dto;
    }
}