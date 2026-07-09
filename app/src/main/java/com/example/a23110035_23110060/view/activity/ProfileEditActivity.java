package com.example.a23110035_23110060.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.data.local.GoalEntity;
import com.example.a23110035_23110060.data.repository.GoalRepository;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.ValidationHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {
    private static final String PREFS = "nutrition_profile";
    private static final String[] GENDERS = {"Nam", "Nữ", "Khác"};
    private static final String[] ACTIVITY_LEVELS = {
            "Ít vận động",
            "Vận động nhẹ",
            "Vận động vừa phải",
            "Vận động nhiều"
    };

    private EditText editDisplayName;
    private EditText editEmail;
    private EditText editAge;
    private EditText editHeight;
    private EditText editWeight;
    private Spinner spinnerGender;
    private Spinner spinnerActivity;
    private TextView textBmr;
    private TextView textTdee;
    private TextView textRecommendation;
    private GoalRepository goalRepository;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        goalRepository = new GoalRepository(this);
        user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        bindViews();
        setupSpinners();
        loadProfile();
        setupClicks();
        setupLiveCalculation();
        updateProfileCalculations();
    }

    private void bindViews() {
        editDisplayName = findViewById(R.id.edit_display_name);
        editEmail = findViewById(R.id.edit_email);
        editAge = findViewById(R.id.edit_profile_age);
        editHeight = findViewById(R.id.edit_profile_height);
        editWeight = findViewById(R.id.edit_profile_weight);
        spinnerGender = findViewById(R.id.spinner_profile_gender);
        spinnerActivity = findViewById(R.id.spinner_profile_activity);
        textBmr = findViewById(R.id.text_bmr_value);
        textTdee = findViewById(R.id.text_tdee_value);
        textRecommendation = findViewById(R.id.text_profile_recommendation);
    }

    private void setupSpinners() {
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, GENDERS);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ACTIVITY_LEVELS);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(activityAdapter);
    }

    private void loadProfile() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        editEmail.setText(user.getEmail());
        editDisplayName.setText(user.getDisplayName() != null ? user.getDisplayName() : prefs.getString("displayName", ""));
        editAge.setText(prefs.getString("age", ""));
        editHeight.setText(prefs.getString("heightCm", ""));
        editWeight.setText(prefs.getString("weightKg", ""));
        spinnerGender.setSelection(prefs.getInt("genderIndex", 0));
        spinnerActivity.setSelection(prefs.getInt("activityIndex", 1));
    }

    private void setupClicks() {
        findViewById(R.id.btn_back_profile).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save_profile).setOnClickListener(v -> saveProfile());
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            FirebaseHelper.signOut();
            Intent intent = new Intent(this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setupLiveCalculation() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateProfileCalculations();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        editAge.addTextChangedListener(watcher);
        editHeight.addTextChangedListener(watcher);
        editWeight.addTextChangedListener(watcher);
        spinnerGender.setOnItemSelectedListener(new SimpleSelectionListener(this::updateProfileCalculations));
        spinnerActivity.setOnItemSelectedListener(new SimpleSelectionListener(this::updateProfileCalculations));
    }

    private void saveProfile() {
        ProfileNumbers numbers = calculateProfileNumbers();
        if (!numbers.valid) {
            Toast.makeText(this, "Vui lòng nhập tuổi, chiều cao và cân nặng trước khi lưu", Toast.LENGTH_LONG).show();
            return;
        }

        String newName = editDisplayName.getText().toString().trim();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    saveProfileLocally(newName, numbers);
                    saveProfileToFirestore(newName, numbers);
                    saveRecommendedGoals(numbers);
                });
    }

    private void saveProfileLocally(String displayName, ProfileNumbers numbers) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString("displayName", displayName)
                .putString("age", editAge.getText().toString().trim())
                .putString("heightCm", editHeight.getText().toString().trim())
                .putString("weightKg", editWeight.getText().toString().trim())
                .putInt("genderIndex", spinnerGender.getSelectedItemPosition())
                .putInt("activityIndex", spinnerActivity.getSelectedItemPosition())
                .putFloat("bmr", (float) numbers.bmr)
                .putFloat("tdee", (float) numbers.tdee)
                .apply();
    }

    private void saveProfileToFirestore(String displayName, ProfileNumbers numbers) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("fullName", displayName);
        profile.put("email", user.getEmail());
        profile.put("age", numbers.age);
        profile.put("heightCm", numbers.heightCm);
        profile.put("weightKg", numbers.weightKg);
        profile.put("gender", GENDERS[spinnerGender.getSelectedItemPosition()]);
        profile.put("activityLevel", ACTIVITY_LEVELS[spinnerActivity.getSelectedItemPosition()]);
        profile.put("bmr", numbers.bmr);
        profile.put("tdee", numbers.tdee);
        profile.put("recommendedCalories", numbers.targetCalories);
        profile.put("recommendedProtein", numbers.targetProtein);
        profile.put("recommendedCarbs", numbers.targetCarbs);
        profile.put("recommendedFat", numbers.targetFat);
        profile.put("updatedAt", System.currentTimeMillis());
        FirebaseHelper.getFirestore()
                .collection("users")
                .document(user.getUid())
                .set(profile, SetOptions.merge());
    }

    private void saveRecommendedGoals(ProfileNumbers numbers) {
        GoalEntity goal = new GoalEntity();
        goal.goalId = user.getUid() + "_goal";
        goal.userId = user.getUid();
        goal.targetCalories = numbers.targetCalories;
        goal.targetProtein = numbers.targetProtein;
        goal.targetCarbs = numbers.targetCarbs;
        goal.targetFat = numbers.targetFat;
        goalRepository.saveGoal(goal, new RepositoryCallback<GoalEntity>() {
            @Override
            public void onSuccess(GoalEntity result) {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileEditActivity.this, "Đã lưu hồ sơ và các khuyến nghị", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(ProfileEditActivity.this, message, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void updateProfileCalculations() {
        ProfileNumbers numbers = calculateProfileNumbers();
        if (!numbers.valid) {
            textBmr.setText("BMR: 0 kcal/ngày");
            textTdee.setText("TDEE: 0 kcal/ngày");
            textRecommendation.setText("Khuyến nghị sẽ xuất hiện sau khi nhập các chỉ số cơ thể.");
            return;
        }
        textBmr.setText(String.format(Locale.US, "BMR: %.0f kcal/ngày", numbers.bmr));
        textTdee.setText(String.format(Locale.US, "TDEE: %.0f kcal/ngày", numbers.tdee));
        textRecommendation.setText(String.format(Locale.US,
                "Mục tiêu khuyến nghị: %.0f kcal | Đạm %.0fg | Tinh bột %.0fg | Béo %.0fg",
                numbers.targetCalories, numbers.targetProtein, numbers.targetCarbs, numbers.targetFat));
    }

    private ProfileNumbers calculateProfileNumbers() {
        ProfileNumbers numbers = new ProfileNumbers();
        numbers.age = (int) ValidationHelper.parseDoubleOrZero(editAge.getText().toString());
        numbers.heightCm = ValidationHelper.parseDoubleOrZero(editHeight.getText().toString());
        numbers.weightKg = ValidationHelper.parseDoubleOrZero(editWeight.getText().toString());
        numbers.valid = numbers.age > 0 && numbers.heightCm > 0 && numbers.weightKg > 0;
        if (!numbers.valid) {
            return numbers;
        }

        int genderIndex = spinnerGender.getSelectedItemPosition();
        double genderOffset = genderIndex == 0 ? 5 : genderIndex == 1 ? -161 : -78;
        numbers.bmr = 10 * numbers.weightKg + 6.25 * numbers.heightCm - 5 * numbers.age + genderOffset;
        numbers.tdee = numbers.bmr * activityFactor(spinnerActivity.getSelectedItemPosition());
        numbers.targetCalories = Math.round(numbers.tdee);
        numbers.targetProtein = Math.round(numbers.weightKg * 1.6);
        numbers.targetFat = Math.round((numbers.targetCalories * 0.25) / 9.0);
        numbers.targetCarbs = Math.max(0, Math.round((numbers.targetCalories - numbers.targetProtein * 4 - numbers.targetFat * 9) / 4.0));
        return numbers;
    }

    private double activityFactor(int index) {
        if (index == 0) {
            return 1.2;
        }
        if (index == 2) {
            return 1.55;
        }
        if (index == 3) {
            return 1.725;
        }
        return 1.375;
    }

    private static class ProfileNumbers {
        boolean valid;
        int age;
        double heightCm;
        double weightKg;
        double bmr;
        double tdee;
        double targetCalories;
        double targetProtein;
        double targetCarbs;
        double targetFat;
    }

    private static class SimpleSelectionListener implements android.widget.AdapterView.OnItemSelectedListener {
        private final Runnable callback;

        SimpleSelectionListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
            callback.run();
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }
    }
}
