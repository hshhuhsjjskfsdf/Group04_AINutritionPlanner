package com.example.a23110035_23110060.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.data.local.UserEntity;
import com.example.a23110035_23110060.data.repository.FirebaseRepository;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.data.repository.UserRepository;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.ImageHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileEditActivity extends AppCompatActivity {

    private EditText editDisplayName;
    private EditText editEmail;
    private ImageView imgAvatar;
    private ProgressBar progressSaving;
    private FirebaseUser user;
    private UserRepository userRepository;
    private FirebaseRepository firebaseRepository;
    private UserEntity currentUserEntity;
    private String selectedImagePath;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImagePath = ImageHelper.copyUriToCache(this, uri);
                    Glide.with(this)
                            .load(uri)
                            .circleCrop()
                            .into(imgAvatar);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        
        user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        
        userRepository = new UserRepository(this);
        firebaseRepository = new FirebaseRepository(this);
        
        bindViews();
        loadProfile();
        setupClicks();
    }

    private void bindViews() {
        editDisplayName = findViewById(R.id.edit_display_name);
        editEmail = findViewById(R.id.edit_email);
        imgAvatar = findViewById(R.id.img_profile_avatar_large);
        progressSaving = findViewById(R.id.progress_saving_profile);
    }

    private void loadProfile() {
        if (user != null) {
            editEmail.setText(user.getEmail());
            editDisplayName.setText(user.getDisplayName());
            
            // 1. Load local
            userRepository.getCurrentUser(user.getUid(), new RepositoryCallback<UserEntity>() {
                @Override
                public void onSuccess(UserEntity result) {
                    if (result != null) {
                        runOnUiThread(() -> {
                            currentUserEntity = result;
                            renderAvatar(result);
                        });
                    }
                    
                    // 2. Fetch remote to sync
                    if (com.example.a23110035_23110060.helper.NetworkHelper.isNetworkAvailable(ProfileEditActivity.this)) {
                        firebaseRepository.getUserProfile(user.getUid(), new RepositoryCallback<UserEntity>() {
                            @Override
                            public void onSuccess(UserEntity remoteUser) {
                                currentUserEntity = remoteUser;
                                userRepository.saveUserLocal(remoteUser, null);
                                runOnUiThread(() -> renderAvatar(remoteUser));
                            }
                            @Override
                            public void onError(String message) {}
                        });
                    }
                }
                @Override
                public void onError(String message) { }
            });
        }
    }

    private void renderAvatar(UserEntity result) {
        if (result != null && result.avatarUrl != null && !result.avatarUrl.trim().isEmpty()) {
            imgAvatar.setColorFilter(null);
            Glide.with(ProfileEditActivity.this)
                    .load(result.avatarUrl)
                    .signature(new ObjectKey(result.updatedAt))
                    .placeholder(R.drawable.ic_nav_profile)
                    .error(R.drawable.ic_nav_profile)
                    .circleCrop()
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_nav_profile);
            imgAvatar.setColorFilter(ContextCompat.getColor(ProfileEditActivity.this, R.color.primary));
        }
    }

    private void setupClicks() {
        findViewById(R.id.btn_back_profile).setOnClickListener(v -> finish());
        
        findViewById(R.id.btn_save_account).setOnClickListener(v -> saveAccountInfo());
        
        View btnChangeAvatar = findViewById(R.id.btn_change_avatar_text);
        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        }
        imgAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        
        findViewById(R.id.btn_change_password).setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });

        findViewById(R.id.btn_logout_account).setOnClickListener(v -> {
            FirebaseHelper.getAuth().signOut();
            Intent intent = new Intent(this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void saveAccountInfo() {
        String name = editDisplayName.getText().toString().trim();
        if (name.isEmpty()) {
            editDisplayName.setError("Họ tên không được để trống");
            return;
        }

        findViewById(R.id.btn_save_account).setEnabled(false);
        progressSaving.setVisibility(View.VISIBLE);

        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            firebaseRepository.uploadAvatar(user.getUid(), selectedImagePath, new RepositoryCallback<String>() {
                @Override
                public void onSuccess(String url) {
                    updateProfileWithDetails(name, url);
                }

                @Override
                public void onError(String message) {
                    updateProfileWithDetails(name, null);
                }
            });
        } else {
            updateProfileWithDetails(name, null);
        }
    }

    private void updateProfileWithDetails(String name, String avatarUrl) {
        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                .setDisplayName(name);
        if (avatarUrl != null) {
            builder.setPhotoUri(Uri.parse(avatarUrl));
        }
        
        UserProfileChangeRequest profileUpdates = builder.build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (currentUserEntity != null) {
                            currentUserEntity.fullName = name;
                            if (avatarUrl != null) {
                                currentUserEntity.avatarUrl = avatarUrl;
                            }
                            saveUserEntityAndFinish(true);
                        } else {
                            finishSaving("Đã cập nhật tài khoản");
                        }
                    } else {
                        finishSaving("Lỗi: " + task.getException().getMessage());
                    }
                });
    }

    private void saveUserEntityAndFinish(boolean avatarUploaded) {
        long now = System.currentTimeMillis();
        currentUserEntity.updatedAt = now;
        userRepository.updateUser(currentUserEntity, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                firebaseRepository.createUserProfile(currentUserEntity, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void res) {
                        if (avatarUploaded) {
                            finishSaving("Đã lưu tài khoản thành công");
                        } else {
                            finishSaving("Thông tin đã lưu nhưng ảnh đại diện chưa được cập nhật.");
                        }
                    }

                    @Override
                    public void onError(String message) {
                        finishSaving("Lỗi đồng bộ hồ sơ: " + message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                finishSaving("Lỗi lưu hồ sơ cục bộ: " + message);
            }
        });
    }

    private void finishSaving(String message) {
        runOnUiThread(() -> {
            progressSaving.setVisibility(View.GONE);
            findViewById(R.id.btn_save_account).setEnabled(true);
            Toast.makeText(ProfileEditActivity.this, message, Toast.LENGTH_SHORT).show();
        });
    }
}
