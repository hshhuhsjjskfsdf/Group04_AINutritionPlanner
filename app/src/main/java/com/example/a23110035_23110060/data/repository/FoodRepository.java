package com.example.a23110035_23110060.data.repository;

import android.content.Context;

import com.example.a23110035_23110060.data.local.AppDatabase;
import com.example.a23110035_23110060.data.local.FoodEntity;
import com.example.a23110035_23110060.helper.CsvImportHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FoodRepository {
    private final Context context;
    private final AppDatabase database;
    private final FirebaseRepository firebaseRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public FoodRepository(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.firebaseRepository = new FirebaseRepository(context);
    }

    public void seedFoodsIfNeeded(RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            int count = database.foodDao().countFoods();
            if (count == 0) {
                List<FoodEntity> foods = CsvImportHelper.readFoodsFromAssets(context);
                if (!foods.isEmpty()) {
                    database.foodDao().insertAll(foods);
                    firebaseRepository.uploadFoodsIfNeeded(foods);
                    count = foods.size();
                }
            } else {
                uploadFoodsToFirestoreIfNeeded();
            }
            if (callback != null) {
                callback.onSuccess(count);
            }
        });
    }

    public void searchFoods(String keyword, RepositoryCallback<List<FoodEntity>> callback) {
        executor.execute(() -> {
            List<FoodEntity> result = database.foodDao().searchFoods(keyword == null ? "" : keyword.trim());
            if (callback != null) {
                callback.onSuccess(result);
            }
        });
    }

    public void getFoodByName(String dishName, RepositoryCallback<FoodEntity> callback) {
        executor.execute(() -> {
            FoodEntity food = database.foodDao().getFoodByName(dishName);
            if (callback != null) {
                callback.onSuccess(food);
            }
        });
    }

    public void getAllFoods(RepositoryCallback<List<FoodEntity>> callback) {
        executor.execute(() -> {
            List<FoodEntity> foods = database.foodDao().getAllFoods();
            if (callback != null) {
                callback.onSuccess(foods);
            }
        });
    }

    public void uploadFoodsToFirestoreIfNeeded() {
        executor.execute(() -> firebaseRepository.uploadFoodsIfNeeded(database.foodDao().getAllFoods()));
    }
}
