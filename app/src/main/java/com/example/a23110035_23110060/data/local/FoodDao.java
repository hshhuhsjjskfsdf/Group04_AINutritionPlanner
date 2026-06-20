package com.example.a23110035_23110060.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FoodEntity> foods);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(FoodEntity food);

    @Query("SELECT COUNT(*) FROM foods")
    int countFoods();

    @Query("SELECT * FROM foods WHERE dishName LIKE '%' || :keyword || '%' ORDER BY dishName LIMIT 50")
    List<FoodEntity> searchFoods(String keyword);

    @Query("SELECT * FROM foods WHERE dishName = :name LIMIT 1")
    FoodEntity getFoodByName(String name);

    @Query("SELECT * FROM foods ORDER BY dishName")
    List<FoodEntity> getAllFoods();
}
