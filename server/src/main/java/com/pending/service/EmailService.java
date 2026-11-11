package com.pending.service;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendAPIKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendVerificationEmail(String toEmail, String firstName, String lastName, String code) {
        Map<String, Object> payload = getPayload(toEmail, firstName, code);

        try {
            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendAPIKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() >= 400) {
                System.err.println("RESEND API ERROR: " + response.body());
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to send verification email via Resend: " + response.body()
                );
            }

        } catch(ResponseStatusException e) {
            throw e;
        } catch(Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error sending verification email",
                    e
            );
        }
    }

    private @NonNull Map<String, Object> getPayload(String toEmail, String firstName, String code) {
        String htmlContent = """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; padding: 24px;">
                    <h2 style="color: #203D35;">Verify your Pending account</h2>
                    <p>Hi %s,</p>
                    <p>Enter the following 6-digit code in the Pending app to verify your email address:</p>
                    <div style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #2B806B; margin: 24px 0;">
                        %s
                    </div>
                    <p style="color: #666; font-size: 14px;">This code will expire in 15 minutes.</p>
                </div>
                """.formatted(firstName, code);

        return Map.of(
                "from", fromEmail,
                "to", List.of(toEmail),
                "subject", "Your Pending Verification Code: " + code,
                "html", htmlContent
        );
    }
}
