package com.example.a23110035_23110060.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MealPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MealPlanEntity mealPlan);

    @Update
    void update(MealPlanEntity mealPlan);

    @Delete
    void delete(MealPlanEntity mealPlan);

    @Query("DELETE FROM meal_plans WHERE mealPlanId = :mealPlanId")
    void deleteById(String mealPlanId);

    @Query("SELECT * FROM meal_plans WHERE userId = :userId AND planDate = :date ORDER BY mealType, createdAt DESC")
    List<MealPlanEntity> getByUserAndDate(String userId, String date);

    @Query("SELECT * FROM meal_plans WHERE isSynced = 0 AND userId = :userId ORDER BY createdAt ASC")
    List<MealPlanEntity> getUnsyncedByUser(String userId);

    @Query("UPDATE meal_plans SET isSynced = 1, updatedAt = :updatedAt WHERE mealPlanId = :mealPlanId")
    void markSynced(String mealPlanId, long updatedAt);

    @Query("SELECT * FROM meal_plans WHERE mealPlanId = :mealPlanId LIMIT 1")
    MealPlanEntity getById(String mealPlanId);

    @Query("UPDATE meal_plans SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE mealPlanId = :mealPlanId")
    void updateCompletionStatus(String mealPlanId, boolean isCompleted, long updatedAt);

    @Query("SELECT * FROM meal_plans WHERE userId = :userId AND planDate = :date AND mealType = :mealType ORDER BY createdAt DESC")
    List<MealPlanEntity> getByMealType(String userId, String date, String mealType);
}
