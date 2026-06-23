package com.example.a23110035_23110060.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
    private EditText editSignupEmail;
    private EditText editSignupPassword;
    private View signInLayout;
    private View signUpLayout;
    private View otpContainer;
    private View loadingView;
    private TextView textAuthError;

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
        signInLayout = findViewById(R.id.layout_sign_in);
        signUpLayout = findViewById(R.id.layout_sign_up);
        otpContainer = findViewById(R.id.otp_container);
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editSignupEmail = findViewById(R.id.editSignupEmail);
        editSignupPassword = findViewById(R.id.editSignupPassword);
        loadingView = findViewById(R.id.loadingView);
        textAuthError = findViewById(R.id.textAuthError);
    }

    private void setupClicks() {
        Button login = findViewById(R.id.buttonLogin);
        Button register = findViewById(R.id.btn_confirm_signup);
        Button forgot = findViewById(R.id.buttonForgot);
        Button sendOtp = findViewById(R.id.btn_send_otp);
        Button googleSignIn = findViewById(R.id.btn_google_signin);
        View goSignup = findViewById(R.id.tv_go_signup);
        View goSignin = findViewById(R.id.tv_go_signin);
        login.setOnClickListener(v -> login());
        register.setOnClickListener(v -> register());
        forgot.setOnClickListener(v -> forgotPassword());
        sendOtp.setOnClickListener(v -> showOtpInputs());
        googleSignIn.setOnClickListener(v -> Toast.makeText(this, "Google Sign-In cần cấu hình OAuth client", Toast.LENGTH_SHORT).show());
        goSignup.setOnClickListener(v -> showAuthForm(signUpLayout, signInLayout));
        goSignin.setOnClickListener(v -> showAuthForm(signInLayout, signUpLayout));
    }

    private void showAuthForm(View showView, View hideView) {
        if (showView == null || hideView == null || showView.getVisibility() == View.VISIBLE) {
            return;
        }
        textAuthError.setVisibility(View.GONE);
        hideView.animate()
                .alpha(0f)
                .setDuration(120)
                .withEndAction(() -> {
                    hideView.setVisibility(View.GONE);
                    showView.setAlpha(0f);
                    showView.setVisibility(View.VISIBLE);
                    showView.animate().alpha(1f).setDuration(160).start();
                })
                .start();
    }

    private void showOtpInputs() {
        if (otpContainer != null) {
            otpContainer.setVisibility(View.VISIBLE);
        }
        Toast.makeText(this, "Kiểm tra email để nhập mã OTP", Toast.LENGTH_SHORT).show();
    }

    private void login() {
        setLoading(true);
        controller.login(text(editEmail), text(editPassword), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    setLoading(false);
                    textAuthError.setVisibility(View.GONE);
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
        controller.register(text(editFullName), text(editSignupEmail), text(editSignupPassword), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    setLoading(false);
                    textAuthError.setVisibility(View.GONE);
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
                    textAuthError.setVisibility(View.GONE);
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
            textAuthError.setText(message);
            textAuthError.setVisibility(View.VISIBLE);
            Toast.makeText(AuthActivity.this, message, Toast.LENGTH_LONG).show();
        });
    }

    private String text(EditText editText) {
        return editText.getText().toString().trim();
    }

    private void setLoading(boolean loading) {
        loadingView.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading && textAuthError != null) {
            textAuthError.setVisibility(View.GONE);
        }
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
