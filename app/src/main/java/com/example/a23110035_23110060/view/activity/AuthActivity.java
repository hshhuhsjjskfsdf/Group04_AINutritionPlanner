package com.example.a23110035_23110060.view.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a23110035_23110060.controller.AuthController;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        boolean rememberMe = getSharedPreferences("auth_prefs", MODE_PRIVATE).getBoolean("remember_me", true);
        AuthController controller = new AuthController(this);
        
        Class<?> destination;
        if (rememberMe && controller.hasCurrentUser()) {
            destination = MainActivity.class;
        } else {
            // Nếu không chọn "Ghi nhớ", hãy đăng xuất để đảm bảo an toàn
            if (!rememberMe) {
                controller.logout();
            }
            destination = SignInActivity.class;
        }

        Intent intent = new Intent(this, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
