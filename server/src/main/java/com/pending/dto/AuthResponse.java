package com.pending.dto;

public class AuthResponse {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String token;
    private String message;

    private AuthResponse(Builder builder) {
        this.userId = builder.userId;
        this.email = builder.email;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.token = builder.token;
        this.message = builder.message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private String email;
        private String firstName;
        private String lastName;
        private String token;
        private String message;

        public Builder() {}

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public AuthResponse build(){
            return new AuthResponse(this);
        }
    }
}
