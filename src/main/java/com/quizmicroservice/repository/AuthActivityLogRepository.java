package com.quizmicroservice.repository;

import com.quizmicroservice.model.AuthActivityLog;
import com.quizmicroservice.model.AuthActivityLog.EventStatus;
import com.quizmicroservice.model.AuthActivityLog.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuthActivityLogRepository extends JpaRepository<AuthActivityLog, Long> {

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

    List<AuthActivityLog> findByEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    List<AuthActivityLog> findTop10ByEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    Optional<AuthActivityLog> findTopByEmailIgnoreCaseAndEventTypeOrderByCreatedAtDesc(
            String email,
            EventType eventType
    );

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