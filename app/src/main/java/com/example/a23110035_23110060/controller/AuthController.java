package com.example.a23110035_23110060.controller;

import android.content.Context;

import com.example.a23110035_23110060.data.local.UserEntity;
import com.example.a23110035_23110060.data.repository.FirebaseRepository;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.data.repository.UserRepository;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.ValidationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthController {
    private final FirebaseAuth auth;
    private final UserRepository userRepository;
    private final FirebaseRepository firebaseRepository;

    public AuthController(Context context) {
        auth = FirebaseHelper.getAuth();
        userRepository = new UserRepository(context);
        firebaseRepository = new FirebaseRepository(context);
    }

    public boolean hasCurrentUser() {
        return auth.getCurrentUser() != null;
    }

    public void login(String email, String password, RepositoryCallback<Void> callback) {
        if (!ValidationHelper.isValidEmail(email)) {
            callback.onError("Email không hợp lệ");
            return;
        }
        if (!ValidationHelper.isValidPassword(password)) {
            callback.onError("Mật khẩu cần ít nhất 6 ký tự");
            return;
        }
        auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError("Đăng nhập thất bại: " + e.getMessage()));
    }

    public void register(String fullName, String email, String password, RepositoryCallback<Void> callback) {
        if (fullName == null || fullName.trim().isEmpty()) {
            callback.onError("Vui lòng nhập họ tên");
            return;
        }
        if (!ValidationHelper.isValidEmail(email)) {
            callback.onError("Email không hợp lệ");
            return;
        }
        if (!ValidationHelper.isValidPassword(password)) {
            callback.onError("Mật khẩu cần ít nhất 6 ký tự");
            return;
        }
        auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Không tạo được người dùng");
                        return;
                    }
                    UserEntity user = new UserEntity();
                    user.userId = firebaseUser.getUid();
                    user.fullName = fullName.trim();
                    user.email = email.trim();
                    user.createdAt = System.currentTimeMillis();
                    user.updatedAt = user.createdAt;
                    userRepository.saveUserLocal(user, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            firebaseRepository.createUserProfile(user, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    callback.onSuccess(null);
                                }

                                @Override
                                public void onError(String message) {
                                    callback.onSuccess(null);
                                }
                            });
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onError("Đăng ký thất bại: " + e.getMessage()));
    }

    public void forgotPassword(String email, RepositoryCallback<Void> callback) {
        if (!ValidationHelper.isValidEmail(email)) {
            callback.onError("Email không hợp lệ");
            return;
        }
        auth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError("Không gửi được email đặt lại mật khẩu"));
    }

    public void logout() {
        auth.signOut();
    }
}
