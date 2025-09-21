package com.hills.nova.controllers;

import com.hills.nova.domain.dtos.SignupRequestDto;
import com.hills.nova.domain.dtos.SignupResponseDto;
import com.hills.nova.repositories.UserRepository;
import com.hills.nova.security.NovaUserDetails;
import com.hills.nova.services.AuthenticationService;
import com.hills.nova.services.EmailService;
import com.hills.nova.services.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final OtpService otpService;
    private final EmailService emailService;

    @PostMapping(path = "/sign-up")
    public ResponseEntity<SignupResponseDto> signUp(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        UserDetails userDetails = authenticationService.signup(signupRequestDto);
        NovaUserDetails novaUserDetails = (NovaUserDetails) userDetails;

        String otp = otpService.generateOtpAndStoreOtp(novaUserDetails.getId());

        try {
            emailService.sendOtpEmail(
                    novaUserDetails.getUser().getEmail(),
                    otp,
                    novaUserDetails.getUser().getFirstName()
            );
            log.info("OTP email sent successfully to user: {}", novaUserDetails.getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP email to user: {}", novaUserDetails.getUser().getEmail(), e);
        }

        SignupResponseDto signupResponseDto = SignupResponseDto.builder()
                .id(novaUserDetails.getId())
                .email(novaUserDetails.getUser().getEmail())
                .message("Account created successfully. Please check your email for the verification code.")
                .createdAt(novaUserDetails.getUser().getCreatedAt())
                .otp(otp)
                .build();

        return new ResponseEntity<>(signupResponseDto, HttpStatus.CREATED);
    }
}
