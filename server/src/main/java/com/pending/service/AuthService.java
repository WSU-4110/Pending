package com.pending.service;

import com.pending.dto.*;
import com.pending.model.PendingUser;
import com.pending.repository.PendingUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

    private final PendingUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(PendingUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JWTService jwtService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    private String generateVerificationCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        Optional<PendingUser> existingUserOption = userRepository.findByEmail(normalizedEmail);

        if(existingUserOption.isPresent()) {
            PendingUser existingUser = existingUserOption.get();
            if(existingUser.isVerified()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email is already registered. Please log in instead."
                );
            }

            // If the registered user exists but is not verified, then send code again after updating info
            existingUser.setPasswordHash(passwordEncoder.encode(request.password()));
            existingUser.setFirstName(request.firstName().trim());
            existingUser.setLastName(request.lastName().trim());

            String newCode = generateVerificationCode();
            existingUser.setVerificationCode(newCode);
            existingUser.setVerificationExpiration(OffsetDateTime.now().plusMinutes(15));

            PendingUser savedUser = userRepository.save(existingUser);
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFirstName(), savedUser.getLastName(), newCode);

            return new AuthResponse(
                 savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    null,
                    "Verification code was sent to your email."
            );
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        String code = generateVerificationCode();

        PendingUser newUser = new PendingUser(
                normalizedEmail,
                hashedPassword,
                request.firstName().trim(),
                request.lastName().trim(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        newUser.setVerified(false);
        newUser.setVerificationCode(code);
        newUser.setVerificationExpiration(OffsetDateTime.now().plusMinutes(15));

        PendingUser savedUser = userRepository.save(newUser);
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFirstName(), savedUser.getLastName(), code);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                null,
                "Registration successful."
        );
    }

    public AuthResponse verifyEmail(EmailVerificationRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        PendingUser user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow( () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found."
                ));

        if(user.isVerified()){
            String token = jwtService.generateToken(user.getId(), user.getEmail());
            return new AuthResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    token,
                    "Email is already verified."
            );
        }

        if(user.getVerificationCode() == null || user.getVerificationExpiration() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No active verification code found. Please request a new code."
            );
        }

        if(OffsetDateTime.now().isAfter(user.getVerificationExpiration())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Verification code has expired. Please request a new code."
            );
        }

        if(!user.getVerificationCode().equals(request.code().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid verification code. Please try again."
            );
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationExpiration(null);
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                token,
                "Email verified successfully!"
        );
    }

    public void resendVerificationCode(ResendVerificationRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        PendingUser user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found."
                ));

        if(user.isVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is already verified. Please log in."
            );
        }

        String code = generateVerificationCode();
        user.setVerificationCode(code);
        user.setVerificationExpiration(OffsetDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), user.getLastName(), code);
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

        if(!user.isVerified()) {
            String newCode = generateVerificationCode();
            user.setVerificationCode(newCode);
            user.setVerificationExpiration(OffsetDateTime.now().plusMinutes(15));

            PendingUser savedUser = userRepository.save(user);
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFirstName(), savedUser.getLastName(), newCode);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your email is not verified. Please verify your email with the 6-digit code before logging in."
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
