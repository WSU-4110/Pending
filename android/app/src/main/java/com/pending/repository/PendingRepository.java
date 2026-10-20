package com.pending.repository;

import com.pending.model.PendingItem;
import com.pending.network.ApiService;
import com.pending.network.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingRepository {
    private final ApiService apiService;

    public PendingRepository() {
        this.apiService = RetrofitClient.getInstance();
    }

    public void createItem(PendingItem item, NetworkCallback<PendingItem> callback) {
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

    public void fetchAllItems(NetworkCallback<List<PendingItem>> callback) {
        apiService.getAllItems().enqueue(new Callback<List<PendingItem>>() {
            @Override
            public void onResponse(Call<List<PendingItem>> call, Response<List<PendingItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error loading tasks: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PendingItem>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void fetchItemById(Long id, NetworkCallback<PendingItem> callback) {
        apiService.getItemById(id).enqueue(new Callback<PendingItem>() {
            @Override
            public void onResponse(Call<PendingItem> call, Response<PendingItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Item not found: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PendingItem> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void updateItem(Long id, PendingItem item, NetworkCallback<PendingItem> callback) {
        apiService.updateItem(id, item).enqueue(new Callback<PendingItem>() {
            @Override
            public void onResponse(Call<PendingItem> call, Response<PendingItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update task: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PendingItem> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteItem(Long id, NetworkCallback<String> callback) {
        apiService.deleteItem(id).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to delete task: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
