package com.example.a23110035_23110060.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String userId = "";
    public String fullName;
    public String email;
    public int age;
    public String gender;
    public double heightCm;
    public double weightKg;
    public String activityLevel;
    public long createdAt;
    public long updatedAt;
    
    @NonNull
    public String avatarUrl = "";
    
    public String breakfastReminderTime = "07:00";
    public String lunchReminderTime = "12:00";
    public String dinnerReminderTime = "18:00";
    public String snackReminderTime = "22:00";
}
