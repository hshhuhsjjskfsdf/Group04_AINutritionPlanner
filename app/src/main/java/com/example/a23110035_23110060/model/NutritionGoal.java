package com.example.a23110035_23110060.model;

public class NutritionGoal {
    public double targetCalories;
    public double targetProtein;
    public double targetCarbs;
    public double targetFat;

    public NutritionGoal() {
    }

    public NutritionGoal(double targetCalories, double targetProtein, double targetCarbs, double targetFat) {
        this.targetCalories = targetCalories;
        this.targetProtein = targetProtein;
        this.targetCarbs = targetCarbs;
        this.targetFat = targetFat;
    }
}
