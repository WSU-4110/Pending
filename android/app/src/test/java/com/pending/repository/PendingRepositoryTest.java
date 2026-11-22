package com.pending.repository;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.pending.model.AuthResponse;
import com.pending.model.LoginRequest;
import com.pending.model.PendingItem;
import com.pending.model.RegisterRequest;
import com.pending.network.ApiService;
import com.pending.network.SessionManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingRepositoryTest {

    @Mock
    private ApiService mockApiService;

    @Mock
    private SessionManager mockSessionManager;

    @Mock
    private Call<AuthResponse> mockAuthCall;

    @Mock
    private Call<PendingItem> mockItemCall;

    @Mock
    private Call<List<PendingItem>> mockItemListCall;

    @Mock
    private Call<ResponseBody> mockDeleteCall;

    private PendingRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new PendingRepository(mockApiService, mockSessionManager);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void login_success_savesSessionAndInvokesOnSuccess() {
        AuthResponse fakeAuth = mock(AuthResponse.class);
        when(fakeAuth.getToken()).thenReturn("jwt-token-123");
        when(fakeAuth.getId()).thenReturn(8L);
        when(fakeAuth.getEmail()).thenReturn("test@example.com");
        when(fakeAuth.getFirstName()).thenReturn("Sophia");
        when(fakeAuth.getLastName()).thenReturn("Maher");

        when(mockApiService.login(any(LoginRequest.class))).thenReturn(mockAuthCall);

        NetworkCallback<AuthResponse> callback = mock(NetworkCallback.class);
        repository.login("test@example.com", "Password123!", callback);

        ArgumentCaptor<Callback<AuthResponse>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockAuthCall).enqueue(captor.capture());

        captor.getValue().onResponse(mockAuthCall, Response.success(fakeAuth));

        verify(mockSessionManager).saveSession("jwt-token-123", 8L, "test@example.com", "Sophia Maher");
        verify(callback).onSuccess(fakeAuth);
        verify(callback, never()).onError(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void login_unverified403_invokesOnErrorWithUnverified() {
        when(mockApiService.login(any(LoginRequest.class))).thenReturn(mockAuthCall);

        NetworkCallback<AuthResponse> callback = mock(NetworkCallback.class);
        repository.login("unverified@example.com", "Password123!", callback);

        ArgumentCaptor<Callback<AuthResponse>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockAuthCall).enqueue(captor.capture());

        Response<AuthResponse> error403 = Response.error(
                403, ResponseBody.create(MediaType.parse("application/json"), "{}"));
        captor.getValue().onResponse(mockAuthCall, error403);

        verify(mockSessionManager, never()).saveSession(anyString(), anyLong(), anyString(), anyString());
        verify(callback).onError("UNVERIFIED");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void register_success_invokesOnSuccess() {
        AuthResponse fakeAuth = mock(AuthResponse.class);
        when(mockApiService.register(any(RegisterRequest.class))).thenReturn(mockAuthCall);

        NetworkCallback<AuthResponse> callback = mock(NetworkCallback.class);
        repository.register("new@example.com", "Password123!", "Sophia", "Maher", callback);

        ArgumentCaptor<Callback<AuthResponse>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockAuthCall).enqueue(captor.capture());

        captor.getValue().onResponse(mockAuthCall, Response.success(fakeAuth));

        verify(callback).onSuccess(fakeAuth);
        verify(callback, never()).onError(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void register_networkFailure_invokesOnError() {
        when(mockApiService.register(any(RegisterRequest.class))).thenReturn(mockAuthCall);

        NetworkCallback<AuthResponse> callback = mock(NetworkCallback.class);
        repository.register("new@example.com", "Password123!", "Sophia", "Maher", callback);

        ArgumentCaptor<Callback<AuthResponse>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockAuthCall).enqueue(captor.capture());

        captor.getValue().onFailure(mockAuthCall, new RuntimeException("Timeout"));

        verify(callback).onError(contains("Network failure: Timeout"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void validateSession_notLoggedIn_immediatelyCallsOnError() {
        when(mockSessionManager.isLoggedIn()).thenReturn(false);

        NetworkCallback<Boolean> callback = mock(NetworkCallback.class);
        repository.validateSession(callback);

        verify(callback).onError("No stored token");
        verify(mockApiService, never()).getAllItems();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void validateSession_expiredToken401_clearsSessionAndCallsOnError() {
        when(mockSessionManager.isLoggedIn()).thenReturn(true);
        when(mockApiService.getAllItems()).thenReturn(mockItemListCall);

        NetworkCallback<Boolean> callback = mock(NetworkCallback.class);
        repository.validateSession(callback);

        ArgumentCaptor<Callback<List<PendingItem>>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockItemListCall).enqueue(captor.capture());

        Response<List<PendingItem>> unauthorized = Response.error(
                401, ResponseBody.create(MediaType.parse("application/json"), "{}"));
        captor.getValue().onResponse(mockItemListCall, unauthorized);

        verify(mockSessionManager).clearSession();
        verify(callback).onError(contains("401"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void validateSession_validToken200_callsOnSuccess() {
        when(mockSessionManager.isLoggedIn()).thenReturn(true);
        when(mockApiService.getAllItems()).thenReturn(mockItemListCall);

        NetworkCallback<Boolean> callback = mock(NetworkCallback.class);
        repository.validateSession(callback);

        ArgumentCaptor<Callback<List<PendingItem>>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockItemListCall).enqueue(captor.capture());

        captor.getValue().onResponse(mockItemListCall, Response.success(Collections.emptyList()));

        verify(mockSessionManager, never()).clearSession();
        verify(callback).onSuccess(true);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void createItem_nullUserId_defaultsOwnerIdTo8LAndSucceeds() {
        when(mockSessionManager.getUserId()).thenReturn(null);
        when(mockApiService.createItem(any(PendingItem.class))).thenReturn(mockItemCall);

        NetworkCallback<PendingItem> callback = mock(NetworkCallback.class);
        repository.createItem("Submit Report", "Manager", "2026-11-20", "2026-11-25", 0, "Notes", 2, callback);

        ArgumentCaptor<PendingItem> itemCaptor = ArgumentCaptor.forClass(PendingItem.class);
        verify(mockApiService).createItem(itemCaptor.capture());
        assertEquals(Long.valueOf(8L), itemCaptor.getValue().getOwnerId());
        assertEquals("Submit Report", itemCaptor.getValue().getTitle());

        ArgumentCaptor<Callback<PendingItem>> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(mockItemCall).enqueue(callbackCaptor.capture());

        PendingItem saved = new PendingItem();
        saved.setId(42L);
        callbackCaptor.getValue().onResponse(mockItemCall, Response.success(saved));

        verify(callback).onSuccess(saved);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void updateItem_success_invokesOnSuccess() {
        PendingItem item = new PendingItem();
        item.setTitle("Updated Task");
        when(mockApiService.updateItem(eq(10L), eq(item))).thenReturn(mockItemCall);

        NetworkCallback<PendingItem> callback = mock(NetworkCallback.class);
        repository.updateItem(10L, item, callback);

        ArgumentCaptor<Callback<PendingItem>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockItemCall).enqueue(captor.capture());

        captor.getValue().onResponse(mockItemCall, Response.success(item));

        verify(callback).onSuccess(item);
        verify(callback, never()).onError(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void updateItem_notFound404_invokesOnError() {
        PendingItem item = new PendingItem();
        when(mockApiService.updateItem(eq(99L), eq(item))).thenReturn(mockItemCall);

        NetworkCallback<PendingItem> callback = mock(NetworkCallback.class);
        repository.updateItem(99L, item, callback);

        ArgumentCaptor<Callback<PendingItem>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockItemCall).enqueue(captor.capture());

        Response<PendingItem> error404 = Response.error(
                404, ResponseBody.create(MediaType.parse("application/json"), "{}"));
        captor.getValue().onResponse(mockItemCall, error404);

        verify(callback).onError("Failed to update task: 404");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void deleteItem_success_invokesOnSuccess() {
        when(mockApiService.deleteItem(15L)).thenReturn(mockDeleteCall);

        NetworkCallback<String> callback = mock(NetworkCallback.class);
        repository.deleteItem(15L, callback);

        ArgumentCaptor<Callback<ResponseBody>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockDeleteCall).enqueue(captor.capture());

        ResponseBody body = ResponseBody.create(MediaType.parse("text/plain"), "Pending item deleted.");
        captor.getValue().onResponse(mockDeleteCall, Response.success(body));

        verify(callback).onSuccess("Task deleted successfully");
        verify(callback, never()).onError(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void deleteItem_notFound404_invokesOnError() {
        when(mockApiService.deleteItem(15L)).thenReturn(mockDeleteCall);

        NetworkCallback<String> callback = mock(NetworkCallback.class);
        repository.deleteItem(15L, callback);

        ArgumentCaptor<Callback<ResponseBody>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockDeleteCall).enqueue(captor.capture());

        Response<ResponseBody> error404 = Response.error(
                404, ResponseBody.create(MediaType.parse("text/plain"), ""));
        captor.getValue().onResponse(mockDeleteCall, error404);

        verify(callback).onError("Failed to delete task: 404");
    }
}