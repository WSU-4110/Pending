package com.pending.service;


import com.pending.dto.*;
import com.pending.model.PendingUser;
import com.pending.repository.PendingUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private PendingUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTService jwtService;

    @Mock
    private EmailService emailService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, emailService);
    }

    // Test 1: AuthService.register()
    @Test
    @Timeout(5)
    @DisplayName("register: creates unverified user, sends code, and rejects already-verified email")
    void testRegister() {
        System.out.println("Starting valid equivalence partition");
        // Valid equivalence partition: new user registration with mixed-case and whitespace email
        RegisterRequest newRequest = mock(RegisterRequest.class);
        when(newRequest.email()).thenReturn(" mahdi@example.com ");
        when(newRequest.password()).thenReturn("ThisIsAPassword12!");
        when(newRequest.firstName()).thenReturn(" Mahdi ");
        when(newRequest.lastName()).thenReturn(" Ballout ");

        when(userRepository.findByEmail("mahdi@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("ThisIsAPassword12!")).thenReturn("hashed-password");
        when(userRepository.save(any(PendingUser.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.register(newRequest);

        ArgumentCaptor<PendingUser> savedUserCaptor = ArgumentCaptor.forClass(PendingUser.class);
        verify(userRepository).save(savedUserCaptor.capture());
        PendingUser savedUser = savedUserCaptor.getValue();

        assertAll("New user registration",
                () -> assertEquals("mahdi@example.com", savedUser.getEmail()),
                () -> assertEquals("hashed-password", savedUser.getPasswordHash()),
                () -> assertEquals("Mahdi", savedUser.getFirstName()),
                () -> assertEquals("Ballout", savedUser.getLastName()),
                () -> assertFalse(savedUser.isVerified()),
                () -> assertNotNull(savedUser.getVerificationCode()),
                () -> assertTrue(savedUser.getVerificationCode().matches("\\d{6}")),
                () -> assertNotNull(savedUser.getVerificationExpiration()),
                () -> assertEquals("Registration successful.", response.message()),
                () -> assertNull(response.token())
        );

        verify(emailService).sendVerificationEmail(
                eq("mahdi@example.com"), eq("Mahdi"), eq("Ballout"), eq(savedUser.getVerificationCode())
        );

        System.out.println("Completed valid equivalence partition");

        System.out.println("Starting invalid equivalence partition");
        // Invalid equivalence partition: new user registration with mixed-case and whitespace email
        PendingUser verifiedUser = new PendingUser(
            "taken@example.com", "hash", "John", "Smith", OffsetDateTime.now(), OffsetDateTime.now()
        );
        verifiedUser.setVerified(true);

        RegisterRequest dupliaceRequest = mock(RegisterRequest.class);
        when(dupliaceRequest.email()).thenReturn("taken@example.com");
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(verifiedUser));

        ResponseStatusException conflictException = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(dupliaceRequest)
        );
        assertEquals(HttpStatus.CONFLICT, conflictException.getStatusCode());
        System.out.println("Completed invalid equivalence partition");
    }

    // Test 2: AuthService.verifyEmail()
    @Test
    @Timeout(5)
    @DisplayName("verifyEmail: verifies valid unexpired code and rejects wrong/expired codes.")
    void testVerifyEmail() {
        System.out.println("Starting valid equivalence partition");
        // Valid equivalence partition: valid and unexpired code
        PendingUser user = new PendingUser(
            "mahdi@example.com", "hashed", "Mahdi", "Ballout", OffsetDateTime.now(), OffsetDateTime.now()
        );
        user.setVerified(false);
        user.setVerificationCode("123456");
        user.setVerificationExpiration(OffsetDateTime.now().plusMinutes(10));

        EmailVerificationRequest validRequest = mock(EmailVerificationRequest.class);
        when(validRequest.email()).thenReturn("MAHDI@example.com");
        when(validRequest.code()).thenReturn(" 123456 ");

        when(userRepository.findByEmail("mahdi@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), eq("mahdi@example.com"))).thenReturn("jwt-token-abc");

        AuthResponse response = authService.verifyEmail(validRequest);

        assertAll("Successful email verification",
                () -> assertTrue(user.isVerified()),
                () -> assertNull(user.getVerificationCode()),
                () -> assertNull(user.getVerificationExpiration()),
                () -> assertEquals("jwt-token-abc", response.token()),
                () -> assertEquals("Email verified successfully!", response.message())
        );

        verify(userRepository).save(user);
        System.out.println("Completed valid equivalence partition");

        System.out.println("Starting invalid equivalence partition");
        // Invalid equivalence partition: expired code
        PendingUser expiredUser = new PendingUser(
            "expired@example.com", "hashed", "John", "Smith", OffsetDateTime.now(), OffsetDateTime.now()
        );
        expiredUser.setVerified(false);
        expiredUser.setVerificationCode("654321");
        expiredUser.setVerificationExpiration(OffsetDateTime.now().minusSeconds(1));

        EmailVerificationRequest expiredRequest = mock(EmailVerificationRequest.class);
        when(expiredRequest.email()).thenReturn("expired@example.com");
        when(userRepository.findByEmail("expired@example.com")).thenReturn(Optional.of(expiredUser));

        ResponseStatusException expiredEx = assertThrows(
                ResponseStatusException.class,
                () -> authService.verifyEmail(expiredRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, expiredEx.getStatusCode());
        System.out.println("Completed invalid equivalence partition");
    }

    // Test 3: AuthService.resendVerificationCode()
    @Test
    @Timeout(5)
    @DisplayName("resendVerificationCode: issues new code for unverified user and rejects verified/missing users")
    void testResendVerificationCode() {
        System.out.println("Starting valid equivalence partition");
        // Valid equivalence partition: unverified account receives a newly generated 6-digit code
        PendingUser unverifiedUser = new PendingUser(
            "mahdi@example.com", "hashed", "Mahdi", "Ballout", OffsetDateTime.now(), OffsetDateTime.now()
        );
        unverifiedUser.setVerified(false);

        ResendVerificationRequest validRequest = mock(ResendVerificationRequest.class);
        when(validRequest.email()).thenReturn(" mahdi@example.com ");
        when(userRepository.findByEmail("mahdi@example.com")).thenReturn(Optional.of(unverifiedUser));

        authService.resendVerificationCode(validRequest);

        assertAll("Resent verification code",
                () -> assertNotNull(unverifiedUser.getVerificationCode()),
                () -> assertTrue(unverifiedUser.getVerificationCode().matches("\\d{6}")),
                () -> assertTrue(unverifiedUser.getVerificationExpiration().isAfter(OffsetDateTime.now()))
        );
        verify(userRepository).save(unverifiedUser);
        verify(emailService).sendVerificationEmail(
                eq("mahdi@example.com"), eq("Mahdi"), eq("Ballout"), eq(unverifiedUser.getVerificationCode())
        );
        System.out.println("Completed valid equivalence partition");

        System.out.println("Starting valid equivalence partition");
        // Invalid equivalence partition: already verified user
        PendingUser verifiedUser = new PendingUser(
                "done@example.com", "hashed", "John", "Smith", OffsetDateTime.now(), OffsetDateTime.now()
        );
        verifiedUser.setVerified(true);

        ResendVerificationRequest verifiedRequest = mock(ResendVerificationRequest.class);
        when(verifiedRequest.email()).thenReturn("done@example.com");
        when(userRepository.findByEmail("done@example.com")).thenReturn(Optional.of(verifiedUser));

        ResponseStatusException badRequestException = assertThrows(
                ResponseStatusException.class,
                () -> authService.resendVerificationCode(verifiedRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, badRequestException.getStatusCode());
        System.out.println("Completed invalid equivalence partition");
    }

    // Test 4: AuthService.login()
    @Test
    @Timeout(5)
    @DisplayName("login: returns JWT for verified user and blocks invalid password or unverified email")
    void testLogin() {
        System.out.println("Starting valid equivalence partition");
        // Valid equivalence partition: verified user with matching password
        PendingUser verifiedUser = new PendingUser(
                "mahdi@example.com", "hashed-password", "Mahdi", "Ballout", OffsetDateTime.now(), OffsetDateTime.now()
        );
        verifiedUser.setVerified(true);

        LoginRequest validLogin = mock(LoginRequest.class);
        when(validLogin.email()).thenReturn("Mahdi@example.com");
        when(validLogin.password()).thenReturn("CorrectPassword12!");

        when(userRepository.findByEmail("mahdi@example.com")).thenReturn(Optional.of(verifiedUser));
        when(passwordEncoder.matches("CorrectPassword12!", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(any(), eq("mahdi@example.com"))).thenReturn("valid-jwt");

        AuthResponse response = authService.login(validLogin);

        assertAll("Successful login",
                () -> assertEquals("mahdi@example.com", response.email()),
                () -> assertEquals("valid-jwt", response.token()),
                () -> assertEquals("Login successful", response.message())
        );

        System.out.println("Completed valid equivalence partition");

        System.out.println("Starting valid equivalence partition 1");
        // Invalid equivalence partition 1: wrong password
        LoginRequest wrongPassLogin = mock(LoginRequest.class);
        when(wrongPassLogin.email()).thenReturn("mahdi@example.com");
        when(wrongPassLogin.password()).thenReturn("WrongPassword12!");
        when(passwordEncoder.matches("WrongPassword12!", "hashed-password")).thenReturn(false);

        ResponseStatusException unauthorizedException = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(wrongPassLogin)
        );
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedException.getStatusCode());
        System.out.println("Completed invalid equivalence partition 1");

        System.out.println("Starting valid equivalence partition 2");
        // Invalid equivalence partition 2: correct password but unverified email -> resends code
        PendingUser unverifiedUser = new PendingUser(
                "new@example.com", "hashed-password", "New", "User", OffsetDateTime.now(), OffsetDateTime.now()
        );
        unverifiedUser.setVerified(false);

        LoginRequest unverifiedLogin = mock(LoginRequest.class);
        when(unverifiedLogin.email()).thenReturn("new@example.com");
        when(unverifiedLogin.password()).thenReturn("NewPassword12!");

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(unverifiedUser));
        when(passwordEncoder.matches("NewPassword12!", "hashed-password")).thenReturn(true);
        when(userRepository.save(any(PendingUser.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseStatusException forbiddenException = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(unverifiedLogin)
        );
        assertEquals(HttpStatus.FORBIDDEN, forbiddenException.getStatusCode());
        verify(emailService).sendVerificationEmail(
                eq("new@example.com"), eq("New"), eq("User"), anyString()
        );
        System.out.println("Completed invalid equivalence partition 2");
    }
}
