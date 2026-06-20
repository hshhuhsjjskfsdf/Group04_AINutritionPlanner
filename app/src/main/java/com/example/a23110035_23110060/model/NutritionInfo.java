package com.example.a23110035_23110060.model;

public class NutritionInfo {
    public String foodName;
    public double calories;
    public double protein;
    public double fat;
    public double carbs;
    public String serving;

    public NutritionInfo() {
    }

    public NutritionInfo(String foodName, double calories, double protein, double fat, double carbs, String serving) {
        this.foodName = foodName;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.serving = serving;
    }
}
