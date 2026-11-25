package com.vasudha.referral_backend.service;

import com.vasudha.referral_backend.dto.LoginRequest;
import com.vasudha.referral_backend.dto.RegisterRequest;
import com.vasudha.referral_backend.entity.Role;
import com.vasudha.referral_backend.entity.User;
import com.vasudha.referral_backend.exception.EmailAlreadyExistsException;
import com.vasudha.referral_backend.exception.InvalidCredentialsException;
import com.vasudha.referral_backend.exception.UsernameAlreadyExistsException;
import com.vasudha.referral_backend.repository.UserRepository;
import com.vasudha.referral_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER) // Never trust client for role
                .build();

        userRepository.save(user);

        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }
}
