package com.pending.network;

import com.pending.model.AuthResponse;
import com.pending.model.LoginRequest;
import com.pending.model.PendingItem;
import com.pending.model.RegisterRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @GET("api/items")
    Call<List<PendingItem>> getAllItems();

    @GET("api/items/{id}")
    Call<PendingItem> getItemById(@Path("id") Long id);

    @POST("api/items")
    Call<PendingItem> createItem(@Body PendingItem item);

    @PUT("api/items/{id}")
    Call<PendingItem> updateItem(@Path("id") Long id, @Body PendingItem item);

    @DELETE("api/items/{id}")
    Call<String> deleteItem(@Path("id") Long id);
}