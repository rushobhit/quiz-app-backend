package com.quizmicroservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_activity_logs")
public class AuthActivityLog {

    public enum EventType {
        LOGIN,
        LOGOUT,
        FAILED_LOGIN,
        SECURITY_ALERT,
        PASSWORD_RESET_REQUEST,
        PASSWORD_RESET_SUCCESS
    }

    public enum EventStatus {
        SUCCESS,
        FAILED,
        ALERT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false, length = 20)
    private EventStatus eventStatus;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AuthActivityLog() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        email = normalizeEmail(email);
        fullName = normalizeText(fullName);
        ipAddress = normalizeText(ipAddress);
        userAgent = normalizeText(userAgent);
        deviceInfo = normalizeText(deviceInfo);
        message = normalizeText(message);
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = normalizeText(fullName);
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public EventStatus getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = normalizeText(ipAddress);
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = normalizeText(userAgent);
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = normalizeText(deviceInfo);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = normalizeText(message);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}