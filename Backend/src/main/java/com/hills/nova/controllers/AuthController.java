package com.hills.nova.controllers;

import com.hills.nova.domain.dtos.*;
import com.hills.nova.domain.entities.User;
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
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final UserRepository userRepository;

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

    @PostMapping(path = "/verify-otp")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody OtpRequestDto otpRequestDto) {

        try {
            UUID userId = otpService.verifyOtp(otpRequestDto.getOtp());

            authenticationService.verifyUser(userId);

            return ResponseEntity.ok("Verification successful");
        } catch (Exception e) {
            log.error("OTP verification failed", e);
            return ResponseEntity.badRequest().body("Verification failed");
        }
    }

    @PostMapping("/resend-otp/{id}")
    public ResponseEntity<?> resendOtp(@PathVariable UUID id){
        User user = userRepository.findById(id).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        if (!otpService.canResendOtp(id)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Please wait before requesting a new OTP"));
        }

        String otp = otpService.resendOtp(id);

        try{
            emailService.sendOtpEmail(
                    user.getEmail(),
                    otp,
                    user.getFirstName()
            );
            log.info("OTP email resent successfully to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to resend OTP email to user: {}", user.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to send OTP email"));
        }

        return ResponseEntity.ok(Map.of(
                "message", "OTP resent successfully",
                "otp", otp,
                "canResendAgainIn", "60 seconds"
        ));
    }

    @PostMapping(path = "/sign-in")
    public ResponseEntity<LoginResponse> signIn(@RequestBody LoginRequest loginRequest){
            UserDetails user = authenticationService.authenticate(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );

            String accessToken  = authenticationService.generateAccessToken(user);
            String refreshToken = authenticationService.generateRefreshToken(user);

            LoginResponse loginResponse = LoginResponse.builder()
                    .message("Login successful")
                    .token(
                            LoginResponse.Token.builder()
                                    .accessToken(accessToken)
                                    .refreshToken(refreshToken)
                                    .build()
                    )
                    .build();

             return ResponseEntity.ok(loginResponse);

    }

    @PostMapping(path = "/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody Map<String, String> request){
        String refreshToken = request.get("refresh_token");
        UserDetails user = authenticationService.validateToken(refreshToken);

        String newAccessToken = authenticationService.generateAccessToken(user);
        String newRefreshToken = authenticationService.generateRefreshToken(user);

        LoginResponse response = LoginResponse.builder()
                .message("Token refreshed successfully")
                .token(LoginResponse.Token.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }

}

