package com.hills.nova.services.impl;

import com.hills.nova.services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final Duration OTP_TTL = Duration.ofMinutes(30);
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateOtpAndStoreOtp(UUID userId) {
        String otp = String.format("%06d", secureRandom.nextInt(1000000));

        Object[] otpData = {otp, userId};

        redisTemplate.opsForValue().set(otp, otpData, OTP_TTL);

        return otp;
    }
}
