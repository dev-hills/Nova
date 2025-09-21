package com.hills.nova.services;

import java.util.UUID;

public interface OtpService {
    String generateOtpAndStoreOtp(UUID userId);
}
