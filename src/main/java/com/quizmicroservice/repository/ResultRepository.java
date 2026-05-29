package com.quizmicroservice.repository;

import com.quizmicroservice.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    // Optional helper methods if you need them later

    // All results for a specific student email
    List<Result> findByEmail(String email);

    // All results for a specific quiz title
    List<Result> findByQuizTitle(String quizTitle);
}