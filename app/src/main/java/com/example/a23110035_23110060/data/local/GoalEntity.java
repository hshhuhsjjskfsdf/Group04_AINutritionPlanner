package com.example.a23110035_23110060.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "goals")
public class GoalEntity {
    @PrimaryKey
    @NonNull
    public String goalId = "";
    public String userId;
    public double targetCalories;
    public double targetProtein;
    public double targetCarbs;
    public double targetFat;
    public long createdAt;
    public long updatedAt;
}
