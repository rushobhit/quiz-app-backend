package com.quizmicroservice.repository;

import com.quizmicroservice.model.AuthActivityLog;
import com.quizmicroservice.model.AuthActivityLog.EventStatus;
import com.quizmicroservice.model.AuthActivityLog.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuthActivityLogRepository extends JpaRepository<AuthActivityLog, Long> {

	Page<AuthActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
	
    long countByEmailIgnoreCaseAndEventTypeAndCreatedAtAfter(
            String email,
            EventType eventType,
            LocalDateTime createdAt
    );

    long countByEmailIgnoreCaseAndEventTypeAndEventStatusAndCreatedAtAfter(
            String email,
            EventType eventType,
            EventStatus eventStatus,
            LocalDateTime createdAt
    );
    
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
    
    List<AuthActivityLog> findByEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    List<AuthActivityLog> findTop10ByEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    Optional<AuthActivityLog> findTopByEmailIgnoreCaseAndEventTypeAndEventStatusOrderByCreatedAtDesc(
            String email,
            EventType eventType,
            EventStatus eventStatus
    );

    Optional<AuthActivityLog> findTopByEmailIgnoreCaseAndEventTypeAndCreatedAtAfterOrderByCreatedAtDesc(
            String email,
            EventType eventType,
            LocalDateTime createdAt
    );
}