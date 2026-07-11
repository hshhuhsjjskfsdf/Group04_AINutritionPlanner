package com.example.a23110035_23110060.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.data.local.GoalEntity;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.data.local.UserEntity;
import com.example.a23110035_23110060.data.repository.FoodRepository;
import com.example.a23110035_23110060.data.repository.GoalRepository;
import com.example.a23110035_23110060.data.repository.MealRepository;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.data.repository.UserRepository;
import com.example.a23110035_23110060.helper.DailyProgressCalculator;
import com.example.a23110035_23110060.helper.DateHelper;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.NavigationHelper;
import com.example.a23110035_23110060.helper.NotificationHelper;
import com.example.a23110035_23110060.model.DailyProgress;
import com.example.a23110035_23110060.view.adapter.TodayMealAdapter;
import com.google.firebase.auth.FirebaseUser;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView textHello, textTodayDate;
    private TextView textConsumedCalories, textCaloriePercent;
    private TextView textTargetCalories, textConsumedCaloriesSmall, textRemainingCalories, labelRemaining;
    private ProgressBar progressCalories;
    
    private TextView textProteinLabel, textCarbsLabel, textFatLabel;
    private ProgressBar progressProtein, progressCarbs, progressFat;
    
    private TextView textSuggestion;
    private TextView textSeeAllMeals, textEmptyMeals;
    private RecyclerView recyclerTodayMeals;
    private View buttonSetupGoal;
    
    private View loadingView;
    private ImageView imgUserAvatar;
    
    private GoalRepository goalRepository;
    private MealRepository mealRepository;
    private FoodRepository foodRepository;
    private UserRepository userRepository;
    private TodayMealAdapter mealAdapter;
    
    private final DecimalFormat df = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (FirebaseHelper.getAuth().getCurrentUser() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        
        NotificationHelper.createNotificationChannel(this);
        goalRepository = new GoalRepository(this);
        mealRepository = new MealRepository(this);
        foodRepository = new FoodRepository(this);
        userRepository = new UserRepository(this);
        
        bindViews();
        setupRecyclerView();
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
        textTodayDate = findViewById(R.id.text_today_date);
        
        textConsumedCalories = findViewById(R.id.textConsumedCalories);
        textCaloriePercent = findViewById(R.id.textCaloriePercent);
        progressCalories = findViewById(R.id.progressCalories);
        
        textTargetCalories = findViewById(R.id.textTargetCalories);
        textConsumedCaloriesSmall = findViewById(R.id.textConsumedCaloriesSmall);
        textRemainingCalories = findViewById(R.id.textRemainingCalories);
        labelRemaining = findViewById(R.id.labelRemaining);
        
        textProteinLabel = findViewById(R.id.textProteinLabel);
        textCarbsLabel = findViewById(R.id.textCarbsLabel);
        textFatLabel = findViewById(R.id.textFatLabel);
        progressProtein = findViewById(R.id.progressProtein);
        progressCarbs = findViewById(R.id.progressCarbs);
        progressFat = findViewById(R.id.progressFat);
        
        textSuggestion = findViewById(R.id.textSuggestion);
        textSeeAllMeals = findViewById(R.id.textSeeAllMeals);
        textEmptyMeals = findViewById(R.id.textEmptyMeals);
        recyclerTodayMeals = findViewById(R.id.recyclerTodayMeals);
        buttonSetupGoal = findViewById(R.id.buttonSetupGoal);
        
        loadingView = findViewById(R.id.loadingView);
        imgUserAvatar = findViewById(R.id.img_user_avatar);
    }

    private void setupRecyclerView() {
        mealAdapter = new TodayMealAdapter();
        recyclerTodayMeals.setLayoutManager(new LinearLayoutManager(this));
        recyclerTodayMeals.setAdapter(mealAdapter);
    }

    private void setupButtons() {
        NavigationHelper.setupBottomNavigation(this, R.id.nav_home);
        
        if (imgUserAvatar != null) {
            imgUserAvatar.setOnClickListener(this::showProfileMenu);
        }
        
        textSeeAllMeals.setOnClickListener(v -> {
            Intent intent = new Intent(this, NutritionActivity.class);
            intent.putExtra(NutritionActivity.EXTRA_START_TAB, NutritionActivity.TAB_DIARY);
            startActivity(intent);
        });
        
        buttonSetupGoal.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void showProfileMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.menu_profile, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_edit_profile) {
                startActivity(new Intent(this, ProfileEditActivity.class));
                return true;
            } else if (id == R.id.menu_logout) {
                FirebaseHelper.getAuth().signOut();
                Intent intent = new Intent(this, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void seedFoods() {
        foodRepository.seedFoodsIfNeeded(new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                // Silently succeed
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadDashboard() {
        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) return;

        String name = user.getDisplayName();
        if (name == null || name.isEmpty()) name = user.getEmail();
        textHello.setText("Xin chào, " + name);
        textTodayDate.setText("Hôm nay là ngày " + DateHelper.today());
        
        userRepository.getCurrentUser(user.getUid(), new RepositoryCallback<UserEntity>() {
            @Override
            public void onSuccess(UserEntity result) {
                runOnUiThread(() -> {
                    if (result != null && result.avatarUrl != null && !result.avatarUrl.trim().isEmpty()) {
                        imgUserAvatar.setColorFilter(null);
                        Glide.with(MainActivity.this)
                                .load(result.avatarUrl)
                                .signature(new ObjectKey(result.updatedAt))
                                .placeholder(R.drawable.ic_nav_profile)
                                .error(R.drawable.ic_nav_profile)
                                .circleCrop()
                                .into(imgUserAvatar);
                    } else {
                        imgUserAvatar.setImageResource(R.drawable.ic_nav_profile);
                        imgUserAvatar.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.primary));
                    }
                });
            }

            @Override
            public void onError(String message) {}
        });

        loadingView.setVisibility(View.VISIBLE);
        goalRepository.getGoal(new RepositoryCallback<GoalEntity>() {
            @Override
            public void onSuccess(GoalEntity goal) {
                if (goal == null) {
                    runOnUiThread(() -> {
                        loadingView.setVisibility(View.GONE);
                        buttonSetupGoal.setVisibility(View.VISIBLE);
                        textSuggestion.setText("Bạn chưa thiết lập mục tiêu dinh dưỡng.");
                    });
                    return;
                }

                mealRepository.getLogsByDate(user.getUid(), DateHelper.today(), new RepositoryCallback<List<MealLogEntity>>() {
                    @Override
                    public void onSuccess(List<MealLogEntity> logs) {
                        DailyProgress progress = DailyProgressCalculator.calculate(logs, goal);
                        runOnUiThread(() -> {
                            loadingView.setVisibility(View.GONE);
                            buttonSetupGoal.setVisibility(View.GONE);
                            updateCaloriesUI(progress, goal);
                            updateMacrosUI(progress, goal);
                            updateMealsUI(logs);
                            generateTodaySuggestion(progress, goal, logs);
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

    private void updateCaloriesUI(DailyProgress progress, GoalEntity goal) {
        textConsumedCalories.setText(df.format(progress.consumedCalories));
        
        int percent = (int) (goal.targetCalories > 0 ? (progress.consumedCalories / goal.targetCalories * 100) : 0);
        textCaloriePercent.setText(percent + "%");
        progressCalories.setProgress(Math.min(100, percent));
        
        textTargetCalories.setText(df.format(goal.targetCalories) + " kcal");
        textConsumedCaloriesSmall.setText(df.format(progress.consumedCalories) + " kcal");
        
        double diff = goal.targetCalories - progress.consumedCalories;
        if (diff >= 0) {
            labelRemaining.setText("Còn lại");
            textRemainingCalories.setText(df.format(diff) + " kcal");
            textRemainingCalories.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        } else {
            labelRemaining.setText("Đã vượt");
            textRemainingCalories.setText(df.format(Math.abs(diff)) + " kcal");
            textRemainingCalories.setTextColor(ContextCompat.getColor(this, R.color.error));
        }
    }

    private void updateMacrosUI(DailyProgress progress, GoalEntity goal) {
        textProteinLabel.setText(String.format(Locale.US, "Protein %.1f / %.0f g", progress.consumedProtein, goal.targetProtein));
        progressProtein.setProgress(calculatePercent(progress.consumedProtein, goal.targetProtein));
        
        textCarbsLabel.setText(String.format(Locale.US, "Carbs %.1f / %.0f g", progress.consumedCarbs, goal.targetCarbs));
        progressCarbs.setProgress(calculatePercent(progress.consumedCarbs, goal.targetCarbs));
        
        textFatLabel.setText(String.format(Locale.US, "Fat %.1f / %.0f g", progress.consumedFat, goal.targetFat));
        progressFat.setProgress(calculatePercent(progress.consumedFat, goal.targetFat));
    }

    private int calculatePercent(double consumed, double target) {
        if (target <= 0) return 0;
        return (int) Math.min(100, (consumed / target * 100));
    }

    private void updateMealsUI(List<MealLogEntity> logs) {
        if (logs == null || logs.isEmpty()) {
            textEmptyMeals.setVisibility(View.VISIBLE);
            recyclerTodayMeals.setVisibility(View.GONE);
        } else {
            textEmptyMeals.setVisibility(View.GONE);
            recyclerTodayMeals.setVisibility(View.VISIBLE);
            mealAdapter.submitList(logs);
        }
    }

    private void generateTodaySuggestion(DailyProgress progress, GoalEntity goal, List<MealLogEntity> logs) {
        if (logs == null || logs.isEmpty()) {
            textSuggestion.setText("Hôm nay bạn chưa ghi nhận bữa ăn nào.");
            return;
        }

        if (progress.consumedCalories > goal.targetCalories) {
            double excess = progress.consumedCalories - goal.targetCalories;
            textSuggestion.setText("Bạn đã vượt mục tiêu " + df.format(excess) + " kcal hôm nay.");
            return;
        }

        if (goal.targetFat > 0 && (progress.consumedFat / goal.targetFat) >= 0.9) {
            textSuggestion.setText("Lượng chất béo hôm nay đã gần đạt giới hạn.");
            return;
        }

        if (goal.targetProtein > 0 && (goal.targetProtein - progress.consumedProtein) > 30) {
            double missing = goal.targetProtein - progress.consumedProtein;
            textSuggestion.setText("Bạn còn thiếu " + String.format(Locale.US, "%.0f", missing) + " g protein để đạt mục tiêu hôm nay.");
            return;
        }

        textSuggestion.setText("Bạn đang duy trì tiến độ dinh dưỡng tốt hôm nay.");
    }

    private void showToast(String message) {
        runOnUiThread(() -> {
            loadingView.setVisibility(View.GONE);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }
}
