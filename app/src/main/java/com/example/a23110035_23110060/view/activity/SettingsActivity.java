package com.example.a23110035_23110060.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.controller.ReminderController;
import com.example.a23110035_23110060.controller.SettingsController;
import com.example.a23110035_23110060.data.local.GoalEntity;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.helper.AlarmHelper;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.NavigationHelper;
import com.example.a23110035_23110060.helper.NotificationHelper;
import com.example.a23110035_23110060.helper.ValidationHelper;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private static final int REQ_NOTIFICATION = 5001;
    private SettingsController settingsController;
    private ReminderController reminderController;
    private EditText editGoalCalories;
    private EditText editGoalProtein;
    private EditText editGoalCarbs;
    private EditText editGoalFat;
    private EditText editBreakfastTime;
    private EditText editLunchTime;
    private EditText editDinnerTime;
    private Switch switchReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settingsController = new SettingsController(this);
        reminderController = new ReminderController(this);
        NotificationHelper.createNotificationChannel(this);
        bindViews();

        loadGoal();
        loadReminderPrefs();
        setupClicks();
        NavigationHelper.setupBottomNavigation(this, R.id.nav_profile);
    }

    private void bindViews() {

        editGoalCalories = findViewById(R.id.editGoalCalories);
        editGoalProtein = findViewById(R.id.editGoalProtein);
        editGoalCarbs = findViewById(R.id.editGoalCarbs);
        editGoalFat = findViewById(R.id.editGoalFat);
        editBreakfastTime = findViewById(R.id.editBreakfastTime);
        editLunchTime = findViewById(R.id.editLunchTime);
        editDinnerTime = findViewById(R.id.editDinnerTime);
        switchReminder = findViewById(R.id.switchReminder);
    }



    private void loadGoal() {
        settingsController.loadGoal(new RepositoryCallback<GoalEntity>() {
            @Override
            public void onSuccess(GoalEntity result) {
                runOnUiThread(() -> {
                    editGoalCalories.setText(String.format(Locale.US, "%.0f", result.targetCalories));
                    editGoalProtein.setText(String.format(Locale.US, "%.0f", result.targetProtein));
                    editGoalCarbs.setText(String.format(Locale.US, "%.0f", result.targetCarbs));
                    editGoalFat.setText(String.format(Locale.US, "%.0f", result.targetFat));
                });
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void loadReminderPrefs() {
        SharedPreferences prefs = getSharedPreferences(AlarmHelper.PREFS, MODE_PRIVATE);
        switchReminder.setChecked(prefs.getBoolean(AlarmHelper.KEY_ENABLED, false));
        editBreakfastTime.setText(prefs.getString(AlarmHelper.KEY_BREAKFAST, "07:00"));
        editLunchTime.setText(prefs.getString(AlarmHelper.KEY_LUNCH, "12:00"));
        editDinnerTime.setText(prefs.getString(AlarmHelper.KEY_DINNER, "18:00"));
    }

    private void setupClicks() {
        Button saveGoal = findViewById(R.id.buttonSaveGoal);
        Button saveReminder = findViewById(R.id.buttonSaveReminder);
        View back = findViewById(R.id.buttonBackSettings);
        saveGoal.setOnClickListener(v -> saveGoal());
        saveReminder.setOnClickListener(v -> saveReminders());
        if (back != null) {
            back.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            });
        }
        
        View profileAvatar = findViewById(R.id.icon_profile_avatar);
        if (profileAvatar != null) {
            profileAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileEditActivity.class)));
        }
        View nutritionProfile = findViewById(R.id.buttonEditNutritionProfile);
        if (nutritionProfile != null) {
            nutritionProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileEditActivity.class)));
        }
    }

    private void saveGoal() {
        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) {
            showToast("Vui lòng đăng nhập lại");
            return;
        }
        GoalEntity goal = new GoalEntity();
        goal.goalId = userId + "_goal";
        goal.userId = userId;
        goal.targetCalories = ValidationHelper.parseDoubleOrZero(editGoalCalories.getText().toString());
        goal.targetProtein = ValidationHelper.parseDoubleOrZero(editGoalProtein.getText().toString());
        goal.targetCarbs = ValidationHelper.parseDoubleOrZero(editGoalCarbs.getText().toString());
        goal.targetFat = ValidationHelper.parseDoubleOrZero(editGoalFat.getText().toString());
        settingsController.saveGoal(goal, new RepositoryCallback<GoalEntity>() {
            @Override
            public void onSuccess(GoalEntity result) {
                runOnUiThread(() -> Toast.makeText(SettingsActivity.this, "Đã lưu mục tiêu dinh dưỡng", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void saveReminders() {
        if (switchReminder.isChecked()) {
            String breakfast = editBreakfastTime.getText().toString().trim();
            String lunch = editLunchTime.getText().toString().trim();
            String dinner = editDinnerTime.getText().toString().trim();
            if (!isValidTime(breakfast) || !isValidTime(lunch) || !isValidTime(dinner)) {
                showToast("Giờ nhắc nhở phải sử dụng định dạng HH:mm, ví dụ 07:30");
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATION);
                return;
            }
            reminderController.scheduleReminders(
                    breakfast,
                    lunch,
                    dinner
            );
            showToast("Đã bật nhắc bữa ăn");
        } else {
            reminderController.cancelReminders();
            showToast("Đã tắt nhắc bữa ăn");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFICATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveReminders();
        } else if (requestCode == REQ_NOTIFICATION) {
            switchReminder.setChecked(false);
            showToast("Cần cấp quyền thông báo để sử dụng tính năng nhắc nhở");
        }
    }

    private boolean isValidTime(String value) {
        if (value == null || !value.matches("^\\d{2}:\\d{2}$")) {
            return false;
        }
        try {
            String[] parts = value.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
        } catch (Exception e) {
            return false;
        }
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}
