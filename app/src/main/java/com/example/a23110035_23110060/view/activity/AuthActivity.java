package com.example.a23110035_23110060.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.controller.AuthController;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;

public class AuthActivity extends AppCompatActivity {
    private AuthController controller;
    private EditText editFullName;
    private EditText editEmail;
    private EditText editPassword;
    private View loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        controller = new AuthController(this);
        if (controller.hasCurrentUser()) {
            goToMain();
            return;
        }
        bindViews();
        setupClicks();
    }

    private void bindViews() {
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        loadingView = findViewById(R.id.loadingView);
    }

    private void setupClicks() {
        Button login = findViewById(R.id.buttonLogin);
        Button register = findViewById(R.id.buttonRegister);
        Button forgot = findViewById(R.id.buttonForgot);
        login.setOnClickListener(v -> login());
        register.setOnClickListener(v -> register());
        forgot.setOnClickListener(v -> forgotPassword());
    }

    private void login() {
        setLoading(true);
        controller.login(text(editEmail), text(editPassword), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(AuthActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    goToMain();
                });
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private void register() {
        setLoading(true);
        controller.register(text(editFullName), text(editEmail), text(editPassword), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(AuthActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    goToMain();
                });
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private void forgotPassword() {
        setLoading(true);
        controller.forgotPassword(text(editEmail), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(AuthActivity.this, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            setLoading(false);
            Toast.makeText(AuthActivity.this, message, Toast.LENGTH_LONG).show();
        });
    }

    private String text(EditText editText) {
        return editText.getText().toString().trim();
    }

    private void setLoading(boolean loading) {
        loadingView.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
