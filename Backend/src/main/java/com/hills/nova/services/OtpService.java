package com.hills.nova.services;

import java.util.UUID;

public interface OtpService {
    String generateOtpAndStoreOtp(UUID userId);
    UUID verifyOtp(String otp);
    String resendOtp(UUID userId);
    boolean canResendOtp(UUID userId);
    String generateOtp();
}
