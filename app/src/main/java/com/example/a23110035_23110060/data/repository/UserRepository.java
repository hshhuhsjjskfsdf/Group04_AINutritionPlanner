package com.example.a23110035_23110060.data.repository;

import android.content.Context;

import com.example.a23110035_23110060.data.local.AppDatabase;
import com.example.a23110035_23110060.data.local.UserEntity;
import com.example.a23110035_23110060.helper.FirebaseHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private final AppDatabase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserRepository(Context context) {
        database = AppDatabase.getInstance(context);
    }

    public void saveUserLocal(UserEntity user, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            database.userDao().insertOrUpdate(user);
            if (callback != null) {
                callback.onSuccess(null);
            }
        });
    }

    public void getCurrentUserLocal(RepositoryCallback<UserEntity> callback) {
        executor.execute(() -> {
            String userId = FirebaseHelper.getCurrentUserId();
            UserEntity user = userId == null ? null : database.userDao().getById(userId);
            if (callback != null) {
                callback.onSuccess(user);
            }
        });
    }
}
