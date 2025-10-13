package com.hills.nova.controllers;

import com.hills.nova.domain.dtos.*;
import com.hills.nova.exceptions.TooManyRequestsException;
import com.hills.nova.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping(path = "/sign-up")
    public ResponseEntity<SignupResponseDto> signUp(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        SignupResponseDto response = authenticationService.signup(signupRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(path = "/sign-in")
    public ResponseEntity<LoginResponse> signIn(@RequestBody LoginRequest loginRequest){
        LoginResponse loginResponse = authenticationService.signin(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );
        return ResponseEntity.ok(loginResponse);

    }

    @PostMapping(path = "/verify-otp")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody OtpRequestDto otpRequestDto) {
        String response = authenticationService.verifyUser(otpRequestDto.getOtp());
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody Map<String, String> request){
        String refreshToken = request.get("refresh_token");
        LoginResponse loginResponse = authenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/resend-otp/{id}")
    public ResponseEntity<?> resendOtp(@PathVariable UUID id){
        try {
            Map<String, Object> response = authenticationService.resendOtp(id);
            return ResponseEntity.ok(response);
        } catch (TooManyRequestsException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(path = "/initiate-password-reset/{id}")
    public ResponseEntity<?> initiatePasswordReset(@PathVariable UUID id){
        Map<String, Object> response = authenticationService.initiatePasswordReset(id);
        return ResponseEntity.ok(response);

    }

    @PutMapping(path = "/complete-password-reset")
    public ResponseEntity<?> completePasswordReset(@RequestBody CompletePasswordResetDto completePasswordResetDto){
        return ResponseEntity.ok(authenticationService.completePasswordReset(completePasswordResetDto));
    }

}

