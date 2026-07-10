package com.example.a23110035_23110060.view.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.data.local.GoalEntity;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.data.repository.GoalRepository;
import com.example.a23110035_23110060.data.repository.MealRepository;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.helper.DailyProgressCalculator;
import com.example.a23110035_23110060.helper.DateHelper;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.view.adapter.DateAdapter;
import com.example.a23110035_23110060.view.adapter.CategorizedMealLogAdapter;
import com.example.a23110035_23110060.data.local.FoodEntity;
import com.example.a23110035_23110060.data.repository.FoodRepository;
import com.example.a23110035_23110060.helper.ValidationHelper;
import com.example.a23110035_23110060.view.activity.SettingsActivity;
import com.example.a23110035_23110060.view.adapter.FoodSearchAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.content.Intent;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MealLogFragment extends Fragment {

    private GoalRepository goalRepository;
    private MealRepository mealRepository;
    private FoodRepository foodRepository;
    private String userId;
    private Calendar selectedDate = Calendar.getInstance();
    private List<MealLogEntity> allLogsForDate = new ArrayList<>();
    
    private TextView textConsumedCalories, textTargetCalories, textRemainingCalories;
    private ProgressBar progressCalories;
    private TextView textProtein, textCarbs, textFat;
    private TextView textWeekRange, textSelectedFullDate;
    private RecyclerView recyclerDates;
    private DateAdapter dateAdapter;
    private Calendar currentWeekCalendar = Calendar.getInstance();
    private CategorizedMealLogAdapter adapter;
    private ChipGroup chipGroupFilters;
    private View layoutEmpty, loadingView, btnSetupGoal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        goalRepository = new GoalRepository(requireContext());
        mealRepository = new MealRepository(requireContext());
        foodRepository = new FoodRepository(requireContext());
        userId = FirebaseHelper.getCurrentUserId();
        
        initViews(view);
        setupDateNavigation(view);
        setupRecyclerView(view);
        setupFilters(view);
        
        btnSetupGoal.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), SettingsActivity.class));
        });

        loadData();
    }

    private void initViews(View view) {
        textConsumedCalories = view.findViewById(R.id.textConsumedCalories);
        textTargetCalories = view.findViewById(R.id.textTargetCalories);
        textRemainingCalories = view.findViewById(R.id.textRemainingCalories);
        progressCalories = view.findViewById(R.id.progressCalories);
        textProtein = view.findViewById(R.id.textProtein);
        textCarbs = view.findViewById(R.id.textCarbs);
        textFat = view.findViewById(R.id.textFat);
        
        textWeekRange = view.findViewById(R.id.textWeekRange);
        textSelectedFullDate = view.findViewById(R.id.textSelectedFullDate);
        recyclerDates = view.findViewById(R.id.recyclerDates);
        
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        loadingView = view.findViewById(R.id.loadingView);
        btnSetupGoal = view.findViewById(R.id.btnSetupGoal);
    }

    private void setupDateNavigation(View view) {
        view.findViewById(R.id.btnPrevWeek).setOnClickListener(v -> {
            currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, -1);
            updateWeekSelector();
        });
        view.findViewById(R.id.btnNextWeek).setOnClickListener(v -> {
            currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            updateWeekSelector();
        });

        dateAdapter = new DateAdapter(date -> {
            selectedDate = (Calendar) date.clone();
            loadData();
        });
        recyclerDates.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerDates.setAdapter(dateAdapter);
        
        updateWeekSelector();
    }

    private void updateWeekSelector() {
        List<Calendar> weekDates = new ArrayList<>();
        Calendar cal = (Calendar) currentWeekCalendar.clone();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        
        SimpleDateFormat rangeFormat = new SimpleDateFormat("MMM d", new Locale("vi", "VN"));
        String start = rangeFormat.format(cal.getTime());
        
        for (int i = 0; i < 7; i++) {
            weekDates.add((Calendar) cal.clone());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String end = rangeFormat.format(cal.getTime());
        textWeekRange.setText(start + " - " + end);
        
        dateAdapter.setDates(weekDates, selectedDate);
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            loadData();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.recyclerMealLogs);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CategorizedMealLogAdapter(new CategorizedMealLogAdapter.OnMealLogActionListener() {
            @Override
            public void onEdit(MealLogEntity log) {
                showEditDialog(log);
            }

            @Override
            public void onDelete(MealLogEntity log) {
                confirmDelete(log);
            }
        });
        rv.setAdapter(adapter);
    }

    private void setupFilters(View view) {
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            filterLogs();
        });
    }

    private void loadData() {
        updateDateText();
        if (loadingView != null) loadingView.setVisibility(View.VISIBLE);
        
        String dateStr = DateHelper.formatDate(selectedDate.getTime());
        
        goalRepository.getGoal(new RepositoryCallback<GoalEntity>() {
            @Override
            public void onSuccess(GoalEntity goal) {
                GoalEntity currentGoal = goal != null ? goal : DailyProgressCalculator.defaultGoal();
                mealRepository.getLogsByDate(userId, dateStr, new RepositoryCallback<List<MealLogEntity>>() {
                    @Override
                    public void onSuccess(List<MealLogEntity> logs) {
                        allLogsForDate = logs != null ? logs : new ArrayList<>();
                        if (getActivity() != null) getActivity().runOnUiThread(() -> {
                            if (loadingView != null) loadingView.setVisibility(View.GONE);
                            updateSummary(currentGoal, allLogsForDate);
                            filterLogs();
                            
                            if (goal == null) {
                                btnSetupGoal.setVisibility(View.VISIBLE);
                            } else {
                                btnSetupGoal.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        showToast(message);
                        if (getActivity() != null) getActivity().runOnUiThread(() -> {
                            if (loadingView != null) loadingView.setVisibility(View.GONE);
                        });
                    }
                });
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void updateDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM", new Locale("vi", "VN"));
        textSelectedFullDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void updateSummary(GoalEntity goal, List<MealLogEntity> logs) {
        double totalCal = 0, totalP = 0, totalC = 0, totalF = 0;
        for (MealLogEntity log : logs) {
            totalCal += log.calories;
            totalP += log.protein;
            totalC += log.carbs;
            totalF += log.fat;
        }

        textConsumedCalories.setText(String.format(Locale.US, "%.0f", totalCal));
        textTargetCalories.setText(String.format(Locale.US, "/ %.0f kcal", goal.targetCalories));
        
        int progress = (int) (goal.targetCalories > 0 ? (totalCal / goal.targetCalories * 100) : 0);
        progressCalories.setProgress(Math.min(100, progress));

        double remaining = goal.targetCalories - totalCal;
        if (remaining >= 0) {
            textRemainingCalories.setText(String.format(Locale.US, "Còn lại %.0f kcal", remaining));
            textRemainingCalories.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark));
        } else {
            textRemainingCalories.setText(String.format(Locale.US, "Đã vượt %.0f kcal", Math.abs(remaining)));
            textRemainingCalories.setTextColor(ContextCompat.getColor(requireContext(), R.color.error));
        }

        textProtein.setText(String.format(Locale.US, "%.0f / %.0f g", totalP, goal.targetProtein));
        textCarbs.setText(String.format(Locale.US, "%.0f / %.0f g", totalC, goal.targetCarbs));
        textFat.setText(String.format(Locale.US, "%.0f / %.0f g", totalF, goal.targetFat));
    }

    private void filterLogs() {
        int checkedId = chipGroupFilters.getCheckedChipId();
        List<MealLogEntity> filtered = new ArrayList<>();
        
        String filterType = "";
        if (checkedId == R.id.chipBreakfast) filterType = "Breakfast";
        else if (checkedId == R.id.chipLunch) filterType = "Lunch";
        else if (checkedId == R.id.chipDinner) filterType = "Dinner";
        else if (checkedId == R.id.chipSnack) filterType = "Snack";

        if (filterType.isEmpty()) {
            filtered.addAll(allLogsForDate);
        } else {
            for (MealLogEntity log : allLogsForDate) {
                if (filterType.equalsIgnoreCase(log.mealType)) {
                    filtered.add(log);
                }
            }
        }

        adapter.submitData(filtered);
        layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showEditDialog(MealLogEntity log) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_meal_plan, null); // Reuse the same layout
        dialog.setContentView(view);

        TextView title = view.findViewById(R.id.textDialogTitle);
        EditText editSearch = view.findViewById(R.id.editSearchFood);
        RecyclerView recyclerSearch = view.findViewById(R.id.recyclerFoodSearch);
        EditText editName = view.findViewById(R.id.editFoodName);
        EditText editCal = view.findViewById(R.id.editCalories);
        EditText editPortion = view.findViewById(R.id.editPortion);
        EditText editProt = view.findViewById(R.id.editProtein);
        EditText editCarb = view.findViewById(R.id.editCarbs);
        EditText editFat = view.findViewById(R.id.editFat);
        EditText editNote = view.findViewById(R.id.editNote);
        View btnSave = view.findViewById(R.id.btnSavePlan);

        title.setText("Sửa bữa ăn");
        editName.setText(log.foodName);
        editCal.setText(String.valueOf(log.calories));
        editPortion.setText(log.serving);
        editProt.setText(String.valueOf(log.protein));
        editCarb.setText(String.valueOf(log.carbs));
        editFat.setText(String.valueOf(log.fat));
        // note is not in MealLogEntity by default, but let's check if it exists in yours.
        // if (editNote != null) editNote.setText(log.note);

        FoodSearchAdapter searchAdapter = new FoodSearchAdapter(food -> {
            editName.setText(food.dishName);
            editCal.setText(String.valueOf(food.calories));
            editProt.setText(String.valueOf(food.protein));
            editCarb.setText(String.valueOf(food.carbs));
            editFat.setText(String.valueOf(food.fat));
            editPortion.setText(food.serving);
            recyclerSearch.setVisibility(View.GONE);
        });
        recyclerSearch.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerSearch.setAdapter(searchAdapter);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 1) {
                    foodRepository.searchFoods(s.toString(), new RepositoryCallback<List<FoodEntity>>() {
                        @Override public void onSuccess(List<FoodEntity> result) {
                            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                                searchAdapter.submitList(result);
                                recyclerSearch.setVisibility(result.isEmpty() ? View.GONE : View.VISIBLE);
                            });
                        }
                        @Override public void onError(String message) {}
                    });
                } else {
                    recyclerSearch.setVisibility(View.GONE);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSave.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            if (name.isEmpty()) {
                showToast("Vui lòng nhập tên món ăn");
                return;
            }

            log.foodName = name;
            log.calories = ValidationHelper.parseDoubleOrZero(editCal.getText().toString());
            log.protein = ValidationHelper.parseDoubleOrZero(editProt.getText().toString());
            log.carbs = ValidationHelper.parseDoubleOrZero(editCarb.getText().toString());
            log.fat = ValidationHelper.parseDoubleOrZero(editFat.getText().toString());
            log.serving = editPortion.getText().toString().trim();

            mealRepository.saveMealLog(log, new RepositoryCallback<MealLogEntity>() {
                @Override public void onSuccess(MealLogEntity result) {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        dialog.dismiss();
                        loadData();
                    });
                }
                @Override public void onError(String message) { showToast(message); }
            });
        });

        dialog.show();
    }

    private void confirmDelete(MealLogEntity log) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xóa bữa ăn")
                .setMessage("Bạn có chắc muốn xóa món này khỏi Nhật ký?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    mealRepository.deleteMealLog(log, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                                showToast("Đã xóa");
                                loadData();
                            });
                        }
                        @Override
                        public void onError(String message) {
                            showToast(message);
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showToast(String message) {
        if (getActivity() != null) getActivity().runOnUiThread(() -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
    }
}
