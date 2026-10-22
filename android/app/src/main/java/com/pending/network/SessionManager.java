package com.pending.network;

import android.content.Context;
import android.content.SharedPreferences;

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

    public String getUserEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getUserName() {
        return pref.getString(KEY_NAME, "");
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public void logout() {
        pref.edit().clear().apply();
    }
}