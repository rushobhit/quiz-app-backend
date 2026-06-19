package com.quizmicroservice.repository;

import com.quizmicroservice.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
	
	@Modifying
	@Transactional
	void deleteByUser_Id(Long userId);
	
    List<Result> findByEmail(String email);
    
    List<Result> findBySubject(String subject);
}