package com.example.a23110035_23110060.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "foods")
public class FoodEntity {
    @PrimaryKey
    @NonNull
    public String dishName = "";
    public double calories;
    public double protein;
    public double fat;
    public double carbs;
    public String serving;
    public String datasetSource;
    public long createdAt;
    public long updatedAt;
}
