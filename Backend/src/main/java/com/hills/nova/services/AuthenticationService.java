package com.hills.nova.services;

import com.hills.nova.domain.dtos.SignupRequestDto;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {
    UserDetails signup(SignupRequestDto signupRequestDto);
}
