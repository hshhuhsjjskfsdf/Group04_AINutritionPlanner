package com.example.a23110035_23110060.view.activity;

import android.content.Intent;
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

public class SignUpActivity extends AppCompatActivity {
    private AuthController controller;
    private EditText editFullName;
    private EditText editSignupEmail;
    private EditText editSignupPassword;
    private EditText[] otpFields;
    private View otpContainer;
    private View loadingView;
    private TextView textAuthError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        controller = new AuthController(this);
        if (controller.hasCurrentUser()) {
            goToMain();
            return;
        }
        bindViews();
        setupOtpInputs();
        setupClicks();
    }

    private void bindViews() {
        editFullName = findViewById(R.id.editFullName);
        editSignupEmail = findViewById(R.id.editSignupEmail);
        editSignupPassword = findViewById(R.id.editSignupPassword);
        otpContainer = findViewById(R.id.otp_container);
        loadingView = findViewById(R.id.loadingView);
        textAuthError = findViewById(R.id.textAuthError);
        otpFields = new EditText[]{
                findViewById(R.id.et_otp_1),
                findViewById(R.id.et_otp_2),
                findViewById(R.id.et_otp_3),
                findViewById(R.id.et_otp_4),
                findViewById(R.id.et_otp_5),
                findViewById(R.id.et_otp_6)
        };
    }

    private void setupClicks() {
        Button sendOtp = findViewById(R.id.btn_send_otp);
        Button register = findViewById(R.id.btn_confirm_signup);
        View goSignin = findViewById(R.id.tv_go_signin);
        sendOtp.setOnClickListener(v -> sendOtp());
        register.setOnClickListener(v -> register());
        goSignin.setOnClickListener(v -> goToSignIn());
    }

    private void setupOtpInputs() {
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void sendOtp() {
        setLoading(true);
        controller.sendSignupOtp(text(editSignupEmail), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    setLoading(false);
                    textAuthError.setVisibility(View.GONE);
                    otpContainer.setVisibility(View.VISIBLE);
                    otpFields[0].requestFocus();
                    Toast.makeText(SignUpActivity.this, "Đã gửi mã OTP. Vui lòng kiểm tra email của bạn.", Toast.LENGTH_LONG).show();
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
        controller.register(text(editFullName), text(editSignupEmail), text(editSignupPassword), collectOtp(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(SignUpActivity.this, "Đã tạo tài khoản. Vui lòng xác thực email trước khi đăng nhập.", Toast.LENGTH_LONG).show();
                    goToSignIn();
                });
            }

            @Override
            public void onError(String message) {
                showError(message);
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

    private String text(EditText editText) {
        return editText.getText().toString().trim();
    }

    private void setLoading(boolean loading) {
        loadingView.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            textAuthError.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            setLoading(false);
            textAuthError.setText(message);
            textAuthError.setVisibility(View.VISIBLE);
            Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_LONG).show();
        });
    }

    private void goToSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
