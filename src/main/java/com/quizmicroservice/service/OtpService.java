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

    private static class VerifiedEntry {
        final Instant expiresAt;

        VerifiedEntry(Instant expiresAt) {
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final Map<String, VerifiedEntry> verifiedStore = new ConcurrentHashMap<>();

    // Match frontend OTP validity: 5 minutes
    private static final long OTP_EXPIRY_SECONDS = 5 * 60;

    // Verified email session validity for signup completion: 15 minutes
    private static final long VERIFIED_EXPIRY_SECONDS = 15 * 60;

    public void storeOtp(String email, String otp) {
        String key = normalize(email);
        Instant expiresAt = Instant.now().plusSeconds(OTP_EXPIRY_SECONDS);

        otpStore.put(key, new OtpEntry(otp, expiresAt));

        // resend/new OTP means previous verification should no longer count
        verifiedStore.remove(key);
    }

    public boolean verifyOtp(String email, String otp) {
        String key = normalize(email);
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

    public void markVerified(String email) {
        String key = normalize(email);
        Instant expiresAt = Instant.now().plusSeconds(VERIFIED_EXPIRY_SECONDS);
        verifiedStore.put(key, new VerifiedEntry(expiresAt));
    }

    public boolean isVerified(String email) {
        String key = normalize(email);
        VerifiedEntry entry = verifiedStore.get(key);

        if (entry == null) {
            return false;
        }

        if (Instant.now().isAfter(entry.expiresAt)) {
            verifiedStore.remove(key);
            return false;
        }

        return true;
    }

    public void clearOtp(String email) {
        String key = normalize(email);
        otpStore.remove(key);
        verifiedStore.remove(key);
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}