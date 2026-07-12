package com.example.a23110035_23110060.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MealLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MealLogEntity mealLog);

    @Update
    void update(MealLogEntity mealLog);

    @Delete
    void delete(MealLogEntity mealLog);

    @Query("DELETE FROM meal_logs WHERE mealLogId = :mealLogId")
    void deleteById(String mealLogId);

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND logDate = :date ORDER BY createdAt DESC")
    List<MealLogEntity> getByUserAndDate(String userId, String date);

    @Query("SELECT * FROM meal_logs WHERE userId = :userId ORDER BY logDate DESC, createdAt DESC")
    List<MealLogEntity> getByUser(String userId);

    @Query("SELECT * FROM meal_logs WHERE userId = :userId ORDER BY createdAt DESC")
    List<MealLogEntity> getAllByUser(String userId);

    @Query("SELECT * FROM meal_logs WHERE isSynced = 0 AND userId = :userId ORDER BY createdAt ASC")
    List<MealLogEntity> getUnsyncedByUser(String userId);

    @Query("UPDATE meal_logs SET isSynced = 1, imageUrl = :imageUrl, updatedAt = :updatedAt WHERE mealLogId = :mealLogId")
    void markSynced(String mealLogId, String imageUrl, long updatedAt);

    @Query("UPDATE meal_logs SET imageUrl = :imageUrl, updatedAt = :updatedAt, isSynced = 0 WHERE mealLogId = :mealLogId")
    void updateMealImageUrl(String mealLogId, String imageUrl, long updatedAt);

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND logDate = :today ORDER BY createdAt DESC")
    List<MealLogEntity> getTodayLogs(String userId, String today);

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND logDate BETWEEN :startDate AND :endDate ORDER BY logDate ASC")
    List<MealLogEntity> getLogsBetweenDates(String userId, String startDate, String endDate);
}
