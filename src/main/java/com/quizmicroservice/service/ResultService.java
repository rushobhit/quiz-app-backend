package com.quizmicroservice.service;

import com.quizmicroservice.dto.StudentResultDto;
import com.quizmicroservice.model.Result;
import com.quizmicroservice.repository.ResultRepository;


import com.quizmicroservice.dto.UpdateResultRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ResultService {

    private final ResultRepository resultRepository;
    
    public byte[] exportResultsCsv() {

        StringBuilder csv = new StringBuilder();

        csv.append(
            "Student Name,Email,Subject,Difficulty,Quiz Title,Score,Total,Attempted Questions,Percentage,Time Taken,Submitted At\n"
        );

        resultRepository.findAll().forEach(result -> {
            csv.append("\"").append(result.getStudentName()).append("\",");
            csv.append("\"").append(result.getEmail()).append("\",");
            csv.append("\"").append(result.getSubject()).append("\",");
            csv.append("\"").append(result.getDifficulty()).append("\",");
            csv.append("\"").append(result.getQuizTitle()).append("\",");
            csv.append(result.getScore()).append(",");
            csv.append(result.getTotal()).append(",");
            csv.append(result.getAttemptedQuestions()).append(",");
            csv.append(result.getPercentage()).append(",");
            csv.append("\"").append(result.getTimeTaken()).append("\",");
            csv.append(result.getSubmittedAt()).append("\n");
        });

        return csv.toString().getBytes();
    }
    
    public byte[] exportResultsExcel() throws IOException {

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Results");

        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("Student Name");
        header.createCell(1).setCellValue("Email");
        header.createCell(2).setCellValue("Subject");
        header.createCell(3).setCellValue("Difficulty");
        header.createCell(4).setCellValue("Quiz Title");
        header.createCell(5).setCellValue("Score");
        header.createCell(6).setCellValue("Total");
        header.createCell(7).setCellValue("Attempted");
        header.createCell(8).setCellValue("Percentage");
        header.createCell(9).setCellValue("Time Taken");
        header.createCell(10).setCellValue("Submitted At");

        int rowNum = 1;

        for (Result result : resultRepository.findAll()) {

            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(
                    result.getStudentName() == null ? "" : result.getStudentName()
            );

            row.createCell(1).setCellValue(
                    result.getEmail() == null ? "" : result.getEmail()
            );

            row.createCell(2).setCellValue(
                    result.getSubject() == null ? "" : result.getSubject()
            );

            row.createCell(3).setCellValue(
                    result.getDifficulty() == null ? "" : result.getDifficulty()
            );

            row.createCell(4).setCellValue(
                    result.getQuizTitle() == null ? "" : result.getQuizTitle()
            );

            row.createCell(5).setCellValue(
                    result.getScore() == null ? 0 : result.getScore()
            );

            row.createCell(6).setCellValue(
                    result.getTotal() == null ? 0 : result.getTotal()
            );

            row.createCell(7).setCellValue(
                    result.getAttemptedQuestions() == null ? 0 : result.getAttemptedQuestions()
            );

            row.createCell(8).setCellValue(
                    result.getPercentage() == null ? 0 : result.getPercentage()
            );

            row.createCell(9).setCellValue(
                    result.getTimeTaken() == null ? "" : result.getTimeTaken()
            );

            row.createCell(10).setCellValue(
                    result.getSubmittedAt() == null
                            ? ""
                            : result.getSubmittedAt().toString()
            );
        }

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        workbook.write(outputStream);

        workbook.close();

        return outputStream.toByteArray();
    }
    
    public ResultService(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public List<StudentResultDto> getAllResultsForAdmin() {
        return resultRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }
    public Page<StudentResultDto> getResultsForAdmin(
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return resultRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    public Result saveResult(Result result) {
        return resultRepository.save(result);
    }
    
    public void updateResult(
            Long id,
            UpdateResultRequest request
    ) {
        Result result = resultRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Result not found"
                        )
                );

        result.setScore(request.getScore());
        result.setAttemptedQuestions(
                request.getAttemptedQuestions()
        );
        result.setPercentage(
                request.getPercentage()
        );

        resultRepository.save(result);
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