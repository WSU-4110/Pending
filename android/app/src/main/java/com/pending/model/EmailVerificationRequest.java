package com.pending.model;

public class EmailVerificationRequest {
    private String email;
    private String code;

    public EmailVerificationRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }
}
