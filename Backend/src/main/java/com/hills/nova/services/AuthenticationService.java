package com.hills.nova.services;

import com.hills.nova.domain.dtos.SignupRequestDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface AuthenticationService {
    UserDetails signup(SignupRequestDto signupRequestDto);
    UserDetails authenticate(String email, String password);
    void verifyUser(UUID userId);
    String generateAccessToken(UserDetails userDetails);
    String generateRefreshToken(UserDetails userDetails);
    UserDetails validateToken(String token);
}
