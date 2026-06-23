package com.example.a23110035_23110060.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.controller.NutritionController;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.data.local.MealPlanEntity;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.helper.DateHelper;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.ValidationHelper;

import com.example.a23110035_23110060.helper.NavigationHelper;
import com.example.a23110035_23110060.view.adapter.MealLogAdapter;
import com.example.a23110035_23110060.view.adapter.MealPlanAdapter;


import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class NutritionActivity extends AppCompatActivity {
    public static final String EXTRA_START_TAB = "extra_start_tab";
    public static final String TAB_DIARY = "diary";
    public static final String TAB_PLAN = "plan";

    private NutritionController controller;
    private MealLogAdapter logAdapter;
    private MealPlanAdapter planAdapter;
    private LinearLayout sectionLog;
    private LinearLayout sectionPlan;
    private EditText editLogDate;
    private EditText editPlanDate;
    private EditText editPlanFoodName;
    private EditText editPlanCalories;
    private EditText editPlanProtein;
    private EditText editPlanCarbs;
    private EditText editPlanFat;
    private EditText editPlanNote;
    private Spinner spinnerPlanMealType;
    private TextView textEmptyLogs;
    private TextView textEmptyPlans;
    private TextView textNutritionTitle;
    private String userId;

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showInitialTab();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);
        userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) {
            finish();
            return;
        }
        controller = new NutritionController(this);
        bindViews();
        setupLists();
        setupPlanForm();
        showInitialTab();
        loadLogs();
        loadPlans();
    }

    private void bindViews() {
        sectionLog = findViewById(R.id.sectionMealLog);
        sectionPlan = findViewById(R.id.sectionMealPlan);
        editLogDate = findViewById(R.id.editLogDate);
        editPlanDate = findViewById(R.id.editPlanDate);
        editPlanFoodName = findViewById(R.id.editPlanFoodName);
        editPlanCalories = findViewById(R.id.editPlanCalories);
        editPlanProtein = findViewById(R.id.editPlanProtein);
        editPlanCarbs = findViewById(R.id.editPlanCarbs);
        editPlanFat = findViewById(R.id.editPlanFat);
        editPlanNote = findViewById(R.id.editPlanNote);
        spinnerPlanMealType = findViewById(R.id.spinnerPlanMealType);
        textEmptyLogs = findViewById(R.id.textEmptyLogs);
        textEmptyPlans = findViewById(R.id.textEmptyPlans);
        textNutritionTitle = findViewById(R.id.text_nutrition_title);
        
        editLogDate.setText(DateHelper.today());
        editPlanDate.setText(DateHelper.today());
        
        findViewById(R.id.buttonLoadLogs).setOnClickListener(v -> loadLogs());
        findViewById(R.id.buttonLoadPlans).setOnClickListener(v -> loadPlans());
    }

    private void showInitialTab() {
        String startTab = getIntent().getStringExtra(EXTRA_START_TAB);
        if (TAB_PLAN.equals(startTab)) {
            textNutritionTitle.setText("Kế hoạch");
            showTab(sectionPlan);
            NavigationHelper.setupBottomNavigation(this, R.id.nav_plan);
            return;
        }
        textNutritionTitle.setText("Nhật ký");
        showTab(sectionLog);
        NavigationHelper.setupBottomNavigation(this, R.id.nav_diary);
    }

    private void setupLists() {
        RecyclerView recyclerLogs = findViewById(R.id.recyclerMealLogs);
        recyclerLogs.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new MealLogAdapter(mealLog -> controller.deleteMealLog(mealLog, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    Toast.makeText(NutritionActivity.this, "Đã xóa bữa ăn", Toast.LENGTH_SHORT).show();
                    loadLogs();
                });
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        }));
        recyclerLogs.setAdapter(logAdapter);

        RecyclerView recyclerPlans = findViewById(R.id.recyclerMealPlans);
        recyclerPlans.setLayoutManager(new LinearLayoutManager(this));
        planAdapter = new MealPlanAdapter(mealPlan -> controller.deleteMealPlan(mealPlan, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    Toast.makeText(NutritionActivity.this, "Đã xóa kế hoạch", Toast.LENGTH_SHORT).show();
                    loadPlans();
                });
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        }));
        recyclerPlans.setAdapter(planAdapter);
    }

    private void setupPlanForm() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Breakfast", "Lunch", "Dinner", "Snack"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlanMealType.setAdapter(adapter);
        Button savePlan = findViewById(R.id.buttonSavePlan);
        savePlan.setOnClickListener(v -> savePlan());
    }

    private void showTab(View selected) {
        sectionLog.setVisibility(selected == sectionLog ? View.VISIBLE : View.GONE);
        sectionPlan.setVisibility(selected == sectionPlan ? View.VISIBLE : View.GONE);
    }

    private void loadLogs() {
        controller.loadMealLogs(userId, editLogDate.getText().toString().trim(), new RepositoryCallback<List<MealLogEntity>>() {
            @Override
            public void onSuccess(List<MealLogEntity> result) {
                runOnUiThread(() -> {
                    logAdapter.submitList(result);
                    textEmptyLogs.setVisibility(result == null || result.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void savePlan() {
        String name = editPlanFoodName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập món trong kế hoạch", Toast.LENGTH_SHORT).show();
            return;
        }
        MealPlanEntity plan = new MealPlanEntity();
        plan.mealPlanId = UUID.randomUUID().toString();
        plan.userId = userId;
        plan.planDate = editPlanDate.getText().toString().trim();
        plan.mealType = spinnerPlanMealType.getSelectedItem().toString();
        plan.foodName = name;
        plan.calories = ValidationHelper.parseDoubleOrZero(editPlanCalories.getText().toString());
        plan.protein = ValidationHelper.parseDoubleOrZero(editPlanProtein.getText().toString());
        plan.carbs = ValidationHelper.parseDoubleOrZero(editPlanCarbs.getText().toString());
        plan.fat = ValidationHelper.parseDoubleOrZero(editPlanFat.getText().toString());
        plan.note = editPlanNote.getText().toString().trim();
        controller.saveMealPlan(plan, new RepositoryCallback<MealPlanEntity>() {
            @Override
            public void onSuccess(MealPlanEntity result) {
                runOnUiThread(() -> {
                    Toast.makeText(NutritionActivity.this, "Đã lưu kế hoạch", Toast.LENGTH_SHORT).show();
                    clearPlanForm();
                    loadPlans();
                });
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void clearPlanForm() {
        editPlanFoodName.setText("");
        editPlanCalories.setText("");
        editPlanProtein.setText("");
        editPlanCarbs.setText("");
        editPlanFat.setText("");
        editPlanNote.setText("");
    }

    private void loadPlans() {
        controller.loadMealPlans(userId, editPlanDate.getText().toString().trim(), new RepositoryCallback<List<MealPlanEntity>>() {
            @Override
            public void onSuccess(List<MealPlanEntity> result) {
                runOnUiThread(() -> {
                    planAdapter.submitList(result);
                    textEmptyPlans.setVisibility(result == null || result.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }



    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }
}
