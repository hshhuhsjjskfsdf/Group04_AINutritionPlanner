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

    public static double calculateBMI(double weightKg, double heightCm) {
        if (heightCm <= 0) return 0;
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    public static String getBMICategory(double bmi) {
        if (bmi < 18.5) return "Thiếu cân";
        if (bmi < 25) return "Bình thường";
        if (bmi < 30) return "Thừa cân";
        return "Béo phì";
    }

    public static double calculateBMR(double weightKg, double heightCm, int age, String gender) {
        double offset = "Nữ".equalsIgnoreCase(gender) ? -161 : 5;
        // Mifflin-St Jeor Equation
        return (10 * weightKg) + (6.25 * heightCm) - (5 * age) + offset;
    }

    public static double calculateTDEE(double bmr, String activityLevel) {
        double factor = 1.2;
        if (activityLevel != null) {
            switch (activityLevel) {
                case "Vận động nhẹ": factor = 1.375; break;
                case "Vận động vừa": 
                case "Vận động vừa phải": factor = 1.55; break;
                case "Vận động nhiều": factor = 1.725; break;
                case "Vận động rất nhiều": factor = 1.9; break;
            }
        }
        return bmr * factor;
    }
}
