package com.example.a23110035_23110060.view.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.data.local.GoalEntity;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.data.repository.GoalRepository;
import com.example.a23110035_23110060.data.repository.MealRepository;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.helper.DailyProgressCalculator;
import com.example.a23110035_23110060.helper.DateHelper;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class NutritionStatisticsFragment extends Fragment {

    private GoalRepository goalRepository;
    private MealRepository mealRepository;
    private String userId;

    private TextView textRangeDisplay, textAvgCalories, textGoalMetDays, textTotalMeals, textAvgProteinStats;
    private TextView textAvgProtein, textAvgCarbs, textAvgFat, textStatsInsight;
    private ProgressBar progressAvgProtein, progressAvgCarbs, progressAvgFat;
    private LineChart chartCaloriesTrend;
    private PieChart chartMacroDist;
    private MaterialButtonToggleGroup toggleGroupRange;

    private Date startDate, endDate;
    private GoalEntity userGoal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nutrition_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        goalRepository = new GoalRepository(requireContext());
        mealRepository = new MealRepository(requireContext());
        userId = FirebaseHelper.getCurrentUserId();

        initViews(view);
        setupRangeSelector();
        
        // Default to 7 days
        selectLastNDays(7);
    }

    private void initViews(View view) {
        textRangeDisplay = view.findViewById(R.id.textRangeDisplay);
        textAvgCalories = view.findViewById(R.id.textAvgCalories);
        textGoalMetDays = view.findViewById(R.id.textGoalMetDays);
        textTotalMeals = view.findViewById(R.id.textTotalMeals);
        textAvgProteinStats = view.findViewById(R.id.textAvgProteinStats);
        textAvgProtein = view.findViewById(R.id.textAvgProtein);
        textAvgCarbs = view.findViewById(R.id.textAvgCarbs);
        textAvgFat = view.findViewById(R.id.textAvgFat);
        textStatsInsight = view.findViewById(R.id.textStatsInsight);
        progressAvgProtein = view.findViewById(R.id.progressAvgProtein);
        progressAvgCarbs = view.findViewById(R.id.progressAvgCarbs);
        progressAvgFat = view.findViewById(R.id.progressAvgFat);
        chartCaloriesTrend = view.findViewById(R.id.chartCaloriesTrend);
        chartMacroDist = view.findViewById(R.id.chartMacroDist);
        toggleGroupRange = view.findViewById(R.id.toggleGroupRange);
    }

    private void setupRangeSelector() {
        toggleGroupRange.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnRange7) {
                selectLastNDays(7);
            } else if (checkedId == R.id.btnRange30) {
                selectLastNDays(30);
            } else if (checkedId == R.id.btnRangeCustom) {
                showCustomRangePicker();
            }
        });
    }

    private void selectLastNDays(int days) {
        Calendar cal = Calendar.getInstance();
        endDate = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, -(days - 1));
        startDate = cal.getTime();
        loadData();
    }

    private void showCustomRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Chọn khoảng thời gian")
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            startDate = new Date(selection.first);
            endDate = new Date(selection.second);
            loadData();
        });
        picker.show(getChildFragmentManager(), "range_picker");
    }

    private void loadData() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        textRangeDisplay.setText(sdf.format(startDate) + " – " + sdf.format(endDate));

        String startStr = DateHelper.formatDate(startDate);
        String endStr = DateHelper.formatDate(endDate);

        goalRepository.getGoal(new RepositoryCallback<GoalEntity>() {
            @Override
            public void onSuccess(GoalEntity goal) {
                userGoal = goal != null ? goal : DailyProgressCalculator.defaultGoal();
                mealRepository.getLogsBetweenDates(userId, startStr, endStr, new RepositoryCallback<List<MealLogEntity>>() {
                    @Override
                    public void onSuccess(List<MealLogEntity> logs) {
                        if (getActivity() != null) getActivity().runOnUiThread(() -> calculateAndRender(logs));
                    }
                    @Override
                    public void onError(String message) {}
                });
            }
            @Override
            public void onError(String message) {}
        });
    }

    private void calculateAndRender(List<MealLogEntity> logs) {
        if (logs == null) logs = new ArrayList<>();
        
        long diffInMillis = endDate.getTime() - startDate.getTime();
        int dayCount = (int) (diffInMillis / (24 * 60 * 60 * 1000)) + 1;
        if (dayCount <= 0) dayCount = 1;

        double totalCal = 0, totalP = 0, totalC = 0, totalF = 0;
        Map<String, Double> dailyCals = new TreeMap<>();
        
        // Initialize daily cals with 0
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTime(startDate);
        for (int i = 0; i < dayCount; i++) {
            dailyCals.put(DateHelper.formatDate(tempCal.getTime()), 0.0);
            tempCal.add(Calendar.DAY_OF_YEAR, 1);
        }

        for (MealLogEntity log : logs) {
            totalCal += log.calories;
            totalP += log.protein;
            totalC += log.carbs;
            totalF += log.fat;
            
            Double current = dailyCals.get(log.logDate);
            if (current != null) {
                dailyCals.put(log.logDate, current + log.calories);
            }
        }

        double avgCal = totalCal / dayCount;
        double avgP = totalP / dayCount;
        double avgC = totalC / dayCount;
        double avgF = totalF / dayCount;

        int goalMetCount = 0;
        for (Double cal : dailyCals.values()) {
            if (cal >= userGoal.targetCalories * 0.9 && cal <= userGoal.targetCalories * 1.1) {
                goalMetCount++;
            }
        }

        // Overview
        textAvgCalories.setText(String.format(Locale.US, "%.0f kcal", avgCal));
        textGoalMetDays.setText(String.format(Locale.US, "%d/%d ngày", goalMetCount, dayCount));
        textTotalMeals.setText(String.format(Locale.US, "%d bữa", logs.size()));
        textAvgProteinStats.setText(String.format(Locale.US, "%.0f g/ngày", avgP));

        // Macro Progress
        textAvgProtein.setText(String.format(Locale.US, "%.0f / %.0f g", avgP, userGoal.targetProtein));
        textAvgCarbs.setText(String.format(Locale.US, "%.0f / %.0f g", avgC, userGoal.targetCarbs));
        textAvgFat.setText(String.format(Locale.US, "%.0f / %.0f g", avgF, userGoal.targetFat));

        progressAvgProtein.setProgress((int) (userGoal.targetProtein > 0 ? (avgP / userGoal.targetProtein * 100) : 0));
        progressAvgCarbs.setProgress((int) (userGoal.targetCarbs > 0 ? (avgC / userGoal.targetCarbs * 100) : 0));
        progressAvgFat.setProgress((int) (userGoal.targetFat > 0 ? (avgF / userGoal.targetFat * 100) : 0));

        // Charts
        renderLineChart(dailyCals);
        renderPieChart(totalP, totalC, totalF);
        
        // Insight
        generateInsights(avgCal, avgP, goalMetCount, dayCount);
    }

    private void renderLineChart(Map<String, Double> dailyCals) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Double> entry : dailyCals.entrySet()) {
            entries.add(new Entry(i, entry.getValue().floatValue()));
            // Just show day/month
            String date = entry.getKey();
            if (date.length() >= 5) {
                labels.add(date.substring(8) + "/" + date.substring(5, 7));
            } else {
                labels.add(date);
            }
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Calories");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        chartCaloriesTrend.setData(lineData);
        chartCaloriesTrend.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartCaloriesTrend.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartCaloriesTrend.getXAxis().setGranularity(1f);
        chartCaloriesTrend.getAxisRight().setEnabled(false);
        chartCaloriesTrend.getDescription().setEnabled(false);
        chartCaloriesTrend.animateX(1000);
        chartCaloriesTrend.invalidate();
    }

    private void renderPieChart(double p, double c, double f) {
        double pCal = p * 4;
        double cCal = c * 4;
        double fCal = f * 9;
        double total = pCal + cCal + fCal;

        if (total == 0) {
            chartMacroDist.clear();
            chartMacroDist.setNoDataText("Chưa đủ dữ liệu dinh dưỡng để phân tích.");
            chartMacroDist.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) (pCal / total * 100), "Protein"));
        entries.add(new PieEntry((float) (cCal / total * 100), "Carbs"));
        entries.add(new PieEntry((float) (fCal / total * 100), "Fat"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#2196F3"), Color.parseColor("#FFC107"), Color.parseColor("#F44336")});
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        chartMacroDist.setData(data);
        chartMacroDist.setUsePercentValues(true);
        chartMacroDist.setCenterText("Dinh dưỡng");
        chartMacroDist.getDescription().setEnabled(false);
        chartMacroDist.getLegend().setEnabled(true);
        chartMacroDist.animateY(1000);
        chartMacroDist.invalidate();
    }

    private void generateInsights(double avgCal, double avgP, int goalMetCount, int dayCount) {
        StringBuilder insight = new StringBuilder();
        if (avgCal > userGoal.targetCalories + 100) {
            insight.append("Calories trung bình đang cao hơn mục tiêu ").append((int)(avgCal - userGoal.targetCalories)).append(" kcal mỗi ngày. ");
        } else if (avgCal < userGoal.targetCalories - 100) {
            insight.append("Calories trung bình đang thấp hơn mục tiêu ").append((int)(userGoal.targetCalories - avgCal)).append(" kcal. ");
        }

        if (goalMetCount > 0) {
            insight.append("Bạn đạt mục tiêu calories ").append(goalMetCount).append(" trong ").append(dayCount).append(" ngày. ");
        }

        if (userGoal.targetProtein - avgP > 15) {
            insight.append("Protein trung bình còn thiếu ").append((int)(userGoal.targetProtein - avgP)).append(" g mỗi ngày.");
        }

        if (insight.length() == 0) {
            insight.append("Duy trì thói quen ghi nhận để có nhận xét chính xác hơn.");
        }

        textStatsInsight.setText(insight.toString());
    }
}
