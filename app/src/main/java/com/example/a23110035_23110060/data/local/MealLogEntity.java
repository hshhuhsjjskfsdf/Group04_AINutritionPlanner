package com.example.a23110035_23110060.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONObject;

@Entity(tableName = "meal_logs")
public class MealLogEntity {
    @PrimaryKey
    @NonNull
    public String mealLogId = "";
    public String userId;
    public String foodName;
    public String mealType;
    public double calories;
    public double protein;
    public double carbs;
    public double fat;
    public String serving;
    public String ingredientsJson;
    public String imageUrl;
    public String imagePath;
    public String source;
    public String logDate;
    public boolean isSynced;
    public long createdAt;
    public long updatedAt;

    public String toJson() {
        try {
            JSONObject object = new JSONObject();
            object.put("mealLogId", mealLogId);
            object.put("userId", userId);
            object.put("foodName", foodName);
            object.put("mealType", mealType);
            object.put("calories", calories);
            object.put("protein", protein);
            object.put("carbs", carbs);
            object.put("fat", fat);
            object.put("serving", serving);
            object.put("ingredientsJson", ingredientsJson);
            object.put("imageUrl", imageUrl);
            object.put("imagePath", imagePath);
            object.put("source", source);
            object.put("logDate", logDate);
            object.put("isSynced", isSynced);
            object.put("createdAt", createdAt);
            object.put("updatedAt", updatedAt);
            return object.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    public static MealLogEntity fromJson(String json) {
        MealLogEntity entity = new MealLogEntity();
        try {
            JSONObject object = new JSONObject(json == null ? "{}" : json);
            entity.mealLogId = object.optString("mealLogId");
            entity.userId = object.optString("userId");
            entity.foodName = object.optString("foodName");
            entity.mealType = object.optString("mealType");
            entity.calories = object.optDouble("calories");
            entity.protein = object.optDouble("protein");
            entity.carbs = object.optDouble("carbs");
            entity.fat = object.optDouble("fat");
            entity.serving = object.optString("serving");
            entity.ingredientsJson = object.optString("ingredientsJson");
            entity.imageUrl = object.optString("imageUrl");
            entity.imagePath = object.optString("imagePath");
            entity.source = object.optString("source");
            entity.logDate = object.optString("logDate");
            entity.isSynced = object.optBoolean("isSynced");
            entity.createdAt = object.optLong("createdAt");
            entity.updatedAt = object.optLong("updatedAt");
        } catch (Exception ignored) {
            entity.mealLogId = "";
        }
        return entity;
    }
}
