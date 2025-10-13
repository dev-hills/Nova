package com.hills.nova.services;

import com.hills.nova.domain.dtos.CompletePasswordResetDto;
import com.hills.nova.domain.dtos.LoginResponse;
import com.hills.nova.domain.dtos.SignupRequestDto;
import com.hills.nova.domain.dtos.SignupResponseDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.UUID;

public interface AuthenticationService {
    SignupResponseDto signup(SignupRequestDto signupRequestDto);
    LoginResponse signin(String email, String password);
    String verifyUser(String otp);
    LoginResponse refreshToken(String token);
    Map<String, Object> resendOtp(UUID id);
    Map<String, Object> initiatePasswordReset(UUID id);
    String completePasswordReset(CompletePasswordResetDto completePasswordResetDto);

    String generateAccessToken(UserDetails userDetails);
    String generateRefreshToken(UserDetails userDetails);
    UserDetails validateToken(String token);

}
