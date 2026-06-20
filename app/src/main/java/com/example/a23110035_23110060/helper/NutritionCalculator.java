package com.example.a23110035_23110060.helper;

import com.example.a23110035_23110060.data.local.MealLogEntity;

import java.util.List;

public class NutritionCalculator {
    private NutritionCalculator() {
    }

    public static double calculateMealCalories(double baseCalories, double multiplier) {
        return baseCalories * Math.max(0, multiplier);
    }

    public static double sumCalories(List<MealLogEntity> logs) {
        double total = 0;
        if (logs != null) {
            for (MealLogEntity log : logs) {
                total += log.calories;
            }
        }
        return total;
    }

    public static double sumProtein(List<MealLogEntity> logs) {
        double total = 0;
        if (logs != null) {
            for (MealLogEntity log : logs) {
                total += log.protein;
            }
        }
        return total;
    }

    public static double sumCarbs(List<MealLogEntity> logs) {
        double total = 0;
        if (logs != null) {
            for (MealLogEntity log : logs) {
                total += log.carbs;
            }
        }
        return total;
    }

    public static double sumFat(List<MealLogEntity> logs) {
        double total = 0;
        if (logs != null) {
            for (MealLogEntity log : logs) {
                total += log.fat;
            }
        }
        return total;
    }
}
