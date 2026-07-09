package com.example.a23110035_23110060.view.activity;

import android.content.res.ColorStateList;
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
import com.example.a23110035_23110060.data.local.GoalEntity;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.data.local.MealPlanEntity;
import com.example.a23110035_23110060.data.repository.GoalRepository;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.helper.DateHelper;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.NavigationHelper;
import com.example.a23110035_23110060.helper.ValidationHelper;
import com.example.a23110035_23110060.view.adapter.MealLogAdapter;
import com.example.a23110035_23110060.view.adapter.MealPlanAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class NutritionActivity extends AppCompatActivity {
    public static final String EXTRA_START_TAB = "extra_start_tab";
    public static final String TAB_DIARY = "diary";
    public static final String TAB_PLAN = "plan";
    public static final String TAB_STATS = "stats";

    private NutritionController controller;
    private GoalRepository goalRepository;
    private MealLogAdapter logAdapter;
    private MealPlanAdapter planAdapter;
    private LinearLayout sectionLog;
    private LinearLayout sectionPlan;
    private LinearLayout sectionStats;
    private EditText editLogDate;
    private EditText editPlanDate;
    private EditText editPlanFoodName;
    private EditText editPlanCalories;
    private EditText editPlanProtein;
    private EditText editPlanCarbs;
    private EditText editPlanFat;
    private EditText editPlanNote;
    private EditText editStatsStartDate;
    private EditText editStatsEndDate;
    private Spinner spinnerPlanMealType;
    private TextView textEmptyLogs;
    private TextView textEmptyPlans;
    private TextView textNutritionTitle;
    private TextView textPlanDailySummary;
    private TextView textStatsSummary;
    private MaterialButton buttonTabDiary;
    private MaterialButton buttonTabPlan;
    private MaterialButton buttonTabStats;
    private LineChart lineCaloriesChart;
    private BarChart barMacrosChart;
    private PieChart pieMacrosChart;
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
        goalRepository = new GoalRepository(this);
        bindViews();
        setupLists();
        setupPlanForm();
        setupTabs();
        showInitialTab();
        loadLogs();
        loadPlans();
        loadStats();
    }

    private void bindViews() {
        sectionLog = findViewById(R.id.sectionMealLog);
        sectionPlan = findViewById(R.id.sectionMealPlan);
        sectionStats = findViewById(R.id.sectionStatistics);
        editLogDate = findViewById(R.id.editLogDate);
        editPlanDate = findViewById(R.id.editPlanDate);
        editPlanFoodName = findViewById(R.id.editPlanFoodName);
        editPlanCalories = findViewById(R.id.editPlanCalories);
        editPlanProtein = findViewById(R.id.editPlanProtein);
        editPlanCarbs = findViewById(R.id.editPlanCarbs);
        editPlanFat = findViewById(R.id.editPlanFat);
        editPlanNote = findViewById(R.id.editPlanNote);
        editStatsStartDate = findViewById(R.id.editStatsStartDate);
        editStatsEndDate = findViewById(R.id.editStatsEndDate);
        spinnerPlanMealType = findViewById(R.id.spinnerPlanMealType);
        textEmptyLogs = findViewById(R.id.textEmptyLogs);
        textEmptyPlans = findViewById(R.id.textEmptyPlans);
        textNutritionTitle = findViewById(R.id.text_nutrition_title);
        textPlanDailySummary = findViewById(R.id.textPlanDailySummary);
        textStatsSummary = findViewById(R.id.textStatsSummary);
        buttonTabDiary = findViewById(R.id.buttonTabDiary);
        buttonTabPlan = findViewById(R.id.buttonTabPlan);
        buttonTabStats = findViewById(R.id.buttonTabStats);
        lineCaloriesChart = findViewById(R.id.lineCaloriesChart);
        barMacrosChart = findViewById(R.id.barMacrosChart);
        pieMacrosChart = findViewById(R.id.pieMacrosChart);

        editLogDate.setText(DateHelper.today());
        editPlanDate.setText(DateHelper.today());
        editStatsStartDate.setText(DateHelper.getStartOfWeek());
        editStatsEndDate.setText(DateHelper.getEndOfWeek());

        findViewById(R.id.buttonLoadLogs).setOnClickListener(v -> loadLogs());
        findViewById(R.id.buttonLoadPlans).setOnClickListener(v -> loadPlans());
        findViewById(R.id.buttonLoadStats).setOnClickListener(v -> loadStats());
    }

    private void setupTabs() {
        buttonTabDiary.setOnClickListener(v -> {
            setIntent(getIntent().putExtra(EXTRA_START_TAB, TAB_DIARY));
            showTab(sectionLog);
        });
        buttonTabPlan.setOnClickListener(v -> {
            setIntent(getIntent().putExtra(EXTRA_START_TAB, TAB_PLAN));
            showTab(sectionPlan);
        });
        buttonTabStats.setOnClickListener(v -> {
            setIntent(getIntent().putExtra(EXTRA_START_TAB, TAB_STATS));
            showTab(sectionStats);
        });
    }

    private void showInitialTab() {
        String startTab = getIntent().getStringExtra(EXTRA_START_TAB);
        if (TAB_PLAN.equals(startTab)) {
            showTab(sectionPlan);
            NavigationHelper.setupBottomNavigation(this, R.id.nav_plan);
            return;
        }
        if (TAB_STATS.equals(startTab)) {
            showTab(sectionStats);
            NavigationHelper.setupBottomNavigation(this, R.id.nav_diary);
            return;
        }
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
                    loadStats();
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
                new String[]{"Sáng", "Trưa", "Tối", "Bữa phụ"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlanMealType.setAdapter(adapter);
        Button savePlan = findViewById(R.id.buttonSavePlan);
        savePlan.setOnClickListener(v -> savePlan());
    }

    private void showTab(View selected) {
        sectionLog.setVisibility(selected == sectionLog ? View.VISIBLE : View.GONE);
        sectionPlan.setVisibility(selected == sectionPlan ? View.VISIBLE : View.GONE);
        sectionStats.setVisibility(selected == sectionStats ? View.VISIBLE : View.GONE);

        if (selected == sectionPlan) {
            textNutritionTitle.setText("Kế hoạch ăn uống");
        } else if (selected == sectionStats) {
            textNutritionTitle.setText("Thống kê");
        } else {
            textNutritionTitle.setText("Nhật ký");
        }
        styleTab(buttonTabDiary, selected == sectionLog);
        styleTab(buttonTabPlan, selected == sectionPlan);
        styleTab(buttonTabStats, selected == sectionStats);
    }

    private void styleTab(MaterialButton button, boolean selected) {
        int background = selected ? getColor(R.color.primary) : getColor(R.color.surface_soft);
        int text = selected ? getColor(R.color.white) : getColor(R.color.primary_dark);
        button.setBackgroundTintList(ColorStateList.valueOf(background));
        button.setTextColor(text);
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
            Toast.makeText(this, "Vui lòng nhập tên món ăn", Toast.LENGTH_SHORT).show();
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
                    updatePlanSummary(result);
                });
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void updatePlanSummary(List<MealPlanEntity> plans) {
        Totals totals = new Totals();
        if (plans != null) {
            for (MealPlanEntity plan : plans) {
                totals.add(plan.calories, plan.protein, plan.carbs, plan.fat);
            }
        }
        goalRepository.getGoal(new RepositoryCallback<GoalEntity>() {
            @Override
            public void onSuccess(GoalEntity goal) {
                runOnUiThread(() -> textPlanDailySummary.setText(String.format(Locale.US,
                        "Kế hoạch: %.0f/%.0f kcal | P %.1f/%.0fg | C %.1f/%.0fg | F %.1f/%.0fg",
                        totals.calories, goal.targetCalories,
                        totals.protein, goal.targetProtein,
                        totals.carbs, goal.targetCarbs,
                        totals.fat, goal.targetFat)));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> textPlanDailySummary.setText(String.format(Locale.US,
                        "Kế hoạch: %.0f kcal | P %.1fg | C %.1fg | F %.1fg",
                        totals.calories, totals.protein, totals.carbs, totals.fat)));
            }
        });
    }

    private void loadStats() {
        String startDate = editStatsStartDate.getText().toString().trim();
        String endDate = editStatsEndDate.getText().toString().trim();
        controller.loadMealLogsBetweenDates(userId, startDate, endDate, new RepositoryCallback<List<MealLogEntity>>() {
            @Override
            public void onSuccess(List<MealLogEntity> result) {
                runOnUiThread(() -> renderStats(result, startDate, endDate));
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void renderStats(List<MealLogEntity> logs, String startDate, String endDate) {
        if (logs == null || logs.isEmpty()) {
            textStatsSummary.setText("Không tìm thấy nhật ký bữa ăn từ " + startDate + " đến " + endDate);
            lineCaloriesChart.clear();
            barMacrosChart.clear();
            pieMacrosChart.clear();
            return;
        }

        TreeMap<String, Totals> dailyTotals = new TreeMap<>();
        Totals periodTotals = new Totals();
        for (MealLogEntity log : logs) {
            Totals day = dailyTotals.get(log.logDate);
            if (day == null) {
                day = new Totals();
                dailyTotals.put(log.logDate, day);
            }
            day.add(log.calories, log.protein, log.carbs, log.fat);
            periodTotals.add(log.calories, log.protein, log.carbs, log.fat);
        }

        textStatsSummary.setText(String.format(Locale.US,
                "%d bữa ăn | %.0f kcal | Đạm %.1fg | Tinh bột %.1fg | Béo %.1fg",
                logs.size(), periodTotals.calories, periodTotals.protein, periodTotals.carbs, periodTotals.fat));
        renderCaloriesLineChart(dailyTotals);
        renderMacroBarChart(periodTotals);
        renderMacroPieChart(periodTotals);
    }

    private void renderCaloriesLineChart(TreeMap<String, Totals> dailyTotals) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Totals> entry : dailyTotals.entrySet()) {
            labels.add(entry.getKey().substring(Math.max(0, entry.getKey().length() - 5)));
            entries.add(new Entry(index, (float) entry.getValue().calories));
            index++;
        }
        LineDataSet dataSet = new LineDataSet(entries, "Lượng Calo theo ngày");
        dataSet.setColor(getColor(R.color.primary));
        dataSet.setCircleColor(getColor(R.color.primary_dark));
        dataSet.setLineWidth(2f);
        dataSet.setValueTextColor(getColor(R.color.text_primary));
        lineCaloriesChart.setData(new LineData(dataSet));
        configureXAxis(lineCaloriesChart.getXAxis(), labels);
        lineCaloriesChart.getDescription().setEnabled(false);
        lineCaloriesChart.getAxisRight().setEnabled(false);
        lineCaloriesChart.invalidate();
    }

    private void renderMacroBarChart(Totals totals) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) totals.protein));
        entries.add(new BarEntry(1, (float) totals.carbs));
        entries.add(new BarEntry(2, (float) totals.fat));
        BarDataSet dataSet = new BarDataSet(entries, "Macro (gram)");
        dataSet.setColors(getColor(R.color.primary), getColor(R.color.accent), getColor(R.color.warning));
        dataSet.setValueTextColor(getColor(R.color.text_primary));
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.55f);
        barMacrosChart.setData(data);
        configureXAxis(barMacrosChart.getXAxis(), macroLabels());
        barMacrosChart.getDescription().setEnabled(false);
        barMacrosChart.getAxisRight().setEnabled(false);
        barMacrosChart.invalidate();
    }

    private void renderMacroPieChart(Totals totals) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) totals.protein, "Đạm"));
        entries.add(new PieEntry((float) totals.carbs, "Tinh bột"));
        entries.add(new PieEntry((float) totals.fat, "Béo"));
        PieDataSet dataSet = new PieDataSet(entries, "Tỷ lệ Macro");
        dataSet.setColors(getColor(R.color.primary), getColor(R.color.accent), getColor(R.color.warning));
        dataSet.setValueTextColor(getColor(R.color.text_primary));
        pieMacrosChart.setData(new PieData(dataSet));
        pieMacrosChart.getDescription().setEnabled(false);
        pieMacrosChart.setUsePercentValues(true);
        pieMacrosChart.invalidate();
    }

    private void configureXAxis(XAxis axis, List<String> labels) {
        axis.setValueFormatter(new IndexAxisValueFormatter(labels));
        axis.setGranularity(1f);
        axis.setPosition(XAxis.XAxisPosition.BOTTOM);
        axis.setTextColor(getColor(R.color.text_secondary));
        axis.setDrawGridLines(false);
    }

    private List<String> macroLabels() {
        List<String> labels = new ArrayList<>();
        labels.add("Đạm");
        labels.add("Tinh bột");
        labels.add("Béo");
        return labels;
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    private static class Totals {
        double calories;
        double protein;
        double carbs;
        double fat;

        void add(double calories, double protein, double carbs, double fat) {
            this.calories += calories;
            this.protein += protein;
            this.carbs += carbs;
            this.fat += fat;
        }
    }
}
