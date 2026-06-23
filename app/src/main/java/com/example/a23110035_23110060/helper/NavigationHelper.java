package com.example.a23110035_23110060.helper;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.view.activity.MainActivity;
import com.example.a23110035_23110060.view.activity.MealEntryActivity;
import com.example.a23110035_23110060.view.activity.NutritionActivity;
import com.example.a23110035_23110060.view.activity.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationHelper {

    public static void setupBottomNavigation(Activity activity, int currentItemId) {
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_nav_main);
        if (bottomNavigationView == null) {
            return;
        }

        bottomNavigationView.setSelectedItemId(currentItemId);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == currentItemId) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(activity, MainActivity.class);
            } else if (itemId == R.id.nav_diary) {
                intent = new Intent(activity, NutritionActivity.class);
                intent.putExtra(NutritionActivity.EXTRA_START_TAB, NutritionActivity.TAB_DIARY);
            } else if (itemId == R.id.nav_plan) {
                intent = new Intent(activity, NutritionActivity.class);
                intent.putExtra(NutritionActivity.EXTRA_START_TAB, NutritionActivity.TAB_PLAN);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(activity, SettingsActivity.class);
            }

            if (intent != null) {
                // Remove animation to make the bottom nav appear persistent
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
            return false; // Return false so the selected item visually updates only when the activity starts? Wait, if we return true, it updates visually before transition. Let's return false and let the new activity set its state. Actually, return true is fine.
        });

        View fabAddMeal = activity.findViewById(R.id.fab_add_meal);
        if (fabAddMeal != null) {
            fabAddMeal.setOnClickListener(v -> {
                Intent intent = new Intent(activity, MealEntryActivity.class);
                activity.startActivity(intent);
            });
        }
    }
}
