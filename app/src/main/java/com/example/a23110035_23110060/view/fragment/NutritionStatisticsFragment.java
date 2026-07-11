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
        generateInsights(avgCal, avgP, avgC, avgF, goalMetCount, dayCount);
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

    private void generateInsights(double avgCal, double avgP, double avgC, double avgF, int goalMetCount, int dayCount) {
        StringBuilder insight = new StringBuilder();
        
        if (avgCal == 0) {
            textStatsInsight.setText("Chào bạn! Hãy bắt đầu ghi chép bữa ăn để mình có thể phân tích và đưa ra những lời khuyên hữu ích cho bạn nhé. 🌱");
            return;
        }

        // 1. Calories Insight
        if (avgCal > userGoal.targetCalories + 100) {
            insight.append("🔥 Mức năng lượng trung bình hiện tại là <b>").append((int)avgCal)
                   .append(" kcal</b>, hơi cao hơn mục tiêu một chút. Việc điều chỉnh nhẹ khẩu phần ăn sẽ giúp bạn duy trì cân nặng ổn định hơn đấy. <br/><br/>");
        } else if (avgCal < userGoal.targetCalories - 100) {
            insight.append("⚡ Bạn đang nạp khoảng <b>").append((int)avgCal)
                   .append(" kcal mỗi ngày</b>, hơi thấp so với mục tiêu. Hãy chú ý bổ sung thêm dinh dưỡng để luôn tràn đầy sức sống nhé! <br/><br/>");
        } else {
            insight.append("🌟 Thật tuyệt vời! Bạn đang duy trì mức năng lượng <b>").append((int)avgCal)
                   .append(" kcal</b> rất sát với mục tiêu đề ra. Cố gắng phát huy nhé! <br/><br/>");
        }

        // 2. Goal Met Days
        if (goalMetCount > 0) {
            insight.append("📅 Trong <b>").append(dayCount).append(" ngày</b> vừa qua, bạn đã hoàn thành mục tiêu <b>").append(goalMetCount)
                   .append(" lần</b>. Sự kiên trì chính là chìa khóa để đạt được vóc dáng mơ ước! <br/><br/>");
        }

        // 3. Nutrients & Recommendations
        insight.append("🥗 <b>Lời khuyên dinh dưỡng:</b><br/>");
        boolean hasSpecificMacroAdvice = false;

        // Protein
        if (userGoal.targetProtein - avgP > 15) {
            insight.append("• Bạn nên bổ sung thêm khoảng <b>").append((int)(userGoal.targetProtein - avgP))
                   .append("g protein</b> mỗi ngày. Các món như ức gà, trứng hay các loại đậu sẽ là lựa chọn tuyệt vời. <br/>");
            hasSpecificMacroAdvice = true;
        }

        // Carbs
        if (userGoal.targetCarbs - avgC > 30) {
            insight.append("• Có vẻ lượng tinh bột hơi thấp, bạn có thể thêm chút khoai lang hoặc yến mạch để có thêm năng lượng nhé. <br/>");
            hasSpecificMacroAdvice = true;
        } else if (avgC > userGoal.targetCarbs + 50) {
            insight.append("• Lượng tinh bột hơi dư, bạn nên hạn chế bớt đồ ngọt và tinh bột trắng để cơ thể cảm thấy nhẹ nhàng hơn. <br/>");
            hasSpecificMacroAdvice = true;
        }

        // Fat
        if (userGoal.targetFat - avgF > 10) {
            insight.append("• Đừng quên thêm một chút chất béo tốt từ hạt hoặc dầu ô-liu để hỗ trợ làn da và sức khỏe bạn nhé. <br/>");
            hasSpecificMacroAdvice = true;
        }

        if (!hasSpecificMacroAdvice) {
            insight.append("• Tỷ lệ các nhóm chất của bạn đang ở mức rất cân bằng. Rất tốt, hãy cứ tiếp tục như vậy nhé! ✨");
        }

        textStatsInsight.setText(android.text.Html.fromHtml(insight.toString(), android.text.Html.FROM_HTML_MODE_LEGACY));
    }
}
