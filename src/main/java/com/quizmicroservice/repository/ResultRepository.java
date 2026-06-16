package com.quizmicroservice.repository;

import com.quizmicroservice.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    List<Result> findByEmail(String email);

    List<Result> findByQuizTitle(String quizTitle);

    List<Result> findBySubject(String subject);

    List<Result> findByDifficulty(String difficulty);
}