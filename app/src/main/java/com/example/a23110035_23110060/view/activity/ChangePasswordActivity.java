package com.example.a23110035_23110060.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.controller.AuthController;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;

public class ChangePasswordActivity extends AppCompatActivity {

    private AuthController controller;
    private EditText editOldPassword, editNewPassword, editConfirmPassword;
    private View loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        controller = new AuthController(this);
        bindViews();
        setupClicks();
    }

    private void bindViews() {
        editOldPassword = findViewById(R.id.editOldPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        loadingView = findViewById(R.id.loadingView);
    }

    private void setupClicks() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_change_password).setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPass = editOldPassword.getText().toString().trim();
        String newPass = editNewPassword.getText().toString().trim();
        String confirmPass = editConfirmPassword.getText().toString().trim();

        if (oldPass.isEmpty()) {
            editOldPassword.setError("Vui lòng nhập mật khẩu cũ");
            return;
        }
        if (newPass.length() < 6) {
            editNewPassword.setError("Mật khẩu mới phải từ 6 ký tự");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            editConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        loadingView.setVisibility(View.VISIBLE);
        controller.changePassword(oldPass, newPass, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    loadingView.setVisibility(View.GONE);
                    Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    loadingView.setVisibility(View.GONE);
                    Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
