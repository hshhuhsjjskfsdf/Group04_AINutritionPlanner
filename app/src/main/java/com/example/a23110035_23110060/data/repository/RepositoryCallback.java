package com.example.a23110035_23110060.data.repository;

public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onError(String message);
}
