package com.example.a23110035_23110060.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                UserEntity.class,
                GoalEntity.class,
                FoodEntity.class,
                MealLogEntity.class,
                MealPlanEntity.class,
                PendingSyncEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract UserDao userDao();
    public abstract GoalDao goalDao();
    public abstract FoodDao foodDao();
    public abstract MealLogDao mealLogDao();
    public abstract MealPlanDao mealPlanDao();
    public abstract PendingSyncDao pendingSyncDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "ai_nutrition_planner.db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
