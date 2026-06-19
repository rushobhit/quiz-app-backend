package com.quizmicroservice.repository;

import com.quizmicroservice.model.Question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
	
	Page<Question>
	findByQuestionContainingIgnoreCaseOrQuiz_SubjectContainingIgnoreCaseOrQuiz_DifficultyContainingIgnoreCase(
	        String question,
	        String subject,
	        String difficulty,
	        Pageable pageable
	);
	
	long countByQuiz_SubjectAndQuiz_Difficulty(
	        String subject,
	        String difficulty
	);
	
    List<Question> findByQuiz_Subject(String subject);

    List<Question> findByQuiz_SubjectAndQuiz_Difficulty(String subject, String difficulty);
}