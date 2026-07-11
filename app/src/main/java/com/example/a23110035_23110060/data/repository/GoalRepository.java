package com.example.a23110035_23110060.data.repository;

import android.content.Context;

import com.example.a23110035_23110060.data.local.AppDatabase;
import com.example.a23110035_23110060.data.local.GoalEntity;
import com.example.a23110035_23110060.data.local.UserEntity;
import com.example.a23110035_23110060.helper.DailyProgressCalculator;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.NetworkHelper;
import com.example.a23110035_23110060.helper.NutritionCalculator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoalRepository {
    private final Context context;
    private final AppDatabase database;
    private final FirebaseRepository firebaseRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public GoalRepository(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.firebaseRepository = new FirebaseRepository(context);
    }

    public void saveGoal(GoalEntity goal, RepositoryCallback<GoalEntity> callback) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            if (goal.createdAt == 0) {
                goal.createdAt = now;
            }
            goal.updatedAt = now;
            database.goalDao().insertOrUpdate(goal);
            if (NetworkHelper.isNetworkAvailable(context)) {
                firebaseRepository.saveGoal(goal, null);
            }
            if (callback != null) {
                callback.onSuccess(goal);
            }
        });
    }

    public void getGoal(RepositoryCallback<GoalEntity> callback) {
        executor.execute(() -> {
            String userId = FirebaseHelper.getCurrentUserId();
            GoalEntity goal = userId == null ? null : database.goalDao().getByUserId(userId);
            
            if (goal == null) {
                // Try to generate goal from user profile info
                UserEntity user = userId == null ? null : database.userDao().getById(userId);
                if (user != null && user.weightKg > 0 && user.heightCm > 0 && user.age > 0) {
                    double bmr = NutritionCalculator.calculateBMR(user.weightKg, user.heightCm, user.age, user.gender);
                    double tdee = NutritionCalculator.calculateTDEE(bmr, user.activityLevel);
                    
                    goal = new GoalEntity();
                    goal.userId = userId;
                    goal.goalId = userId + "_goal";
                    goal.targetCalories = Math.round(tdee);
                    goal.targetProtein = Math.round(user.weightKg * 1.5);
                    goal.targetFat = Math.round((goal.targetCalories * 0.25) / 9.0);
                    goal.targetCarbs = Math.max(0, Math.round((goal.targetCalories - goal.targetProtein * 4 - goal.targetFat * 9) / 4.0));
                } else {
                    goal = DailyProgressCalculator.defaultGoal();
                    goal.userId = userId;
                    goal.goalId = userId == null ? "default" : userId + "_goal";
                }
            }

            if (callback != null) {
                callback.onSuccess(goal);
            }
        });
    }
}
