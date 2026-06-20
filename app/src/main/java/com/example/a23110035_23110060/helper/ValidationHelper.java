package com.example.a23110035_23110060.helper;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationHelper {
    private ValidationHelper() {
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static double parseDoubleOrZero(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return 0;
            }
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
