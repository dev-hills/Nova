package com.hills.nova.services.impl;

import com.hills.nova.domain.dtos.CompletePasswordResetDto;
import com.hills.nova.domain.dtos.SignupRequestDto;
import com.hills.nova.domain.entities.User;
import com.hills.nova.exceptions.UserAlreadyExistsException;
import com.hills.nova.repositories.UserRepository;
import com.hills.nova.security.NovaUserDetails;
import com.hills.nova.services.AuthenticationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    public UserDetails signup(SignupRequestDto signupRequestDto) {
        Optional<User> existingUser = userRepository.findByEmail(signupRequestDto.getEmail());
        if(existingUser.isPresent()) {
            throw new UserAlreadyExistsException("User already exists with email " + signupRequestDto.getEmail());
        }
        User newUser = User.builder()
                .email(signupRequestDto.getEmail())
                .password(passwordEncoder.encode(signupRequestDto.getPassword()))
                .firstName(signupRequestDto.getFirstName())
                .lastName(signupRequestDto.getLastName())
                .dateOfBirth(signupRequestDto.getDateOfBirth())
                .mobileNumber(signupRequestDto.getMobileNumber())
                .build();

        User savedUser = userRepository.save(newUser);
        return new NovaUserDetails(savedUser);
    }

    @Override
    public UserDetails authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        return userDetailsService.loadUserByUsername(email);
    }

    @Override
    public void verifyUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () ->  new RuntimeException("User not found with ID: " + userId)
        );

        if(user.getIsVerified()){
            throw new UserAlreadyExistsException("User already verified");
        }

        user.setIsVerified(true);
        userRepository.save(user);

    }

    @Override
    public String generateAccessToken(UserDetails userDetails){
        long jwtExpiryMs = 15 * 60 * 1000;
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiryMs))
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        long refreshExpiryMs = 7 * 24 * 60 * 60 * 1000;
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiryMs))
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();

    }


    @Override
    public UserDetails validateToken(String token) {
        String username = extractUsername(token);
        return userDetailsService.loadUserByUsername(username);
    }

    @Override
    public String completePasswordReset(CompletePasswordResetDto completePasswordResetDto) {
        Object storedValue = redisTemplate.opsForValue().get(completePasswordResetDto.getOtp());

        if (storedValue == null) {
            throw new RuntimeException("OTP not found or has expired");
        }

        UUID storedUserId = UUID.fromString(storedValue.toString());

        if (!storedUserId.equals(completePasswordResetDto.getId())) {
            throw new RuntimeException("OTP does not belong to this user");
        }

        String hashedPassword = passwordEncoder.encode(completePasswordResetDto.getPassword());

        User user = userRepository.findById(completePasswordResetDto.getId()).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        user.setPassword(hashedPassword);

        userRepository.save(user);

        redisTemplate.delete(completePasswordResetDto.getOtp());

        return "Password reset successful";

    }

    private String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigninKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    private Key getSigninKey(){
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

