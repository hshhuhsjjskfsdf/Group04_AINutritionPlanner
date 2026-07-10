package com.example.a23110035_23110060.controller;

import android.content.Context;

import com.example.a23110035_23110060.data.local.UserEntity;
import com.example.a23110035_23110060.data.repository.FirebaseRepository;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.data.repository.UserRepository;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.ValidationHelper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AuthController {
    private static final long OTP_TTL_MS = 10 * 60 * 1000L;
    private static final int OTP_MAX_ATTEMPTS = 5;
    private static final String OTP_COLLECTION = "signup_otps";
    private static final String MAIL_COLLECTION = "mail";

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final UserRepository userRepository;
    private final FirebaseRepository firebaseRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthController(Context context) {
        auth = FirebaseHelper.getAuth();
        firestore = FirebaseHelper.getFirestore();
        userRepository = new UserRepository(context);
        firebaseRepository = new FirebaseRepository(context);
    }

    public boolean hasCurrentUser() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    public void login(String email, String password, RepositoryCallback<Void> callback) {
        if (!ValidationHelper.isValidEmail(email)) {
            callback.onError("Email không hợp lệ");
            return;
        }
        if (!ValidationHelper.isValidPassword(password)) {
            callback.onError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }
        auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> callback.onError("Đăng nhập thất bại: " + e.getMessage()));
    }

    public void sendSignupOtp(String email, RepositoryCallback<Void> callback) {
        if (!ValidationHelper.isValidEmail(email)) {
            callback.onError("Email không hợp lệ");
            return;
        }

        String normalizedEmail = normalizeEmail(email);
        String otp = String.format(Locale.US, "%06d", secureRandom.nextInt(1_000_000));
        long now = System.currentTimeMillis();

        Map<String, Object> otpData = new HashMap<>();
        otpData.put("email", normalizedEmail);
        otpData.put("codeHash", hashOtp(normalizedEmail, otp));
        otpData.put("expiresAt", now + OTP_TTL_MS);
        otpData.put("attempts", 0);
        otpData.put("used", false);
        otpData.put("createdAt", now);

        Map<String, Object> message = new HashMap<>();
        message.put("subject", "Mã xác thực AI Nutrition Planner");
        message.put("text", "Mã OTP của bạn là " + otp + ". Mã có hiệu lực trong 10 phút.");
        message.put("html", "<p>Mã OTP của bạn là <b>" + otp + "</b>.</p><p>Mã có hiệu lực trong 10 phút.</p>");

        Map<String, Object> mail = new HashMap<>();
        List<String> recipients = new ArrayList<>();
        recipients.add(normalizedEmail);
        mail.put("to", recipients);
        mail.put("message", message);
        mail.put("createdAt", now);

        Task<Void> otpTask = firestore.collection(OTP_COLLECTION)
                .document(otpDocumentId(normalizedEmail))
                .set(otpData, SetOptions.merge());
        Task<?> mailTask = firestore.collection(MAIL_COLLECTION).add(mail);
        Tasks.whenAll(otpTask, mailTask)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError("Cannot send OTP: " + e.getMessage()));
    }

    public void register(String fullName, String email, String password, String otp, RepositoryCallback<Void> callback) {
        if (fullName == null || fullName.trim().isEmpty()) {
            callback.onError("Họ tên là bắt buộc");
            return;
        }
        if (!ValidationHelper.isValidEmail(email)) {
            callback.onError("Email không hợp lệ");
            return;
        }
        if (!ValidationHelper.isValidPassword(password)) {
            callback.onError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }
        if (otp == null || otp.trim().length() != 6) {
            callback.onError("Vui lòng nhập mã OTP 6 chữ số");
            return;
        }

        verifySignupOtp(email, otp.trim(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                createVerifiedUser(fullName, email, password, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void loginWithGoogle(String idToken, RepositoryCallback<Void> callback) {
        if (idToken == null || idToken.trim().isEmpty()) {
            callback.onError("Chưa cấu hình Google OAuth client");
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Không thể đọc thông tin người dùng Google");
                        return;
                    }
                    saveUserProfile(firebaseUser, callback);
                })
                .addOnFailureListener(e -> callback.onError("Đăng nhập Google thất bại: " + e.getMessage()));
    }

    public void forgotPassword(String email, RepositoryCallback<Void> callback) {
        if (!ValidationHelper.isValidEmail(email)) {
            callback.onError("Email không hợp lệ");
            return;
        }
        auth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError("Không thể gửi email đặt lại mật khẩu"));
    }

    public void logout() {
        auth.signOut();
    }

    private void createVerifiedUser(String fullName, String email, String password, RepositoryCallback<Void> callback) {
        auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Cannot create user");
                        return;
                    }
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName.trim())
                            .build();
                    firebaseUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(profileTask -> saveUserProfile(firebaseUser, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    markOtpUsed(email);
                                    auth.signOut();
                                    callback.onSuccess(null);
                                }

                                @Override
                                public void onError(String message) {
                                    auth.signOut();
                                    callback.onError(message);
                                }
                            }));
                })
                .addOnFailureListener(e -> callback.onError("Registration failed: " + e.getMessage()));
    }

    private void saveUserProfile(FirebaseUser firebaseUser, RepositoryCallback<Void> callback) {
        UserEntity user = new UserEntity();
        user.userId = firebaseUser.getUid();
        user.fullName = firebaseUser.getDisplayName() == null ? "" : firebaseUser.getDisplayName();
        user.email = firebaseUser.getEmail() == null ? "" : firebaseUser.getEmail();
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
    }

    private void verifySignupOtp(String email, String otp, RepositoryCallback<Void> callback) {
        String normalizedEmail = normalizeEmail(email);
        firestore.collection(OTP_COLLECTION)
                .document(otpDocumentId(normalizedEmail))
                .get()
                .addOnSuccessListener(document -> verifyOtpDocument(normalizedEmail, otp, document, callback))
                .addOnFailureListener(e -> callback.onError("Cannot verify OTP: " + e.getMessage()));
    }

    private void verifyOtpDocument(String email, String otp, DocumentSnapshot document, RepositoryCallback<Void> callback) {
        if (!document.exists()) {
            callback.onError("Send OTP first");
            return;
        }
        Boolean used = document.getBoolean("used");
        Long expiresAt = document.getLong("expiresAt");
        Long attempts = document.getLong("attempts");
        String savedHash = document.getString("codeHash");
        if (Boolean.TRUE.equals(used)) {
            callback.onError("OTP was already used");
            return;
        }
        if (expiresAt == null || expiresAt < System.currentTimeMillis()) {
            callback.onError("OTP expired");
            return;
        }
        if (attempts != null && attempts >= OTP_MAX_ATTEMPTS) {
            callback.onError("Too many OTP attempts");
            return;
        }
        if (!hashOtp(email, otp).equals(savedHash)) {
            document.getReference().update("attempts", FieldValue.increment(1));
            callback.onError("OTP is incorrect");
            return;
        }
        callback.onSuccess(null);
    }

    private void markOtpUsed(String email) {
        firestore.collection(OTP_COLLECTION)
                .document(otpDocumentId(normalizeEmail(email)))
                .update("used", true, "usedAt", System.currentTimeMillis());
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.US);
    }

    private String otpDocumentId(String email) {
        return sha256(email);
    }

    private String hashOtp(String email, String otp) {
        return sha256(email + ":" + otp + ":ai-nutrition-planner");
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format(Locale.US, "%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
