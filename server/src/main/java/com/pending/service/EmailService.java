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
        Map<String, Object> payload = getVerificationPayload(toEmail, firstName, code);

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

    private @NonNull Map<String, Object> getVerificationPayload(String toEmail, String firstName, String code) {
        String htmlContent = """
                <body
                    style="
                        margin: 0;
                        padding: 0;
                        background-color: #f7faf8;
                        font-family: -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, Roboto, Helvetica, Arial, sans-serif;
                        -webkit-font-smoothing: antialiased;
                        "
                >
                
                    <table
                        border="0"
                        cellpadding="0"
                        cellspacing="0"
                        width="100%%"
                        style="background-color: #f7faf8; padding: 40px 20px"
                    >
                        <tr>
                            <td align="center">
                                <table
                                    border="0"
                                    cellpadding="0"
                                    cellspacoing="0"
                                    width="100%%"
                                    style="
                                        max-width: 600px;
                                        background-color: #ffffff;
                                        border-radius: 16px;
                                        box-shadow: 0 4px 12px rgba(32, 61, 53, 0.05);
                                    "
                                >
                                    <tr>
                                        <td
                                            style="
                                                background-color: #2b806b;
                                                padding: 32px 40px;
                                                text-align: center;
                                                border-radius: 16px 16px 0 0;
                                            "
                                        >
                                            <h1
                                                style="
                                                    margin: 0;
                                                    color: #ffffff;
                                                    font-size: 24px;
                                                    font-weight: 800;
                                                    letter-spacing: 1px;
                                                "
                                            >
                                                Pending
                                            </h1>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 40px 40px 20px 40px; text-align: center;">
                                            <h2 style="margin: 0; color: #203d35; font-size: 22px; font-weight: 700">
                                                Verify your email
                                            </h2>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td
                                            style="
                                                padding: 0 40px 20px 40px;
                                                color: #64736e;
                                                font-size: 16px;
                                                line-height: 24px;
                                                text-align: left;
                                            "
                                        >
                                            <p style="margin: 0 0 16px 0">Hi %s,</p>
                                            <p style="margin: 0">
                                                Thanks for setting up your <strong>Pending</strong> account.
                                                To complete your registration, please enter the 6-digit verification code below into the app.
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 20px 40px" align="center">
                                            <div
                                                style="
                                                    background-color: #e2f3ec;
                                                    border-radius: 12px;
                                                    padding: 24px;
                                                    display: inline-block;
                                                "
                                            >
                                                <span
                                                    style="
                                                        font-family: &quot;Courier New&quot;, Courier, monospace;
                                                        font-size: 36px;
                                                        font-weight: 700;
                                                        letter-spacing: 8px;
                                                        color: #2b806b;
                                                    "
                                                >
                                                    %s
                                                </span>
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td
                                            style="
                                                padding: 10px 40px 40px 40px;
                                                color: #64736e;
                                                font-size: 14px;
                                                line-height: 20px;
                                                text-align: center;
                                            "
                                        >
                                            <p style="margin: 0">This code will expire in 15 minutes.</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td
                                            style="
                                                padding: 20px 40px;
                                                background-color: #f7faf8;
                                                border-top: 1px solid #e2f3ec;
                                                border-radius: 0 0 16px 16px;
                                                color: #64736e;
                                                font-size: 12px;
                                                line-height: 18px;
                                                text-align: center;
                                            "
                                        >
                                            <p style="margin: 0">If you didn't request this code, you can safely ignore this email.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                """.formatted(firstName, code);

        return Map.of(
                "from", fromEmail,
                "to", List.of(toEmail),
                "subject", "Your Pending Verification Code: " + code,
                "html", htmlContent
        );
    }
}
