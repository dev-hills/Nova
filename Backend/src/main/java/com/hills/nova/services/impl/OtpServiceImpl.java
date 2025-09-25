package com.hills.nova.services.impl;

import com.hills.nova.domain.entities.User;
import com.hills.nova.repositories.UserRepository;
import com.hills.nova.services.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {
    private final UserRepository userRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    private static final Duration OTP_TTL = Duration.ofMinutes(30);
    private static final Duration RESEND_COOLDOWN = Duration.ofMinutes(1);
    private static final SecureRandom secureRandom = new SecureRandom();

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String RESEND_KEY_PREFIX = "resend_cooldown:";
    private static final String USER_OTP_KEY_PREFIX = "user_otp:";


    @Override
    public String generateOtpAndStoreOtp(UUID userId) {

        String otp = String.format("%06d", secureRandom.nextInt(1000000));

        redisTemplate.opsForValue().set(OTP_KEY_PREFIX + otp, userId.toString(), OTP_TTL);

        redisTemplate.opsForValue().set(USER_OTP_KEY_PREFIX + userId, otp, OTP_TTL);

        return otp;
    }

    @Override
    public UUID verifyOtp(String otp) {
        log.info("Looking up OTP with key: {}", OTP_KEY_PREFIX + otp);
        Object storedValue = redisTemplate.opsForValue().get(OTP_KEY_PREFIX + otp);
        log.info("Stored value: {}", storedValue);

        if (storedValue == null) {
            throw new RuntimeException("OTP not found or has expired");
        }

        try{
            UUID storedUserId = UUID.fromString(storedValue.toString());

            redisTemplate.delete(OTP_KEY_PREFIX + otp);
            redisTemplate.delete(USER_OTP_KEY_PREFIX + storedUserId);
            redisTemplate.delete(RESEND_KEY_PREFIX + storedUserId);


            return storedUserId;
        }catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid OTP format");
        }

    }

    @Override
    public String resendOtp(UUID userId) {
        if (canResendOtp(userId)) {
            throw new RuntimeException("Please wait before requesting a new OTP");
        }

        User user = userRepository.findById(userId) .orElseThrow(() -> new RuntimeException("User not found"));

        if(user.getIsVerified()) {
            return null;
        }

        deleteExistingUserOtp(userId);

        String otp = String.format("%06d", secureRandom.nextInt(1000000));

        redisTemplate.opsForValue().set(OTP_KEY_PREFIX + otp, userId.toString(), OTP_TTL);
        redisTemplate.opsForValue().set(USER_OTP_KEY_PREFIX + userId, otp, OTP_TTL);
        redisTemplate.opsForValue().set(RESEND_KEY_PREFIX + userId, "blocked", RESEND_COOLDOWN);

        return otp;

    }

    @Override
    public boolean canResendOtp(UUID userId) {
        return redisTemplate.hasKey(RESEND_KEY_PREFIX + userId);
    }

    private void deleteExistingUserOtp(UUID userId) {
        Object existingOtp = redisTemplate.opsForValue().get(USER_OTP_KEY_PREFIX + userId);

        if(existingOtp != null) {
            redisTemplate.delete(OTP_KEY_PREFIX + existingOtp.toString());
            redisTemplate.delete(USER_OTP_KEY_PREFIX + userId);
        }
    }
}
