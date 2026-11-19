package com.pending.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
        ReflectionTestUtils.setField(emailService, "resendAPIKey", "test-api-key");
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@pending.app");
        ReflectionTestUtils.setField(emailService, "httpClient", mockHttpClient);
    }

    // Test 5: EmailService.sendVerificationEmail()
    @Test
    @Timeout(5)
    @DisplayName("sendVerificationEmail: sends POST request on 200 OK and throws 500 on >= 400 status")
    void testSendVerificationEmail() throws Exception {
        System.out.println("Starting valid equivalence partition");
        // Valid equivalence partition: Resent API returns 200 OK
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpClient.send(
                any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()
        )).thenReturn(mockHttpResponse);

        assertDoesNotThrow(
                () -> emailService.sendVerificationEmail("mahdi@example.com", "Mahdi", "Ballout", "123456")
        );

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(
                requestCaptor.capture(),
                ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()
        );
        HttpRequest sentRequest = requestCaptor.getValue();

        assertAll("Outgoing Resent HTTP request",
                () -> assertEquals(URI.create("https://api.resend.com/emails"), sentRequest.uri()),
                () -> assertEquals("POST", sentRequest.method()),
                () -> assertEquals("Bearer test-api-key", sentRequest.headers().firstValue("Authorization").orElse("")),
                () -> assertEquals("application/json", sentRequest.headers().firstValue("Content-Type").orElse(""))
        );

        System.out.println("Completed valid equivalence partition");

        System.out.println("Starting invalid equivalence partition");
        // Invalid equivalence partition: statusCode == 400 triggers ResponseStatusException
        when(mockHttpResponse.statusCode()).thenReturn(400);
        when(mockHttpResponse.body()).thenReturn("{\"message\":\"Invalid recipient\"}");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> emailService.sendVerificationEmail("bad-email", "John", "Smith", "123456")
        );
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Failed to send verification email via Resend"));

        System.out.println("Completed invalid equivalence partition");
    }

    // Test 6: EmailService.getVerificationPayload()
    @Test
    @Timeout(5)
    @DisplayName("getVerificationPayload: builds complete Resend payload with recipient, subject, and HTML code block")
    void testGetVerificationPayload() {
        Map<String, Object> payload = ReflectionTestUtils.invokeMethod(
                emailService,
                "getVerificationPayload",
                "mahdi@example.com",
                "Mahdi",
                "987654"
        );

        assertNotNull(payload);
        String html = (String) payload.get("html");

        assertAll("Verification email payload fields",
                () -> assertEquals("noreply@pending.app", payload.get("from")),
                () -> assertEquals(List.of("mahdi@example.com"), payload.get("to")),
                () -> assertEquals("Your Pending Verification Code: 987654", payload.get("subject")),
                () -> assertNotNull(html),
                () -> assertTrue(html.contains("Hi Mahdi,")),
                () -> assertTrue(html.contains("987654")),
                () -> assertTrue(html.contains("This code will expire in 15 minutes."))
        );
    }
}
