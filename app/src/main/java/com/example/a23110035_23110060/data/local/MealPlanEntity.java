package com.example.a23110035_23110060.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONObject;

@Entity(tableName = "meal_plans")
public class MealPlanEntity {
    @PrimaryKey
    @NonNull
    public String mealPlanId = "";
    public String userId;
    public String planDate;
    public String mealType;
    public String foodName;
    public String portion;
    public double calories;
    public double protein;
    public double carbs;
    public double fat;
    public String note;
    public boolean isCompleted;
    public boolean isSynced;
    public long createdAt;
    public long updatedAt;

    public String toJson() {
        try {
            JSONObject object = new JSONObject();
            object.put("mealPlanId", mealPlanId);
            object.put("userId", userId);
            object.put("planDate", planDate);
            object.put("mealType", mealType);
            object.put("foodName", foodName);
            object.put("portion", portion);
            object.put("calories", calories);
            object.put("protein", protein);
            object.put("carbs", carbs);
            object.put("fat", fat);
            object.put("note", note);
            object.put("isCompleted", isCompleted);
            object.put("isSynced", isSynced);
            object.put("createdAt", createdAt);
            object.put("updatedAt", updatedAt);
            return object.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    public static MealPlanEntity fromJson(String json) {
        MealPlanEntity entity = new MealPlanEntity();
        try {
            JSONObject object = new JSONObject(json == null ? "{}" : json);
            entity.mealPlanId = object.optString("mealPlanId");
            entity.userId = object.optString("userId");
            entity.planDate = object.optString("planDate");
            entity.mealType = object.optString("mealType");
            entity.foodName = object.optString("foodName");
            entity.portion = object.optString("portion");
            entity.calories = object.optDouble("calories");
            entity.protein = object.optDouble("protein");
            entity.carbs = object.optDouble("carbs");
            entity.fat = object.optDouble("fat");
            entity.note = object.optString("note");
            entity.isCompleted = object.optBoolean("isCompleted");
            entity.isSynced = object.optBoolean("isSynced");
            entity.createdAt = object.optLong("createdAt");
            entity.updatedAt = object.optLong("updatedAt");
        } catch (Exception ignored) {
            entity.mealPlanId = "";
        }
        return entity;
    }
}
