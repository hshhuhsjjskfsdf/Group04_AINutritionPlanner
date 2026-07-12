package com.example.a23110035_23110060.data.repository;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.a23110035_23110060.data.local.FoodEntity;
import com.example.a23110035_23110060.data.local.GoalEntity;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.data.local.MealPlanEntity;
import com.example.a23110035_23110060.data.local.PendingSyncEntity;
import com.example.a23110035_23110060.data.local.UserEntity;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.NetworkHelper;
import com.example.a23110035_23110060.service.FirebaseSyncService;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseRepository {
    private static final String TAG = "FirebaseRepository";
    private final Context context;
    private final FirebaseFirestore firestore;

    public FirebaseRepository(Context context) {
        this.context = context.getApplicationContext();
        this.firestore = FirebaseHelper.getFirestore();
    }

    public void createUserProfile(UserEntity user, RepositoryCallback<Void> callback) {
        firestore.collection("users").document(user.userId)
                .set(userMap(user), SetOptions.merge())
                .addOnSuccessListener(unused -> success(callback, null))
                .addOnFailureListener(e -> error(callback, "Không lưu được hồ sơ Firebase"));
    }

    public void getUserProfile(String userId, RepositoryCallback<UserEntity> callback) {
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        UserEntity user = new UserEntity();
                        user.userId = document.getString("userId");
                        user.fullName = document.getString("fullName");
                        user.email = document.getString("email");
                        user.avatarUrl = document.getString("avatarUrl");
                        user.age = document.getLong("age") != null ? document.getLong("age").intValue() : 0;
                        user.gender = document.getString("gender");
                        user.heightCm = document.getDouble("heightCm") != null ? document.getDouble("heightCm") : 0;
                        user.weightKg = document.getDouble("weightKg") != null ? document.getDouble("weightKg") : 0;
                        user.activityLevel = document.getString("activityLevel");
                        user.createdAt = document.getLong("createdAt") != null ? document.getLong("createdAt") : 0;
                        user.updatedAt = document.getLong("updatedAt") != null ? document.getLong("updatedAt") : 0;
                        user.breakfastReminderTime = document.getString("breakfastReminderTime");
                        user.lunchReminderTime = document.getString("lunchReminderTime");
                        user.dinnerReminderTime = document.getString("dinnerReminderTime");
                        user.snackReminderTime = document.getString("snackReminderTime");
                        success(callback, user);
                    } else {
                        error(callback, "Không tìm thấy hồ sơ người dùng");
                    }
                })
                .addOnFailureListener(e -> error(callback, "Lỗi khi lấy dữ liệu: " + e.getMessage()));
    }

    public void saveGoal(GoalEntity goal, RepositoryCallback<Void> callback) {
        firestore.collection("goals").document(goal.goalId)
                .set(goalMap(goal), SetOptions.merge())
                .addOnSuccessListener(unused -> success(callback, null))
                .addOnFailureListener(e -> error(callback, "Không đồng bộ được mục tiêu"));
    }

    public void saveMealLog(MealLogEntity mealLog, RepositoryCallback<Void> callback) {
        firestore.collection("meal_logs").document(mealLog.mealLogId)
                .set(mealLogMap(mealLog), SetOptions.merge())
                .addOnSuccessListener(unused -> success(callback, null))
                .addOnFailureListener(e -> error(callback, "Không đồng bộ được bữa ăn"));
    }

    public void saveMealPlan(MealPlanEntity mealPlan, RepositoryCallback<Void> callback) {
        firestore.collection("meal_plans").document(mealPlan.mealPlanId)
                .set(mealPlanMap(mealPlan), SetOptions.merge())
                .addOnSuccessListener(unused -> success(callback, null))
                .addOnFailureListener(e -> error(callback, "Không đồng bộ được kế hoạch"));
    }

    public void deleteMealLog(String mealLogId, RepositoryCallback<Void> callback) {
        firestore.collection("meal_logs").document(mealLogId)
                .delete()
                .addOnSuccessListener(unused -> success(callback, null))
                .addOnFailureListener(e -> error(callback, "Không xóa được bữa ăn trên Firebase"));
    }

    public void deleteMealPlan(String mealPlanId, RepositoryCallback<Void> callback) {
        firestore.collection("meal_plans").document(mealPlanId)
                .delete()
                .addOnSuccessListener(unused -> success(callback, null))
                .addOnFailureListener(e -> error(callback, "Không xóa được kế hoạch trên Firebase"));
    }

    public void uploadMealImage(String userId, String mealLogId, String imagePath, RepositoryCallback<String> callback) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            success(callback, "");
            return;
        }
        try {
            Uri uri = imagePath.startsWith("content://") || imagePath.startsWith("file://")
                    ? Uri.parse(imagePath)
                    : Uri.fromFile(new File(imagePath));
            StorageReference reference = FirebaseHelper.getStorage()
                    .getReference()
                    .child("meal_logs/" + userId + "/" + mealLogId + ".jpg");
            reference.putFile(uri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            throw task.getException();
                        }
                        return reference.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUri -> success(callback, downloadUri.toString()))
                    .addOnFailureListener(e -> error(callback, "Không tải được ảnh bữa ăn"));
        } catch (Exception e) {
            error(callback, "Đường dẫn ảnh không hợp lệ");
        }
    }

    public void uploadAvatar(String userId, String imagePath, RepositoryCallback<String> callback) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            success(callback, "");
            return;
        }
        try {
            Uri uri = imagePath.startsWith("content://") || imagePath.startsWith("file://")
                    ? Uri.parse(imagePath)
                    : Uri.fromFile(new File(imagePath));
            StorageReference reference = FirebaseHelper.getStorage()
                    .getReference()
                    .child("avatars/" + userId + "/avatar.jpg");
            reference.putFile(uri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            throw task.getException();
                        }
                        return reference.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUri -> success(callback, downloadUri.toString()))
                    .addOnFailureListener(e -> error(callback, "Không tải được ảnh đại diện"));
        } catch (Exception e) {
            error(callback, "Đường dẫn ảnh không hợp lệ");
        }
    }

    public void uploadFoodsIfNeeded(List<FoodEntity> foods) {
        if (foods == null || foods.isEmpty() || !NetworkHelper.isNetworkAvailable(context)) {
            return;
        }
        final int chunkSize = 450;
        for (int start = 0; start < foods.size(); start += chunkSize) {
            int end = Math.min(start + chunkSize, foods.size());
            WriteBatch batch = firestore.batch();
            for (FoodEntity food : foods.subList(start, end)) {
                batch.set(firestore.collection("foods").document(food.dishName), foodMap(food), SetOptions.merge());
            }
            batch.commit().addOnFailureListener(e -> Log.e(TAG, "Food upload failed", e));
        }
    }

    public void syncPendingItems() {
        if (NetworkHelper.isNetworkAvailable(context)) {
            context.startService(new Intent(context, FirebaseSyncService.class));
        }
    }

    public static Map<String, Object> userMap(UserEntity user) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.userId);
        map.put("fullName", user.fullName);
        map.put("email", user.email);
        map.put("avatarUrl", user.avatarUrl != null ? user.avatarUrl : "");
        map.put("createdAt", user.createdAt);
        map.put("updatedAt", user.updatedAt);
        map.put("age", user.age);
        map.put("gender", user.gender);
        map.put("heightCm", user.heightCm);
        map.put("weightKg", user.weightKg);
        map.put("activityLevel", user.activityLevel);
        map.put("breakfastReminderTime", user.breakfastReminderTime);
        map.put("lunchReminderTime", user.lunchReminderTime);
        map.put("dinnerReminderTime", user.dinnerReminderTime);
        map.put("snackReminderTime", user.snackReminderTime);
        return map;
    }

    public static Map<String, Object> goalMap(GoalEntity goal) {
        Map<String, Object> map = new HashMap<>();
        map.put("goalId", goal.goalId);
        map.put("userId", goal.userId);
        map.put("targetCalories", goal.targetCalories);
        map.put("targetProtein", goal.targetProtein);
        map.put("targetCarbs", goal.targetCarbs);
        map.put("targetFat", goal.targetFat);
        map.put("createdAt", goal.createdAt);
        map.put("updatedAt", goal.updatedAt);
        return map;
    }

    public static Map<String, Object> foodMap(FoodEntity food) {
        Map<String, Object> map = new HashMap<>();
        map.put("dishName", food.dishName);
        map.put("calories", food.calories);
        map.put("protein", food.protein);
        map.put("fat", food.fat);
        map.put("carbs", food.carbs);
        map.put("serving", food.serving);
        map.put("datasetSource", food.datasetSource);
        map.put("createdAt", food.createdAt);
        map.put("updatedAt", food.updatedAt);
        return map;
    }

    public static Map<String, Object> mealLogMap(MealLogEntity mealLog) {
        Map<String, Object> map = new HashMap<>();
        map.put("mealLogId", mealLog.mealLogId);
        map.put("userId", mealLog.userId);
        map.put("foodName", mealLog.foodName);
        map.put("mealType", mealLog.mealType);
        map.put("calories", mealLog.calories);
        map.put("protein", mealLog.protein);
        map.put("carbs", mealLog.carbs);
        map.put("fat", mealLog.fat);
        map.put("serving", mealLog.serving);
        map.put("ingredients", mealLog.ingredientsJson);
        map.put("imageUrl", mealLog.imageUrl);
        map.put("imagePath", mealLog.imagePath);
        map.put("source", mealLog.source);
        map.put("logDate", mealLog.logDate);
        map.put("createdAt", mealLog.createdAt);
        map.put("updatedAt", mealLog.updatedAt);
        return map;
    }

    public static Map<String, Object> mealPlanMap(MealPlanEntity mealPlan) {
        Map<String, Object> map = new HashMap<>();
        map.put("mealPlanId", mealPlan.mealPlanId);
        map.put("userId", mealPlan.userId);
        map.put("planDate", mealPlan.planDate);
        map.put("mealType", mealPlan.mealType);
        map.put("foodName", mealPlan.foodName);
        map.put("calories", mealPlan.calories);
        map.put("protein", mealPlan.protein);
        map.put("carbs", mealPlan.carbs);
        map.put("fat", mealPlan.fat);
        map.put("note", mealPlan.note);
        map.put("createdAt", mealPlan.createdAt);
        map.put("updatedAt", mealPlan.updatedAt);
        return map;
    }

    public static Map<String, Object> pendingSyncMap(PendingSyncEntity pendingSync) {
        Map<String, Object> map = new HashMap<>();
        map.put("syncId", pendingSync.syncId);
        map.put("userId", pendingSync.userId);
        map.put("actionType", pendingSync.actionType);
        map.put("collectionName", pendingSync.collectionName);
        map.put("localData", pendingSync.localDataJson);
        map.put("status", pendingSync.status);
        map.put("createdAt", pendingSync.createdAt);
        map.put("updatedAt", pendingSync.updatedAt);
        return map;
    }

    private static <T> void success(RepositoryCallback<T> callback, T result) {
        if (callback != null) {
            callback.onSuccess(result);
        }
    }

    private static void error(RepositoryCallback<?> callback, String message) {
        if (callback != null) {
            callback.onError(message);
        }
    }
}
