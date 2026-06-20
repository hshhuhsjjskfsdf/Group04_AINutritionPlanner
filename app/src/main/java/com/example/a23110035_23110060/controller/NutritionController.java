package com.example.a23110035_23110060.controller;

import android.content.Context;

import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.data.local.MealPlanEntity;
import com.example.a23110035_23110060.data.repository.MealRepository;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.model.SummaryReport;

import java.util.List;

public class NutritionController {
    private final MealRepository mealRepository;

    public NutritionController(Context context) {
        mealRepository = new MealRepository(context);
    }

    public void loadMealLogs(String userId, String date, RepositoryCallback<List<MealLogEntity>> callback) {
        mealRepository.getLogsByDate(userId, date, callback);
    }

    public void deleteMealLog(MealLogEntity mealLog, RepositoryCallback<Void> callback) {
        mealRepository.deleteMealLog(mealLog, callback);
    }

    public void saveMealPlan(MealPlanEntity mealPlan, RepositoryCallback<MealPlanEntity> callback) {
        mealRepository.saveMealPlan(mealPlan, callback);
    }

    public void loadMealPlans(String userId, String date, RepositoryCallback<List<MealPlanEntity>> callback) {
        mealRepository.getPlansByDate(userId, date, callback);
    }

    public void deleteMealPlan(MealPlanEntity mealPlan, RepositoryCallback<Void> callback) {
        mealRepository.deleteMealPlan(mealPlan, callback);
    }

    public void loadSummary(String userId, RepositoryCallback<SummaryReport> callback) {
        mealRepository.getSummary(userId, callback);
    }
}
