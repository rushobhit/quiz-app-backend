package com.quizmicroservice.repository;

import com.quizmicroservice.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Integer> {

    Optional<Quiz> findBySubjectAndDifficulty(String subject, String difficulty);

    List<Quiz> findAllBySubject(String subject);

    List<Quiz> findAllBySubjectAndDifficulty(String subject, String difficulty);

    boolean existsBySubjectAndDifficulty(String subject, String difficulty);
}