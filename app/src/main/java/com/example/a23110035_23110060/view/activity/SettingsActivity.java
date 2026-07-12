package com.example.a23110035_23110060.view.activity;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.controller.ReminderController;
import com.example.a23110035_23110060.controller.SettingsController;
import com.example.a23110035_23110060.data.local.GoalEntity;
import com.example.a23110035_23110060.data.local.UserEntity;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.helper.AlarmHelper;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.NavigationHelper;
import com.example.a23110035_23110060.helper.NutritionCalculator;
import com.example.a23110035_23110060.helper.ValidationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.example.a23110035_23110060.helper.NetworkHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private static final int REQ_NOTIFICATION = 5001;
    private static final String[] ACTIVITY_LEVELS = {
            "Ít vận động",
            "Vận động nhẹ",
            "Vận động vừa",
            "Vận động nhiều",
            "Vận động rất nhiều"
    };

    private SettingsController settingsController;
    private ReminderController reminderController;

    private TextView textValueAge, textValueGender, textValueHeight, textValueWeight, textValueActivity;
    private TextView textBMIValue, textBMICategory, textBMRValue, textTDEEValue, textMissingInfoWarning;
    private TextView textGoalCalories, textGoalProtein, textGoalCarbs, textGoalFat;
    private TextView textNoGoal;
    private ImageView imgSettingsAvatar;
    private View layoutGoalValues;
    private MaterialButton btnEditGoal, btnSetupGoalNow, btnSuggestGoal, btnRetrySync;
    private TextView textTimeBreakfast, textTimeLunch, textTimeDinner, textTimeSnack, textSyncStatus;
    private SwitchMaterial switchReminders, switchAutoSync;

    private UserEntity currentUser;
    private GoalEntity currentGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        settingsController = new SettingsController(this);
        reminderController = new ReminderController(this);
        
        initViews();
        setupNavigation();
        loadData();
    }

    private void initViews() {
        textValueAge = findViewById(R.id.textValueAge);
        textValueGender = findViewById(R.id.textValueGender);
        textValueHeight = findViewById(R.id.textValueHeight);
        textValueWeight = findViewById(R.id.textValueWeight);
        textValueActivity = findViewById(R.id.textValueActivity);

        textBMIValue = findViewById(R.id.textBMIValue);
        textBMICategory = findViewById(R.id.textBMICategory);
        textBMRValue = findViewById(R.id.textBMRValue);
        textTDEEValue = findViewById(R.id.textTDEEValue);
        textMissingInfoWarning = findViewById(R.id.textMissingInfoWarning);

        textGoalCalories = findViewById(R.id.textGoalCalories);
        textGoalProtein = findViewById(R.id.textGoalProtein);
        textGoalCarbs = findViewById(R.id.textGoalCarbs);
        textGoalFat = findViewById(R.id.textGoalFat);
        textNoGoal = findViewById(R.id.textNoGoal);
        imgSettingsAvatar = findViewById(R.id.img_settings_avatar);
        layoutGoalValues = findViewById(R.id.layoutGoalValues);

        textTimeBreakfast = findViewById(R.id.textTimeBreakfast);
        textTimeLunch = findViewById(R.id.textTimeLunch);
        textTimeDinner = findViewById(R.id.textTimeDinner);
        textTimeSnack = findViewById(R.id.textTimeSnack);
        textSyncStatus = findViewById(R.id.textSyncStatus);

        switchReminders = findViewById(R.id.switchReminders);
        switchAutoSync = findViewById(R.id.switchAutoSync);

        findViewById(R.id.btnEditBodyInfo).setOnClickListener(v -> showEditBodyInfoDialog());
        btnEditGoal = findViewById(R.id.btnEditGoal);
        btnSetupGoalNow = findViewById(R.id.btnSetupGoalNow);
        btnSuggestGoal = findViewById(R.id.btnSuggestGoal);

        btnEditGoal.setOnClickListener(v -> showEditGoalDialog());
        btnSetupGoalNow.setOnClickListener(v -> showEditGoalDialog());
        btnSuggestGoal.setOnClickListener(v -> showSuggestGoalDialog());
        btnRetrySync = findViewById(R.id.btnRetrySync);
        btnRetrySync.setOnClickListener(v -> {
            loadData();
            showToast("Đang đồng bộ lại...");
        });

        findViewById(R.id.btnPickBreakfast).setOnClickListener(v -> pickTime("Bữa sáng", textTimeBreakfast, AlarmHelper.KEY_BREAKFAST));
        findViewById(R.id.btnPickLunch).setOnClickListener(v -> pickTime("Bữa trưa", textTimeLunch, AlarmHelper.KEY_LUNCH));
        findViewById(R.id.btnPickDinner).setOnClickListener(v -> pickTime("Bữa tối", textTimeDinner, AlarmHelper.KEY_DINNER));
        findViewById(R.id.btnPickSnack).setOnClickListener(v -> pickTime("Bữa phụ", textTimeSnack, AlarmHelper.KEY_SNACK));

        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> toggleReminders(isChecked));
    }

    private void setupNavigation() {
        NavigationHelper.setupBottomNavigation(this, R.id.nav_profile);
    }

    private void loadData() {
        settingsController.loadUser(new RepositoryCallback<UserEntity>() {
            @Override
            public void onSuccess(UserEntity user) {
                currentUser = user;
                runOnUiThread(() -> {
                    renderUser(user);
                    renderSyncStatus();
                });
            }
            @Override
            public void onError(String message) { showToast(message); }
        });

        settingsController.loadGoal(new RepositoryCallback<GoalEntity>() {
            @Override
            public void onSuccess(GoalEntity goal) {
                currentGoal = goal;
                runOnUiThread(() -> renderGoal(goal));
            }
            @Override
            public void onError(String message) { showToast(message); }
        });

        loadReminderPrefs();
    }

    private void renderSyncStatus() {
        if (!NetworkHelper.isNetworkAvailable(this)) {
            textSyncStatus.setText("Chưa có kết nối mạng");
            textSyncStatus.setTextColor(getColor(R.color.error));
            btnRetrySync.setVisibility(View.VISIBLE);
            return;
        }
        
        textSyncStatus.setText("Đã đồng bộ");
        textSyncStatus.setTextColor(getColor(R.color.text_secondary));
        btnRetrySync.setVisibility(View.GONE);
    }

    private void renderUser(UserEntity user) {
        if (user == null) {
            textValueAge.setText("-- tuổi");
            textValueGender.setText("--");
            textValueHeight.setText("-- cm");
            textValueWeight.setText("-- kg");
            textValueActivity.setText("--");
            
            textBMIValue.setText("--");
            textBMICategory.setText("--");
            textBMRValue.setText("-- kcal");
            textTDEEValue.setText("-- kcal");
            textMissingInfoWarning.setVisibility(View.VISIBLE);
            return;
        }

        // Render avatar
        if (imgSettingsAvatar != null) {
            if (user.avatarUrl != null && !user.avatarUrl.trim().isEmpty()) {
                imgSettingsAvatar.setColorFilter(null);
                Glide.with(this)
                        .load(user.avatarUrl)
                        .signature(new ObjectKey(user.updatedAt))
                        .placeholder(R.drawable.ic_nav_profile)
                        .error(R.drawable.ic_nav_profile)
                        .circleCrop()
                        .into(imgSettingsAvatar);
            } else {
                imgSettingsAvatar.setImageResource(R.drawable.ic_nav_profile);
                imgSettingsAvatar.setColorFilter(getColor(R.color.primary));
            }
        }
        
        textValueAge.setText(user.age > 0 ? user.age + " tuổi" : "-- tuổi");
        textValueGender.setText(user.gender != null ? user.gender : "--");
        textValueHeight.setText(user.heightCm > 0 ? String.format(Locale.US, "%.0f cm", user.heightCm) : "-- cm");
        textValueWeight.setText(user.weightKg > 0 ? String.format(Locale.US, "%.1f kg", user.weightKg) : "-- kg");
        textValueActivity.setText(user.activityLevel != null ? user.activityLevel : "--");

        if (user.breakfastReminderTime != null) {
            textTimeBreakfast.setText(user.breakfastReminderTime);
        }
        if (user.lunchReminderTime != null) {
            textTimeLunch.setText(user.lunchReminderTime);
        }
        if (user.dinnerReminderTime != null) {
            textTimeDinner.setText(user.dinnerReminderTime);
        }
        if (user.snackReminderTime != null) {
            textTimeSnack.setText(user.snackReminderTime);
        }

        if (user.age > 0 && user.heightCm > 0 && user.weightKg > 0 && user.gender != null) {
            double bmi = NutritionCalculator.calculateBMI(user.weightKg, user.heightCm);
            textBMIValue.setText(String.format(Locale.US, "%.1f", bmi));
            textBMICategory.setText(NutritionCalculator.getBMICategory(bmi));

            double bmr = NutritionCalculator.calculateBMR(user.weightKg, user.heightCm, user.age, user.gender);
            textBMRValue.setText(String.format(Locale.US, "%.0f kcal/ngày", bmr));

            double tdee = NutritionCalculator.calculateTDEE(bmr, user.activityLevel);
            textTDEEValue.setText(String.format(Locale.US, "%.0f kcal/ngày", tdee));

            textBMIValue.setVisibility(View.VISIBLE);
            textBMICategory.setVisibility(View.VISIBLE);
            textBMRValue.setVisibility(View.VISIBLE);
            textTDEEValue.setVisibility(View.VISIBLE);
            textMissingInfoWarning.setVisibility(View.GONE);
        } else {
            textBMIValue.setText("--");
            textBMICategory.setText("--");
            textBMRValue.setText("-- kcal");
            textTDEEValue.setText("-- kcal");
            textMissingInfoWarning.setVisibility(View.VISIBLE);
        }
    }

    private void renderGoal(GoalEntity goal) {
        if (goal == null || goal.targetCalories <= 0) {
            layoutGoalValues.setVisibility(View.GONE);
            textNoGoal.setVisibility(View.VISIBLE);
            btnEditGoal.setVisibility(View.GONE);
            btnSetupGoalNow.setVisibility(View.VISIBLE);
            return;
        }
        
        layoutGoalValues.setVisibility(View.VISIBLE);
        textNoGoal.setVisibility(View.GONE);
        btnEditGoal.setVisibility(View.VISIBLE);
        btnSetupGoalNow.setVisibility(View.GONE);

        textGoalCalories.setText(String.format(Locale.US, "%.0f kcal/ngày", goal.targetCalories));
        textGoalProtein.setText(String.format(Locale.US, "%.0f g/ngày", goal.targetProtein));
        textGoalCarbs.setText(String.format(Locale.US, "%.0f g/ngày", goal.targetCarbs));
        textGoalFat.setText(String.format(Locale.US, "%.0f g/ngày", goal.targetFat));
    }

    private void loadReminderPrefs() {
        SharedPreferences prefs = getSharedPreferences(AlarmHelper.PREFS, MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(AlarmHelper.KEY_ENABLED, false);
        switchReminders.setChecked(enabled);
        textTimeBreakfast.setText(prefs.getString(AlarmHelper.KEY_BREAKFAST, "07:00"));
        textTimeLunch.setText(prefs.getString(AlarmHelper.KEY_LUNCH, "12:00"));
        textTimeDinner.setText(prefs.getString(AlarmHelper.KEY_DINNER, "18:30"));
        textTimeSnack.setText(prefs.getString(AlarmHelper.KEY_SNACK, "22:00"));
    }

    private void showEditBodyInfoDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_body_info, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        com.google.android.material.textfield.TextInputLayout layoutAge = view.findViewById(R.id.layoutAge);
        com.google.android.material.textfield.TextInputLayout layoutHeight = view.findViewById(R.id.layoutHeight);
        com.google.android.material.textfield.TextInputLayout layoutWeight = view.findViewById(R.id.layoutWeight);
        TextInputEditText editAge = view.findViewById(R.id.editAge);
        TextInputEditText editHeight = view.findViewById(R.id.editHeight);
        TextInputEditText editWeight = view.findViewById(R.id.editWeight);
        RadioGroup radioGroupGender = view.findViewById(R.id.radioGroupGender);
        Spinner spinnerActivity = view.findViewById(R.id.spinnerActivity);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ACTIVITY_LEVELS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(adapter);

        if (currentUser != null) {
            if (currentUser.age > 0) editAge.setText(String.valueOf(currentUser.age));
            if (currentUser.heightCm > 0) editHeight.setText(String.valueOf(currentUser.heightCm));
            if (currentUser.weightKg > 0) editWeight.setText(String.valueOf(currentUser.weightKg));
            if ("Nữ".equalsIgnoreCase(currentUser.gender)) radioGroupGender.check(R.id.radioFemale);
            else radioGroupGender.check(R.id.radioMale);
            
            for (int i = 0; i < ACTIVITY_LEVELS.length; i++) {
                if (ACTIVITY_LEVELS[i].equalsIgnoreCase(currentUser.activityLevel)) {
                    spinnerActivity.setSelection(i);
                    break;
                }
            }
        }

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            layoutAge.setError(null);
            layoutHeight.setError(null);
            layoutWeight.setError(null);

            String ageStr = editAge.getText().toString();
            String heightStr = editHeight.getText().toString();
            String weightStr = editWeight.getText().toString();

            if (ageStr.isEmpty()) { layoutAge.setError("Tuổi không được để trống"); return; }
            if (heightStr.isEmpty()) { layoutHeight.setError("Chiều cao không được để trống"); return; }
            if (weightStr.isEmpty()) { layoutWeight.setError("Cân nặng không được để trống"); return; }

            int age = (int) ValidationHelper.parseDoubleOrZero(ageStr);
            double height = ValidationHelper.parseDoubleOrZero(heightStr);
            double weight = ValidationHelper.parseDoubleOrZero(weightStr);
            String gender = radioGroupGender.getCheckedRadioButtonId() == R.id.radioFemale ? "Nữ" : "Nam";
            String activity = ACTIVITY_LEVELS[spinnerActivity.getSelectedItemPosition()];

            if (age < 10 || age > 120) { layoutAge.setError("Tuổi phải trong khoảng 10-120"); return; }
            if (height < 50 || height > 250) { layoutHeight.setError("Chiều cao phải trong khoảng 50-250"); return; }
            if (weight < 10 || weight > 500) { layoutWeight.setError("Cân nặng phải trong khoảng 10-500"); return; }

            UserEntity user = currentUser != null ? currentUser : new UserEntity();
            com.google.firebase.auth.FirebaseUser fbUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser != null) {
                user.userId = fbUser.getUid();
                if (user.email == null || user.email.isEmpty()) {
                    user.email = fbUser.getEmail() != null ? fbUser.getEmail() : "";
                }
                if (user.fullName == null || user.fullName.isEmpty()) {
                    user.fullName = fbUser.getDisplayName() != null ? fbUser.getDisplayName() : "";
                }
                if (user.createdAt == 0) {
                    user.createdAt = System.currentTimeMillis();
                }
            } else if (user.userId == null || user.userId.isEmpty()) {
                user.userId = FirebaseHelper.getCurrentUserId();
            }
            user.age = age;
            user.gender = gender;
            user.heightCm = height;
            user.weightKg = weight;
            user.activityLevel = activity;
            user.updatedAt = System.currentTimeMillis();

            settingsController.saveUser(user, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        currentUser = user;
                        renderUser(user);
                        showToast("Đã cập nhật thông tin");
                    });
                }
                @Override
                public void onError(String message) { showToast(message); }
            });
        });

        dialog.show();
    }

    private void showEditGoalDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_goal, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        TextInputEditText editCal = view.findViewById(R.id.editGoalCalories);
        TextInputEditText editProt = view.findViewById(R.id.editGoalProtein);
        TextInputEditText editCarb = view.findViewById(R.id.editGoalCarbs);
        TextInputEditText editFat = view.findViewById(R.id.editGoalFat);

        if (currentGoal != null) {
            editCal.setText(String.format(Locale.US, "%.0f", currentGoal.targetCalories));
            editProt.setText(String.format(Locale.US, "%.0f", currentGoal.targetProtein));
            editCarb.setText(String.format(Locale.US, "%.0f", currentGoal.targetCarbs));
            editFat.setText(String.format(Locale.US, "%.0f", currentGoal.targetFat));
        }

        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            GoalEntity goal = currentGoal != null ? currentGoal : new GoalEntity();
            goal.userId = FirebaseHelper.getCurrentUserId();
            goal.goalId = goal.userId + "_goal";
            goal.targetCalories = ValidationHelper.parseDoubleOrZero(editCal.getText().toString());
            goal.targetProtein = ValidationHelper.parseDoubleOrZero(editProt.getText().toString());
            goal.targetCarbs = ValidationHelper.parseDoubleOrZero(editCarb.getText().toString());
            goal.targetFat = ValidationHelper.parseDoubleOrZero(editFat.getText().toString());
            goal.updatedAt = System.currentTimeMillis();

            settingsController.saveGoal(goal, new RepositoryCallback<GoalEntity>() {
                @Override
                public void onSuccess(GoalEntity result) {
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        currentGoal = result;
                        renderGoal(result);
                        showToast("Đã lưu mục tiêu");
                    });
                }
                @Override
                public void onError(String message) { showToast(message); }
            });
        });

        dialog.show();
    }

    private void showSuggestGoalDialog() {
        if (currentUser == null || currentUser.weightKg <= 0) {
            showToast("Hãy cập nhật thông tin cơ thể trước khi tính mục tiêu gợi ý.");
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_suggest_goal, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        MaterialButtonToggleGroup toggle = view.findViewById(R.id.toggleGoalType);
        TextView resultText = view.findViewById(R.id.textSuggestedValue);
        View cardResult = view.findViewById(R.id.cardResult);
        MaterialButton btnApply = view.findViewById(R.id.btnApply);

        final double[] suggested = new double[4]; // cal, p, c, f

        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            
            double bmr = NutritionCalculator.calculateBMR(currentUser.weightKg, currentUser.heightCm, currentUser.age, currentUser.gender);
            double tdee = NutritionCalculator.calculateTDEE(bmr, currentUser.activityLevel);
            double cal;
            
            if (checkedId == R.id.btnLose) cal = tdee - 400;
            else if (checkedId == R.id.btnGain) cal = tdee + 300;
            else cal = tdee;

            suggested[0] = Math.round(cal);
            suggested[1] = Math.round(currentUser.weightKg * 1.5); // Protein
            suggested[3] = Math.round((cal * 0.25) / 9.0); // Fat 25%
            suggested[2] = Math.max(0, Math.round((cal - suggested[1] * 4 - suggested[3] * 9) / 4.0)); // Carbs

            resultText.setText(String.format(Locale.US, "Mục tiêu gợi ý: %.0f kcal/ngày", suggested[0]));
            cardResult.setVisibility(View.VISIBLE);
            btnApply.setEnabled(true);
        });

        btnApply.setOnClickListener(v -> {
            GoalEntity goal = currentGoal != null ? currentGoal : new GoalEntity();
            goal.userId = FirebaseHelper.getCurrentUserId();
            goal.goalId = goal.userId + "_goal";
            goal.targetCalories = suggested[0];
            goal.targetProtein = suggested[1];
            goal.targetCarbs = suggested[2];
            goal.targetFat = suggested[3];
            goal.updatedAt = System.currentTimeMillis();

            settingsController.saveGoal(goal, new RepositoryCallback<GoalEntity>() {
                @Override
                public void onSuccess(GoalEntity result) {
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        currentGoal = result;
                        renderGoal(result);
                        showToast("Đã áp dụng mục tiêu gợi ý");
                    });
                }
                @Override
                public void onError(String message) { showToast(message); }
            });
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void pickTime(String title, TextView targetView, String prefKey) {
        String current = targetView.getText().toString();
        int hour = 7, min = 0;
        try {
            String[] parts = current.split(":");
            hour = Integer.parseInt(parts[0]);
            min = Integer.parseInt(parts[1]);
        } catch (Exception ignored) {}

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(min)
                .setTitleText("Chọn giờ " + title)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int h = picker.getHour();
            int m = picker.getMinute();

            // Validate time range
            boolean isValid = false;
            String rangeMsg = "";
            if (AlarmHelper.KEY_BREAKFAST.equals(prefKey)) {
                if (h >= 5 && h <= 9) isValid = true;
                rangeMsg = "Bữa sáng: 05:00 - 09:59";
            } else if (AlarmHelper.KEY_LUNCH.equals(prefKey)) {
                if (h >= 10 && h <= 14) isValid = true;
                rangeMsg = "Bữa trưa: 10:00 - 14:59";
            } else if (AlarmHelper.KEY_DINNER.equals(prefKey)) {
                if (h >= 15 && h <= 20) isValid = true;
                rangeMsg = "Bữa tối: 15:00 - 20:59";
            } else if (AlarmHelper.KEY_SNACK.equals(prefKey)) {
                if (h >= 21 || h <= 4) isValid = true;
                rangeMsg = "Bữa phụ: 21:00 - 04:59";
            }

            if (!isValid) {
                showToast("Thời gian không hợp lệ. Khung giờ " + rangeMsg);
                return;
            }

            String time = String.format(Locale.US, "%02d:%02d", h, m);
            targetView.setText(time);
            getSharedPreferences(AlarmHelper.PREFS, MODE_PRIVATE).edit()
                    .putString(prefKey, time).apply();

            if (currentUser != null) {
                if (AlarmHelper.KEY_BREAKFAST.equals(prefKey)) currentUser.breakfastReminderTime = time;
                else if (AlarmHelper.KEY_LUNCH.equals(prefKey)) currentUser.lunchReminderTime = time;
                else if (AlarmHelper.KEY_DINNER.equals(prefKey)) currentUser.dinnerReminderTime = time;
                else if (AlarmHelper.KEY_SNACK.equals(prefKey)) currentUser.snackReminderTime = time;

                settingsController.saveUser(currentUser, null);
            }

            if (switchReminders.isChecked()) {
                updateReminders();
            }
        });

        picker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    private void toggleReminders(boolean enabled) {
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATION);
            } else {
                updateReminders();
                showToast("Đã bật nhắc nhở");
            }
        } else {
            reminderController.cancelReminders();
            showToast("Đã tắt nhắc nhở");
        }
    }

    private void updateReminders() {
        reminderController.scheduleReminders(
                textTimeBreakfast.getText().toString(),
                textTimeLunch.getText().toString(),
                textTimeDinner.getText().toString(),
                textTimeSnack.getText().toString()
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFICATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateReminders();
            } else {
                switchReminders.setChecked(false);
                showToast("Cần quyền thông báo để nhắc nhở");
            }
        }
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}
