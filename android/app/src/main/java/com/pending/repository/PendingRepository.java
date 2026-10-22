package com.pending.repository;

import android.content.Context;
import com.pending.model.AuthResponse;
import com.pending.model.LoginRequest;
import com.pending.model.PendingItem;
import com.pending.model.RegisterRequest;
import com.pending.network.ApiService;
import com.pending.network.RetrofitClient;
import com.pending.network.SessionManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingRepository {
    private final ApiService apiService;
    private final SessionManager sessionManager;

    public PendingRepository(Context context) {
        this.apiService = RetrofitClient.getInstance(context);
        this.sessionManager = SessionManager.getInstance(context);
    }

    public void register(String email, String password, String firstName, String lastName, NetworkCallback<AuthResponse> callback) {
        RegisterRequest request = new RegisterRequest(email, password, firstName, lastName);
        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    String fullName = (auth.getFirstName() != null ? auth.getFirstName() : "") + " " +
                            (auth.getLastName() != null ? auth.getLastName() : "");
                    sessionManager.saveSession(auth.getToken(), auth.getId(), auth.getEmail(), fullName.trim());
                    callback.onSuccess(auth);
                } else {
                    callback.onError("Registration failed (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Network failure: " + t.getMessage());
            }
        });
    }

    public void login(String email, String password, NetworkCallback<AuthResponse> callback) {
        LoginRequest request = new LoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    String fullName = (auth.getFirstName() != null ? auth.getFirstName() : "") + " " +
                            (auth.getLastName() != null ? auth.getLastName() : "");
                    sessionManager.saveSession(auth.getToken(), auth.getId(), auth.getEmail(), fullName.trim());
                    callback.onSuccess(auth);
                } else {
                    callback.onError("Invalid credentials (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Network failure: " + t.getMessage());
            }
        });
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public Long getCurrentUserId() {
        return sessionManager.getUserId();
    }

    public void logout() {
        sessionManager.logout();
    }

    public void createItem(String title, String waitingOn, String startDate, String expectedResponseDate,
                           Integer status, String notes, Integer priority, NetworkCallback<PendingItem> callback) {
        Long ownerId = sessionManager.getUserId() != null ? sessionManager.getUserId() : 1L;
        PendingItem item = new PendingItem(ownerId, title, waitingOn, startDate, expectedResponseDate, status, notes, priority);

        apiService.createItem(item).enqueue(new Callback<PendingItem>() {
            @Override
            public void onResponse(Call<PendingItem> call, Response<PendingItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to create task (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<PendingItem> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    public void updateItem(Long id, PendingItem item, NetworkCallback<PendingItem> callback) {
        apiService.updateItem(id, item).enqueue(new retrofit2.Callback<PendingItem>() {
            @Override
            public void onResponse(retrofit2.Call<PendingItem> call, retrofit2.Response<PendingItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update task: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<PendingItem> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    public void fetchAllItems(NetworkCallback<List<PendingItem>> callback) {
        apiService.getAllItems().enqueue(new Callback<List<PendingItem>>() {
            @Override
            public void onResponse(Call<List<PendingItem>> call, Response<List<PendingItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error loading items: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PendingItem>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}