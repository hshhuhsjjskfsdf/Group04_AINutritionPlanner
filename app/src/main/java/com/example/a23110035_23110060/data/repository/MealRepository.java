package com.example.a23110035_23110060.data.repository;

import android.content.Context;
import android.content.Intent;

import com.example.a23110035_23110060.data.local.AppDatabase;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.data.local.MealPlanEntity;
import com.example.a23110035_23110060.data.local.PendingSyncEntity;
import com.example.a23110035_23110060.helper.DateHelper;
import com.example.a23110035_23110060.helper.NetworkHelper;
import com.example.a23110035_23110060.model.SummaryReport;
import com.example.a23110035_23110060.service.FirebaseSyncService;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MealRepository {
    private final Context context;
    private final AppDatabase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MealRepository(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
    }

    public void saveMealLog(MealLogEntity mealLog, RepositoryCallback<MealLogEntity> callback) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            if (mealLog.createdAt == 0) {
                mealLog.createdAt = now;
            }
            mealLog.updatedAt = now;
            mealLog.isSynced = false;
            database.mealLogDao().insert(mealLog);
            insertPending(mealLog.userId, "CREATE_OR_UPDATE", "meal_logs", mealLog.toJson());
            triggerSyncIfOnline();
            if (callback != null) {
                callback.onSuccess(mealLog);
            }
        });
    }

    public void deleteMealLog(MealLogEntity mealLog, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            database.mealLogDao().deleteById(mealLog.mealLogId);
            insertPending(mealLog.userId, "DELETE", "meal_logs", idJson("mealLogId", mealLog.mealLogId));
            triggerSyncIfOnline();
            if (callback != null) {
                callback.onSuccess(null);
            }
        });
    }

    public void updateMealImageUrl(String mealLogId, String imageUrl, MealLogEntity mealLog, RepositoryCallback<MealLogEntity> callback) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            database.mealLogDao().updateMealImageUrl(mealLogId, imageUrl, now);
            mealLog.imageUrl = imageUrl;
            mealLog.updatedAt = now;
            mealLog.isSynced = false;
            insertPending(mealLog.userId, "CREATE_OR_UPDATE", "meal_logs", mealLog.toJson());
            triggerSyncIfOnline();
            if (callback != null) {
                callback.onSuccess(mealLog);
            }
        });
    }

    public void getLogsByDate(String userId, String date, RepositoryCallback<List<MealLogEntity>> callback) {
        executor.execute(() -> {
            List<MealLogEntity> logs = database.mealLogDao().getByUserAndDate(userId, date);
            if (callback != null) {
                callback.onSuccess(logs);
            }
        });
    }

    public void getLogsBetweenDates(String userId, String startDate, String endDate, RepositoryCallback<List<MealLogEntity>> callback) {
        executor.execute(() -> {
            List<MealLogEntity> logs = database.mealLogDao().getLogsBetweenDates(userId, startDate, endDate);
            if (callback != null) {
                callback.onSuccess(logs);
            }
        });
    }

    public void saveMealPlan(MealPlanEntity mealPlan, RepositoryCallback<MealPlanEntity> callback) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            if (mealPlan.createdAt == 0) {
                mealPlan.createdAt = now;
            }
            mealPlan.updatedAt = now;
            mealPlan.isSynced = false;
            database.mealPlanDao().insert(mealPlan);
            insertPending(mealPlan.userId, "CREATE_OR_UPDATE", "meal_plans", mealPlan.toJson());
            triggerSyncIfOnline();
            if (callback != null) {
                callback.onSuccess(mealPlan);
            }
        });
    }

    public void deleteMealPlan(MealPlanEntity mealPlan, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            database.mealPlanDao().deleteById(mealPlan.mealPlanId);
            insertPending(mealPlan.userId, "DELETE", "meal_plans", idJson("mealPlanId", mealPlan.mealPlanId));
            triggerSyncIfOnline();
            if (callback != null) {
                callback.onSuccess(null);
            }
        });
    }

    public void getPlansByDate(String userId, String date, RepositoryCallback<List<MealPlanEntity>> callback) {
        executor.execute(() -> {
            List<MealPlanEntity> plans = database.mealPlanDao().getByUserAndDate(userId, date);
            if (callback != null) {
                callback.onSuccess(plans);
            }
        });
    }

    public void updatePlanCompletion(String planId, boolean completed, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            database.mealPlanDao().updateCompletionStatus(planId, completed, now);
            MealPlanEntity plan = database.mealPlanDao().getById(planId);
            if (plan != null) {
                insertPending(plan.userId, "CREATE_OR_UPDATE", "meal_plans", plan.toJson());
                triggerSyncIfOnline();
            }
            if (callback != null) {
                callback.onSuccess(null);
            }
        });
    }

    public void getSummary(String userId, RepositoryCallback<SummaryReport> callback) {
        executor.execute(() -> {
            SummaryReport report = new SummaryReport();
            List<MealLogEntity> today = database.mealLogDao().getByUserAndDate(userId, DateHelper.today());
            List<MealLogEntity> week = database.mealLogDao().getLogsBetweenDates(userId, DateHelper.getStartOfWeek(), DateHelper.getEndOfWeek());
            Map<String, Integer> frequency = new HashMap<>();
            for (MealLogEntity log : today) {
                report.todayCalories += log.calories;
                report.todayProtein += log.protein;
                report.todayCarbs += log.carbs;
                report.todayFat += log.fat;
            }
            for (MealLogEntity log : week) {
                report.weeklyCalories += log.calories;
                frequency.put(log.foodName, frequency.getOrDefault(log.foodName, 0) + 1);
            }
            report.weeklyAverageCalories = report.weeklyCalories / 7.0;
            report.mostFrequentFood = "Chưa có dữ liệu";
            int max = 0;
            for (Map.Entry<String, Integer> entry : frequency.entrySet()) {
                if (entry.getValue() > max) {
                    max = entry.getValue();
                    report.mostFrequentFood = entry.getKey();
                }
            }
            if (callback != null) {
                callback.onSuccess(report);
            }
        });
    }

    private void insertPending(String userId, String actionType, String collectionName, String dataJson) {
        PendingSyncEntity pending = new PendingSyncEntity();
        pending.syncId = UUID.randomUUID().toString();
        pending.userId = userId;
        pending.actionType = actionType;
        pending.collectionName = collectionName;
        pending.localDataJson = dataJson;
        pending.status = "PENDING";
        pending.createdAt = System.currentTimeMillis();
        pending.updatedAt = pending.createdAt;
        database.pendingSyncDao().insert(pending);
    }

    private String idJson(String key, String value) {
        try {
            JSONObject object = new JSONObject();
            object.put(key, value);
            return object.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    private void triggerSyncIfOnline() {
        if (NetworkHelper.isNetworkAvailable(context)) {
            context.startService(new Intent(context, FirebaseSyncService.class));
        }
    }
}
