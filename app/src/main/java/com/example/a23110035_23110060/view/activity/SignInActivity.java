package com.example.a23110035_23110060.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.controller.AuthController;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class SignInActivity extends AppCompatActivity {
    private static final int REQ_GOOGLE_SIGN_IN = 6101;

    private AuthController controller;
    private GoogleSignInClient googleSignInClient;
    private EditText editEmail;
    private EditText editPassword;
    private TextView textAuthError;
    private View loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        controller = new AuthController(this);
        if (controller.hasCurrentUser()) {
            goToMain();
            return;
        }
        bindViews();
        setupGoogleSignIn();
        setupClicks();
    }

    private void bindViews() {
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        textAuthError = findViewById(R.id.textAuthError);
        loadingView = findViewById(R.id.loadingView);
    }

    private void setupClicks() {
        Button login = findViewById(R.id.buttonLogin);
        Button forgot = findViewById(R.id.buttonForgot);
        Button google = findViewById(R.id.btn_google_signin);
        View goSignup = findViewById(R.id.tv_go_signup);
        login.setOnClickListener(v -> login());
        forgot.setOnClickListener(v -> forgotPassword());
        google.setOnClickListener(v -> startGoogleSignIn());
        goSignup.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }

    private void setupGoogleSignIn() {
        int webClientIdRes = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
        if (webClientIdRes == 0) {
            googleSignInClient = null;
            return;
        }
        String webClientId = getString(webClientIdRes);
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, options);
    }

    private void login() {
        setLoading(true);
        controller.login(text(editEmail), text(editPassword), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    setLoading(false);
                    textAuthError.setVisibility(View.GONE);
                    Toast.makeText(SignInActivity.this, "Đã đăng nhập", Toast.LENGTH_SHORT).show();
                    goToMain();
                });
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private void startGoogleSignIn() {
        if (googleSignInClient == null) {
            showError("Thiếu cấu hình Google OAuth trong google-services.json");
            return;
        }
        setLoading(true);
        startActivityForResult(googleSignInClient.getSignInIntent(), REQ_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_GOOGLE_SIGN_IN) {
            return;
        }
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            controller.loginWithGoogle(account.getIdToken(), new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        goToMain();
                    });
                }

                @Override
                public void onError(String message) {
                    showError(message);
                }
            });
        } catch (ApiException e) {
            String errorMsg = "Lỗi đăng nhập Google: " + e.getStatusCode();
            if (e.getStatusCode() == 10 || e.getStatusCode() == 12500) {
                errorMsg += "\n(Lỗi này thường do chưa cấu hình đúng SHA-1 trong Firebase Console)";
            }
            showError(errorMsg);
        }
    }

    private void forgotPassword() {
        setLoading(true);
        controller.forgotPassword(text(editEmail), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    setLoading(false);
                    textAuthError.setVisibility(View.GONE);
                    Toast.makeText(SignInActivity.this, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
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
            Toast.makeText(SignInActivity.this, message, Toast.LENGTH_LONG).show();
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
