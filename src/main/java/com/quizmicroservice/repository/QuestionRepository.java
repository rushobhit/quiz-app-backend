package com.quizmicroservice.repository;

import com.quizmicroservice.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByCategory(String category);

    @Query(value = """
            SELECT TOP (:numQ) *
            FROM question
            WHERE category = :category
            ORDER BY NEWID()
            """, nativeQuery = true)
    List<Question> findRandomQuestionsByCategory(
            @Param("category") String category,
            @Param("numQ") int numQ
    );
}