package com.example.a23110035_23110060.controller;

import android.content.Context;

import com.example.a23110035_23110060.data.local.FoodEntity;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.data.repository.FirebaseRepository;
import com.example.a23110035_23110060.data.repository.FoodRepository;
import com.example.a23110035_23110060.data.repository.MealRepository;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;

import java.util.List;

public class MealEntryController {
    private final FoodRepository foodRepository;
    private final MealRepository mealRepository;
    private final FirebaseRepository firebaseRepository;

    public MealEntryController(Context context) {
        foodRepository = new FoodRepository(context);
        mealRepository = new MealRepository(context);
        firebaseRepository = new FirebaseRepository(context);
    }

    public void searchFoods(String keyword, RepositoryCallback<List<FoodEntity>> callback) {
        foodRepository.searchFoods(keyword, callback);
    }

    public void saveMeal(MealLogEntity mealLog, RepositoryCallback<MealLogEntity> callback) {
        mealRepository.saveMealLog(mealLog, callback);
    }

    public void uploadMealImage(String userId, String mealLogId, String imagePath, RepositoryCallback<String> callback) {
        firebaseRepository.uploadMealImage(userId, mealLogId, imagePath, callback);
    }

    public void updateMealImageUrl(String mealLogId, String imageUrl, MealLogEntity mealLog, RepositoryCallback<MealLogEntity> callback) {
        mealRepository.updateMealImageUrl(mealLogId, imageUrl, mealLog, callback);
    }
}
