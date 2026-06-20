package com.example.a23110035_23110060.helper;

import com.example.a23110035_23110060.data.local.GoalEntity;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.model.DailyProgress;

import java.util.List;

public class DailyProgressCalculator {
    private DailyProgressCalculator() {
    }

    public static DailyProgress calculate(List<MealLogEntity> logs, GoalEntity goal) {
        GoalEntity effectiveGoal = goal == null ? defaultGoal() : goal;
        DailyProgress progress = new DailyProgress();
        progress.consumedCalories = NutritionCalculator.sumCalories(logs);
        progress.consumedProtein = NutritionCalculator.sumProtein(logs);
        progress.consumedCarbs = NutritionCalculator.sumCarbs(logs);
        progress.consumedFat = NutritionCalculator.sumFat(logs);
        progress.remainingCalories = Math.max(0, effectiveGoal.targetCalories - progress.consumedCalories);
        progress.caloriePercent = percent(progress.consumedCalories, effectiveGoal.targetCalories);
        progress.proteinPercent = percent(progress.consumedProtein, effectiveGoal.targetProtein);
        progress.carbsPercent = percent(progress.consumedCarbs, effectiveGoal.targetCarbs);
        progress.fatPercent = percent(progress.consumedFat, effectiveGoal.targetFat);
        return progress;
    }

    public static GoalEntity defaultGoal() {
        GoalEntity goal = new GoalEntity();
        goal.goalId = "default";
        goal.targetCalories = 2000;
        goal.targetProtein = 100;
        goal.targetCarbs = 250;
        goal.targetFat = 60;
        return goal;
    }

    private static int percent(double value, double target) {
        if (target <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.round((value / target) * 100));
    }
}
