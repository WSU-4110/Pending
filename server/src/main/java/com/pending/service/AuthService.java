package com.pending.service;

import com.pending.dto.AuthResponse;
import com.pending.dto.LoginRequest;
import com.pending.dto.RegisterRequest;
import com.pending.model.PendingUser;
import com.pending.repository.PendingUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Service
public class AuthService {

    private final PendingUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    public AuthService(PendingUserRepository userRepository, PasswordEncoder passwordEncoder, JWTService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if(userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered. Please log in instead.");
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        PendingUser newUser = new PendingUser(
                normalizedEmail,
                hashedPassword,
                request.firstName().trim(),
                request.lastName().trim(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        PendingUser savedUser = userRepository.save(newUser);

        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail());

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                token,
                "Registration successful"
        );
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        PendingUser user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password. Please try again."
                ));

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password. Please try again."
            );
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                token,
                "Login successful"
        );
    }

}
