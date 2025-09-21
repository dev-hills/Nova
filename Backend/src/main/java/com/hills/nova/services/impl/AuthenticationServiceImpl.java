package com.hills.nova.services.impl;

import com.hills.nova.domain.dtos.SignupRequestDto;
import com.hills.nova.domain.entities.User;
import com.hills.nova.exceptions.UserAlreadyExistsException;
import com.hills.nova.repositories.UserRepository;
import com.hills.nova.security.NovaUserDetails;
import com.hills.nova.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}
