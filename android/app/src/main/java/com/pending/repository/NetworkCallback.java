package com.pending.repository;

public interface NetworkCallback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
}