package com.pending.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SessionManager {
    private static final String PREF_NAME = "pending_user_session";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_NAME = "user_name";

    private static SessionManager instance;
    private final SharedPreferences pref;

    private SessionManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveSession(String token, Long userId, String email, String fullName) {
        pref.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, userId != null ? userId : -1L)
                .putString(KEY_EMAIL, email)
                .putString(KEY_NAME, fullName)
                .apply();
    }

    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    public Long getUserId() {
        long id = pref.getLong(KEY_USER_ID, -1L);
        return id != -1L ? id : null;
    }

    /** Returns the account ID for local preference scoping, including sessions saved before userId was mapped. */
    @Nullable
    public Long getSignedInUserId() {
        Map<String, ?> session = pref.getAll();
        Object token = session.get(KEY_TOKEN);
        if (!(token instanceof String) || ((String) token).isEmpty()) {
            return null;
        }

        Object id = session.get(KEY_USER_ID);
        if (id instanceof Long && (Long) id > 0) {
            return (Long) id;
        }

        // Older installs received this token from the server but stored no account ID.
        // The payload is used only as a local storage namespace. The server validates the token.
        String[] tokenParts = ((String) token).split("\\.", -1);
        if (tokenParts.length != 3) {
            return null;
        }

        try {
            byte[] payload = Base64.decode(tokenParts[1], Base64.URL_SAFE | Base64.NO_WRAP);
            JSONObject claims = new JSONObject(new String(payload, StandardCharsets.UTF_8));
            long userId = claims.optLong("userId", -1L);
            return userId > 0 ? userId : null;
        } catch (IllegalArgumentException | JSONException invalidToken) {
            return null;
        }
    }

    public String getUserEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getUserName() {
        return pref.getString(KEY_NAME, "");
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public void clearSession() {
        pref.edit().clear().apply();
    }
    public void logout() {
        pref.edit().clear().apply();
    }
}