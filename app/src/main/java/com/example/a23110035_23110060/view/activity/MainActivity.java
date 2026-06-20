package com.example.a23110035_23110060.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.data.local.GoalEntity;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.data.repository.FoodRepository;
import com.example.a23110035_23110060.data.repository.GoalRepository;
import com.example.a23110035_23110060.data.repository.MealRepository;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.helper.DailyProgressCalculator;
import com.example.a23110035_23110060.helper.DateHelper;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.NotificationHelper;
import com.example.a23110035_23110060.model.DailyProgress;
import com.example.a23110035_23110060.service.FirebaseSyncService;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView textHello;
    private TextView textTargetCalories;
    private TextView textConsumedCalories;
    private TextView textRemainingCalories;
    private TextView textProteinProgress;
    private TextView textCarbsProgress;
    private TextView textFatProgress;
    private View loadingView;
    private GoalRepository goalRepository;
    private MealRepository mealRepository;
    private FoodRepository foodRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (FirebaseHelper.getAuth().getCurrentUser() == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        NotificationHelper.createNotificationChannel(this);
        goalRepository = new GoalRepository(this);
        mealRepository = new MealRepository(this);
        foodRepository = new FoodRepository(this);
        bindViews();
        setupButtons();
        seedFoods();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
    }

    private void bindViews() {
        textHello = findViewById(R.id.textHello);
        textTargetCalories = findViewById(R.id.textTargetCalories);
        textConsumedCalories = findViewById(R.id.textConsumedCalories);
        textRemainingCalories = findViewById(R.id.textRemainingCalories);
        textProteinProgress = findViewById(R.id.textProteinProgress);
        textCarbsProgress = findViewById(R.id.textCarbsProgress);
        textFatProgress = findViewById(R.id.textFatProgress);
        loadingView = findViewById(R.id.loadingView);
    }

    private void setupButtons() {
        Button addMeal = findViewById(R.id.buttonAddMeal);
        Button nutrition = findViewById(R.id.buttonNutrition);
        Button settings = findViewById(R.id.buttonSettings);
        Button sync = findViewById(R.id.buttonSyncNow);
        addMeal.setOnClickListener(v -> startActivity(new Intent(this, MealEntryActivity.class)));
        nutrition.setOnClickListener(v -> startActivity(new Intent(this, NutritionActivity.class)));
        settings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        sync.setOnClickListener(v -> {
            startService(new Intent(this, FirebaseSyncService.class));
            Toast.makeText(this, "Đang đồng bộ dữ liệu", Toast.LENGTH_SHORT).show();
        });
    }

    private void seedFoods() {
        foodRepository.seedFoodsIfNeeded(new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Đã sẵn sàng " + result + " món ăn", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadDashboard() {
        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            return;
        }
        textHello.setText("Xin chào, " + (user.getEmail() == null ? "bạn" : user.getEmail()));
        loadingView.setVisibility(View.VISIBLE);
        goalRepository.getGoal(new RepositoryCallback<GoalEntity>() {
            @Override
            public void onSuccess(GoalEntity goal) {
                mealRepository.getLogsByDate(user.getUid(), DateHelper.today(), new RepositoryCallback<List<MealLogEntity>>() {
                    @Override
                    public void onSuccess(List<MealLogEntity> logs) {
                        DailyProgress progress = DailyProgressCalculator.calculate(logs, goal);
                        runOnUiThread(() -> {
                            loadingView.setVisibility(View.GONE);
                            textTargetCalories.setText(String.format(Locale.US, "%.0f kcal", goal.targetCalories));
                            textConsumedCalories.setText(String.format(Locale.US, "%.0f kcal", progress.consumedCalories));
                            textRemainingCalories.setText(String.format(Locale.US, "%.0f kcal", progress.remainingCalories));
                            textProteinProgress.setText(String.format(Locale.US, "Protein %.1fg / %.0fg (%d%%)", progress.consumedProtein, goal.targetProtein, progress.proteinPercent));
                            textCarbsProgress.setText(String.format(Locale.US, "Carbs %.1fg / %.0fg (%d%%)", progress.consumedCarbs, goal.targetCarbs, progress.carbsPercent));
                            textFatProgress.setText(String.format(Locale.US, "Fat %.1fg / %.0fg (%d%%)", progress.consumedFat, goal.targetFat, progress.fatPercent));
                        });
                    }

                    @Override
                    public void onError(String message) {
                        showToast(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> {
            loadingView.setVisibility(View.GONE);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }
}
