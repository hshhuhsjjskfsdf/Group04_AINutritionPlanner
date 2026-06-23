package com.example.a23110035_23110060.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileEditActivity extends AppCompatActivity {

    private EditText editDisplayName;
    private EditText editEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        editDisplayName = findViewById(R.id.edit_display_name);
        editEmail = findViewById(R.id.edit_email);

        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user != null) {
            editEmail.setText(user.getEmail());
            editDisplayName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
        } else {
            finish();
            return;
        }

        findViewById(R.id.btn_back_profile).setOnClickListener(v -> finish());
        
        findViewById(R.id.btn_save_profile).setOnClickListener(v -> saveProfile());

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            FirebaseHelper.signOut();
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void saveProfile() {
        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) return;

        String newName = editDisplayName.getText().toString().trim();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đã cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
