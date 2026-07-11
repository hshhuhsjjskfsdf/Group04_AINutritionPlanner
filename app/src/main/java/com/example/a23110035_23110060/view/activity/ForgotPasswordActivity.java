package com.example.a23110035_23110060.view.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.controller.AuthController;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;

public class ForgotPasswordActivity extends AppCompatActivity {

    private AuthController controller;
    private EditText editEmail, editNewPassword;
    private EditText[] otpFields;
    private View otpContainer, loadingView;
    private Button btnAction;
    private TextView textTitle, textSubtitle;
    private boolean isOtpSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        controller = new AuthController(this);
        bindViews();
        setupOtpInputs();
        setupClicks();
    }

    private void bindViews() {
        editEmail = findViewById(R.id.editEmail);
        editNewPassword = findViewById(R.id.editNewPassword);
        otpContainer = findViewById(R.id.otp_container);
        loadingView = findViewById(R.id.loadingView);
        btnAction = findViewById(R.id.btn_action);
        textTitle = findViewById(R.id.textTitle);
        textSubtitle = findViewById(R.id.textSubtitle);

        otpFields = new EditText[]{
                findViewById(R.id.et_otp_1),
                findViewById(R.id.et_otp_2),
                findViewById(R.id.et_otp_3),
                findViewById(R.id.et_otp_4),
                findViewById(R.id.et_otp_5),
                findViewById(R.id.et_otp_6)
        };
    }

    private void setupOtpInputs() {
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupClicks() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnAction.setOnClickListener(v -> {
            if (!isOtpSent) {
                sendOtp();
            } else {
                resetPassword();
            }
        });
    }

    private void sendOtp() {
        String email = editEmail.getText().toString().trim();
        if (email.isEmpty()) {
            editEmail.setError("Vui lòng nhập email");
            return;
        }

        loadingView.setVisibility(View.VISIBLE);
        controller.sendForgotPasswordOtp(email, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    loadingView.setVisibility(View.GONE);
                    isOtpSent = true;
                    otpContainer.setVisibility(View.VISIBLE);
                    editEmail.setEnabled(false);
                    btnAction.setText("Đặt lại mật khẩu");
                    textTitle.setText("Xác thực OTP");
                    textSubtitle.setText("Nhập mã OTP đã được gửi đến email của bạn và mật khẩu mới.");
                    Toast.makeText(ForgotPasswordActivity.this, "Đã gửi mã OTP", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    loadingView.setVisibility(View.GONE);
                    Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void resetPassword() {
        String email = editEmail.getText().toString().trim();
        String otp = collectOtp();
        String newPass = editNewPassword.getText().toString().trim();

        if (otp.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 số OTP", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.length() < 6) {
            editNewPassword.setError("Mật khẩu phải từ 6 ký tự");
            return;
        }

        loadingView.setVisibility(View.VISIBLE);
        controller.resetPasswordWithOtp(email, otp, newPass, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    loadingView.setVisibility(View.GONE);
                    Toast.makeText(ForgotPasswordActivity.this, "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    loadingView.setVisibility(View.GONE);
                    Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String collectOtp() {
        StringBuilder builder = new StringBuilder();
        for (EditText field : otpFields) {
            builder.append(field.getText().toString().trim());
        }
        return builder.toString();
    }
}
