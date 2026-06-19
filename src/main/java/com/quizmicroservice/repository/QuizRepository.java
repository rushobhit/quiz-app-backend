package com.quizmicroservice.repository;

import com.quizmicroservice.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Integer> {

    Optional<Quiz> findBySubjectAndDifficulty(String subject, String difficulty);
}