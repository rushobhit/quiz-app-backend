package com.quizmicroservice.service;

import com.quizmicroservice.model.AuthActivityLog;
import com.quizmicroservice.model.AuthActivityLog.EventStatus;
import com.quizmicroservice.model.AuthActivityLog.EventType;
import com.quizmicroservice.repository.AuthActivityLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthActivityService {

    private static final Logger logger = LoggerFactory.getLogger(AuthActivityService.class);

    private final AuthActivityLogRepository authActivityLogRepository;
    private final EmailService emailService;

    public Page<AuthActivityLog> getLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return authActivityLogRepository
                .findAllByOrderByCreatedAtDesc(pageable);
    }
    
    public AuthActivityService(AuthActivityLogRepository authActivityLogRepository,
                               EmailService emailService) {
        this.authActivityLogRepository = authActivityLogRepository;
        this.emailService = emailService;
    }

    public AuthActivityLog logLoginSuccess(Long userId,
                                           String email,
                                           String fullName,
                                           HttpServletRequest request) {
        AuthActivityLog log = buildLog(
                userId,
                email,
                fullName,
                EventType.LOGIN,
                EventStatus.SUCCESS,
                "User logged in successfully.",
                request
        );

        AuthActivityLog savedLog = authActivityLogRepository.save(log);

        sendLoginSuccessEmail(email, fullName, request);

        return savedLog;
    }

    public AuthActivityLog logLogoutSuccess(Long userId,
                                            String email,
                                            String fullName,
                                            HttpServletRequest request) {
        AuthActivityLog log = buildLog(
                userId,
                email,
                fullName,
                EventType.LOGOUT,
                EventStatus.SUCCESS,
                "User logged out successfully.",
                request
        );

        return authActivityLogRepository.save(log);
    }

    public AuthActivityLog logFailedLogin(String email,
                                          HttpServletRequest request,
                                          String message) {
        AuthActivityLog log = buildLog(
                null,
                email,
                null,
                EventType.FAILED_LOGIN,
                EventStatus.FAILED,
                message != null && !message.isBlank()
                        ? message
                        : "Invalid login attempt.",
                request
        );

        AuthActivityLog savedLog = authActivityLogRepository.save(log);

        checkAndTriggerSecurityAlert(email, request);

        return savedLog;
    }

    public long countRecentFailedLogins(String email, int minutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);

        return authActivityLogRepository
                .countByEmailIgnoreCaseAndEventTypeAndEventStatusAndCreatedAtAfter(
                        email,
                        EventType.FAILED_LOGIN,
                        EventStatus.FAILED,
                        threshold
                );
    }

    public List<AuthActivityLog> getRecentActivityByEmail(String email) {
        return authActivityLogRepository.findTop10ByEmailIgnoreCaseOrderByCreatedAtDesc(email);
    }

    public Optional<AuthActivityLog> getLatestLogin(String email) {
        return authActivityLogRepository
                .findTopByEmailIgnoreCaseAndEventTypeAndEventStatusOrderByCreatedAtDesc(
                        email,
                        EventType.LOGIN,
                        EventStatus.SUCCESS
                );
    }

    public Optional<AuthActivityLog> getLatestLogout(String email) {
        return authActivityLogRepository
                .findTopByEmailIgnoreCaseAndEventTypeAndEventStatusOrderByCreatedAtDesc(
                        email,
                        EventType.LOGOUT,
                        EventStatus.SUCCESS
                );
    }

    private void checkAndTriggerSecurityAlert(String email, HttpServletRequest request) {
        if (email == null || email.isBlank()) {
            return;
        }

        long failedAttempts = countRecentFailedLogins(email, 15);

        if (failedAttempts < 5) {
            return;
        }

        LocalDateTime cooldownThreshold = LocalDateTime.now().minusMinutes(30);

        Optional<AuthActivityLog> recentAlert = authActivityLogRepository
                .findTopByEmailIgnoreCaseAndEventTypeAndCreatedAtAfterOrderByCreatedAtDesc(
                        email,
                        EventType.SECURITY_ALERT,
                        cooldownThreshold
                );

        if (recentAlert.isPresent()) {
            return;
        }

        AuthActivityLog alertLog = buildLog(
                null,
                email,
                null,
                EventType.SECURITY_ALERT,
                EventStatus.ALERT,
                "Security alert triggered after 5 failed login attempts within 15 minutes.",
                request
        );

        authActivityLogRepository.save(alertLog);

        sendSecurityAlertEmail(email, request, failedAttempts);
    }

    private void sendLoginSuccessEmail(String email,
                                       String fullName,
                                       HttpServletRequest request) {
        if (email == null || email.isBlank()) {
            return;
        }

        try {
            String ipAddress = extractClientIp(request);
            String userAgent = extractUserAgent(request);
            String deviceInfo = extractDeviceInfo(request);

            String subject = "Quiz App Login Alert";

            String body = """
                    Hello %s,

                    Your account was successfully logged in.

                    Details:
                    - Time: %s
                    - IP address: %s
                    - Device: %s
                    - User-Agent: %s

                    If this was you, no action is needed.

                    If this was NOT you, please reset your password immediately.

                    Thanks,
                    Quiz Microservice Security Team
                    """.formatted(
                    fullName != null && !fullName.isBlank() ? fullName : "User",
                    LocalDateTime.now(),
                    valueOrUnknown(ipAddress),
                    valueOrUnknown(deviceInfo),
                    valueOrUnknown(userAgent)
            );

            emailService.sendSimpleEmail(email, subject, body);
            logger.info("Login success email sent to {}", email);
        } catch (Exception e) {
            logger.error("Failed to send login success email to {}", email, e);
        }
    }

    private void sendSecurityAlertEmail(String email,
                                        HttpServletRequest request,
                                        long failedAttempts) {
        try {
            String ipAddress = extractClientIp(request);
            String userAgent = extractUserAgent(request);
            String deviceInfo = extractDeviceInfo(request);

            String subject = "Security Alert: Multiple failed login attempts detected";

            String body = """
                    Hello,

                    We detected multiple failed login attempts on your account.

                    Details:
                    - Failed attempts: %d
                    - Time window: Last 15 minutes
                    - IP address: %s
                    - Device: %s
                    - User-Agent: %s
                    - Time: %s

                    If this was you, you can ignore this email.

                    If this was NOT you, please reset your password immediately.

                    Thanks,
                    Quiz Microservice Security Team
                    """.formatted(
                    failedAttempts,
                    valueOrUnknown(ipAddress),
                    valueOrUnknown(deviceInfo),
                    valueOrUnknown(userAgent),
                    LocalDateTime.now()
            );

            emailService.sendSimpleEmail(email, subject, body);
            logger.info("Security alert email sent to {}", email);
        } catch (Exception e) {
            logger.error("Failed to send security alert email to {}", email, e);
        }
    }

    private AuthActivityLog buildLog(Long userId,
                                     String email,
                                     String fullName,
                                     EventType eventType,
                                     EventStatus eventStatus,
                                     String message,
                                     HttpServletRequest request) {
        AuthActivityLog log = new AuthActivityLog();
        log.setUserId(userId);
        log.setEmail(email);
        log.setFullName(fullName);
        log.setEventType(eventType);
        log.setEventStatus(eventStatus);
        log.setIpAddress(extractClientIp(request));
        log.setUserAgent(extractUserAgent(request));
        log.setDeviceInfo(extractDeviceInfo(request));
        log.setMessage(message);
        return log;
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return "Unknown";
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()
                && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()
                && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "Unknown";
        }

        String userAgent = request.getHeader("User-Agent");
        return (userAgent == null || userAgent.isBlank()) ? "Unknown" : userAgent.trim();
    }

    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = extractUserAgent(request);

        if ("Unknown".equals(userAgent)) {
            return "Unknown device";
        }

        return userAgent;
    }

    private String valueOrUnknown(String value) {
        return (value == null || value.isBlank()) ? "Unknown" : value;
    }
}