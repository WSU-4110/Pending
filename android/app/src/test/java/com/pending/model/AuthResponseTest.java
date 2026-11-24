package com.pending.model;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;

import org.junit.Test;

public class AuthResponseTest {
    private final Gson gson = new Gson();

    @Test
    public void readsAccountIdFromCurrentServerResponse() {
        AuthResponse response = gson.fromJson("{\"userId\": 42}", AuthResponse.class);

        assertEquals(Long.valueOf(42), response.getId());
    }

    @Test
    public void acceptsLegacyAccountIdField() {
        AuthResponse response = gson.fromJson("{\"id\": 42}", AuthResponse.class);

        assertEquals(Long.valueOf(42), response.getId());
    }
}
