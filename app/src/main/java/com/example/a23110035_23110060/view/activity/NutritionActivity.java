package com.example.a23110035_23110060.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.helper.NavigationHelper;
import com.example.a23110035_23110060.view.fragment.MealPlanFragment;
import com.example.a23110035_23110060.view.fragment.TrackingFragment;

public class NutritionActivity extends AppCompatActivity {
    public static final String EXTRA_START_TAB = "extra_start_tab";
    public static final String TAB_PLAN = "plan";
    public static final String TAB_DIARY = "tracking";
    public static final String TAB_TRACKING = "tracking";

    private TextView textTitle;
    private android.widget.ImageView imgIcon;
    private String currentTab = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);
        
        textTitle = findViewById(R.id.text_nutrition_title);
        imgIcon = findViewById(R.id.img_nutrition_icon);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String startTab = intent.getStringExtra(EXTRA_START_TAB);
        if (startTab == null) startTab = TAB_PLAN;

        if (startTab.equals(currentTab)) {
            // Update bottom nav selection just in case
            int navId = TAB_PLAN.equals(startTab) ? R.id.nav_plan : R.id.nav_diary;
            NavigationHelper.setupBottomNavigation(this, navId);
            return;
        }

        currentTab = startTab;
        Fragment fragment;
        int navId;

        if (TAB_PLAN.equals(startTab)) {
            fragment = new MealPlanFragment();
            textTitle.setText("Kế hoạch ăn uống");
            if (imgIcon != null) imgIcon.setImageResource(R.drawable.ic_nav_plan);
            navId = R.id.nav_plan;
        } else {
            fragment = new TrackingFragment();
            textTitle.setText("Theo dõi dinh dưỡng");
            if (imgIcon != null) imgIcon.setImageResource(R.drawable.ic_nav_diary);
            navId = R.id.nav_diary;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nutrition_container, fragment)
                .commit();

        NavigationHelper.setupBottomNavigation(this, navId);
    }
}
