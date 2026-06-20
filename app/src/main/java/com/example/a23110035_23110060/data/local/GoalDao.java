package com.example.a23110035_23110060.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(GoalEntity goal);

    @Query("SELECT * FROM goals WHERE userId = :userId LIMIT 1")
    GoalEntity getByUserId(String userId);

    @Delete
    void delete(GoalEntity goal);
}
