package com.quizmicroservice.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static class OtpEntry {
        final String otp;
        final Instant expiresAt;

        OtpEntry(String otp, Instant expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    // 10 minutes expiry
    private static final long EXPIRY_SECONDS = 10 * 60;

    public void storeOtp(String email, String otp) {
        Instant expiresAt = Instant.now().plusSeconds(EXPIRY_SECONDS);
        otpStore.put(email.toLowerCase(), new OtpEntry(otp, expiresAt));
    }

    public boolean verifyOtp(String email, String otp) {
        String key = email.toLowerCase();
        OtpEntry entry = otpStore.get(key);

        if (entry == null) {
            return false;
        }

        if (Instant.now().isAfter(entry.expiresAt)) {
            otpStore.remove(key);
            return false;
        }

        boolean matches = entry.otp.equals(otp);
        if (matches) {
            otpStore.remove(key);
        }
        return matches;
    }
}