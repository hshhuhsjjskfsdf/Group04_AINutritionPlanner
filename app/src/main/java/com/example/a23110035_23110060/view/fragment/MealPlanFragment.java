package com.example.a23110035_23110060.view.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.example.a23110035_23110060.data.local.FoodEntity;
import com.example.a23110035_23110060.data.local.GoalEntity;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.data.local.MealPlanEntity;
import com.example.a23110035_23110060.data.repository.FoodRepository;
import com.example.a23110035_23110060.data.repository.GoalRepository;
import com.example.a23110035_23110060.data.repository.MealRepository;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.helper.DailyProgressCalculator;
import com.example.a23110035_23110060.helper.DateHelper;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.ValidationHelper;
import com.example.a23110035_23110060.view.adapter.FoodSearchAdapter;
import com.example.a23110035_23110060.view.adapter.MealPlanAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MealPlanFragment extends Fragment {

    private GoalRepository goalRepository;
    private MealRepository mealRepository;
    private FoodRepository foodRepository;

    private String userId;
    private Calendar currentPlanCalendar = Calendar.getInstance();
    private MealPlanAdapter adapterBreakfast, adapterLunch, adapterDinner, adapterSnack;
    private TextView textCurrentDate;
    private TextView textPlannedCalories, textTargetCaloriesPlan, textRemainingPlan;
    private ProgressBar progressPlanCalories;
    private TextView textPlannedProtein, textPlannedCarbs, textPlannedFat;
    private TextView textPlanSuggestion;
    private GoalEntity userGoal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        goalRepository = new GoalRepository(requireContext());
        mealRepository = new MealRepository(requireContext());
        foodRepository = new FoodRepository(requireContext());
        userId = FirebaseHelper.getCurrentUserId();

        bindViews(view);
        setupPlanTab(view);
    }

    private void bindViews(View view) {
        textCurrentDate = view.findViewById(R.id.textCurrentDate);
        textPlannedCalories = view.findViewById(R.id.textPlannedCalories);
        textTargetCaloriesPlan = view.findViewById(R.id.textTargetCaloriesPlan);
        textRemainingPlan = view.findViewById(R.id.textRemainingPlan);
        progressPlanCalories = view.findViewById(R.id.progressPlanCalories);
        textPlannedProtein = view.findViewById(R.id.textPlannedProtein);
        textPlannedCarbs = view.findViewById(R.id.textPlannedCarbs);
        textPlannedFat = view.findViewById(R.id.textPlannedFat);
        textPlanSuggestion = view.findViewById(R.id.textPlanSuggestion);

        view.findViewById(R.id.btnPrevDate).setOnClickListener(v -> goToPreviousDate());
        view.findViewById(R.id.btnNextDate).setOnClickListener(v -> goToNextDate());
        view.findViewById(R.id.btnToday).setOnClickListener(v -> goToToday());
        textCurrentDate.setOnClickListener(v -> showDatePicker());
        view.findViewById(R.id.btnCopyPlan).setOnClickListener(v -> showCopyPlanDialog());
    }

    private void setupPlanTab(View view) {
        adapterBreakfast = createPlanAdapter();
        adapterLunch = createPlanAdapter();
        adapterDinner = createPlanAdapter();
        adapterSnack = createPlanAdapter();

        setupSection(view, R.id.sectionBreakfast, "BỮA SÁNG", adapterBreakfast, "Breakfast");
        setupSection(view, R.id.sectionLunch, "BỮA TRƯA", adapterLunch, "Lunch");
        setupSection(view, R.id.sectionDinner, "BỮA TỐI", adapterDinner, "Dinner");
        setupSection(view, R.id.sectionSnack, "BỮA PHỤ", adapterSnack, "Snack");

        updatePlanDateText();
        loadPlanForDate();
    }

    private MealPlanAdapter createPlanAdapter() {
        return new MealPlanAdapter(new MealPlanAdapter.OnPlanActionListener() {
            @Override
            public void onDelete(MealPlanEntity plan) {
                mealRepository.deleteMealPlan(plan, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (getActivity() != null) getActivity().runOnUiThread(() -> loadPlanForDate());
                    }
                    @Override
                    public void onError(String message) {
                        showToast(message);
                    }
                });
            }

            @Override
            public void onToggleCompleted(MealPlanEntity plan, boolean completed) {
                mealRepository.updatePlanCompletion(plan.mealPlanId, completed, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (completed) {
                            if (getActivity() != null) getActivity().runOnUiThread(() -> askToAddToLog(plan));
                        } else {
                            if (getActivity() != null) getActivity().runOnUiThread(() -> loadPlanForDate());
                        }
                    }
                    @Override
                    public void onError(String message) {
                        showToast(message);
                    }
                });
            }

            @Override
            public void onEdit(MealPlanEntity plan) {
                showAddPlanDialog(plan, plan.mealType);
            }
        });
    }

    private void setupSection(View rootView, int id, String title, MealPlanAdapter adapter, String mealType) {
        View section = rootView.findViewById(id);
        ((TextView) section.findViewById(R.id.textMealSectionTitle)).setText(title);
        RecyclerView rv = section.findViewById(R.id.recyclerMealSection);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        section.findViewById(R.id.btnAddMealToSection).setOnClickListener(v -> showAddPlanDialog(null, mealType));
    }

    private void updatePlanDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        textCurrentDate.setText(sdf.format(currentPlanCalendar.getTime()));
    }

    private void goToPreviousDate() {
        currentPlanCalendar.add(Calendar.DAY_OF_YEAR, -1);
        updatePlanDateText();
        loadPlanForDate();
    }

    private void goToNextDate() {
        currentPlanCalendar.add(Calendar.DAY_OF_YEAR, 1);
        updatePlanDateText();
        loadPlanForDate();
    }

    private void goToToday() {
        currentPlanCalendar = Calendar.getInstance();
        updatePlanDateText();
        loadPlanForDate();
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            currentPlanCalendar.set(year, month, dayOfMonth);
            updatePlanDateText();
            loadPlanForDate();
        }, currentPlanCalendar.get(Calendar.YEAR), currentPlanCalendar.get(Calendar.MONTH), currentPlanCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadPlanForDate() {
        String dateStr = DateHelper.formatDate(currentPlanCalendar.getTime());
        goalRepository.getGoal(new RepositoryCallback<GoalEntity>() {
            @Override
            public void onSuccess(GoalEntity goal) {
                userGoal = goal != null ? goal : DailyProgressCalculator.defaultGoal();
                mealRepository.getPlansByDate(userId, dateStr, new RepositoryCallback<List<MealPlanEntity>>() {
                    @Override
                    public void onSuccess(List<MealPlanEntity> plans) {
                        if (getActivity() != null) getActivity().runOnUiThread(() -> updatePlanUI(plans));
                    }
                    @Override
                    public void onError(String message) {
                        showToast(message);
                    }
                });
            }
            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void updatePlanUI(List<MealPlanEntity> plans) {
        List<MealPlanEntity> breakfast = new ArrayList<>();
        List<MealPlanEntity> lunch = new ArrayList<>();
        List<MealPlanEntity> dinner = new ArrayList<>();
        List<MealPlanEntity> snack = new ArrayList<>();

        double totalCal = 0, totalP = 0, totalC = 0, totalF = 0;
        double breakfastCal = 0, lunchCal = 0, dinnerCal = 0, snackCal = 0;

        for (MealPlanEntity p : plans) {
            totalCal += p.calories;
            totalP += p.protein;
            totalC += p.carbs;
            totalF += p.fat;

            if ("Breakfast".equalsIgnoreCase(p.mealType)) {
                breakfast.add(p);
                breakfastCal += p.calories;
            } else if ("Lunch".equalsIgnoreCase(p.mealType)) {
                lunch.add(p);
                lunchCal += p.calories;
            } else if ("Dinner".equalsIgnoreCase(p.mealType)) {
                dinner.add(p);
                dinnerCal += p.calories;
            } else {
                snack.add(p);
                snackCal += p.calories;
            }
        }

        adapterBreakfast.submitList(breakfast);
        adapterLunch.submitList(lunch);
        adapterDinner.submitList(dinner);
        adapterSnack.submitList(snack);

        updateSectionCalories(R.id.sectionBreakfast, breakfastCal, breakfast.isEmpty());
        updateSectionCalories(R.id.sectionLunch, lunchCal, lunch.isEmpty());
        updateSectionCalories(R.id.sectionDinner, dinnerCal, dinner.isEmpty());
        updateSectionCalories(R.id.sectionSnack, snackCal, snack.isEmpty());

        updatePlanSummary(totalCal, totalP, totalC, totalF);
        generatePlanSuggestion(totalCal, totalP, plans);
    }

    private void updateSectionCalories(int sectionId, double calories, boolean empty) {
        View section = getView().findViewById(sectionId);
        if (section == null) return;
        ((TextView) section.findViewById(R.id.textMealSectionCalories)).setText(String.format(Locale.US, "%.0f kcal", calories));
        section.findViewById(R.id.textEmptyMealSection).setVisibility(empty ? View.VISIBLE : View.GONE);
        section.findViewById(R.id.recyclerMealSection).setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void updatePlanSummary(double cal, double p, double c, double f) {
        textPlannedCalories.setText(String.format(Locale.US, "%.0f", cal));
        textTargetCaloriesPlan.setText(String.format(Locale.US, "/ %.0f kcal", userGoal.targetCalories));

        int percent = (int) (userGoal.targetCalories > 0 ? (cal / userGoal.targetCalories * 100) : 0);
        progressPlanCalories.setProgress(Math.min(100, percent));

        double diff = userGoal.targetCalories - cal;
        if (diff >= 0) {
            textRemainingPlan.setText(String.format(Locale.US, "Còn lại %.0f kcal", diff));
            textRemainingPlan.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark));
        } else {
            textRemainingPlan.setText(String.format(Locale.US, "Đã vượt kế hoạch %.0f kcal", Math.abs(diff)));
            textRemainingPlan.setTextColor(ContextCompat.getColor(requireContext(), R.color.error));
        }

        textPlannedProtein.setText(String.format(Locale.US, "P: %.0f/%.0fg", p, userGoal.targetProtein));
        textPlannedCarbs.setText(String.format(Locale.US, "C: %.0f/%.0fg", c, userGoal.targetCarbs));
        textPlannedFat.setText(String.format(Locale.US, "F: %.0f/%.0fg", f, userGoal.targetFat));
    }

    private void generatePlanSuggestion(double cal, double p, List<MealPlanEntity> plans) {
        if (plans.isEmpty()) {
            textPlanSuggestion.setText("Bạn chưa lên kế hoạch cho ngày này.");
            return;
        }
        if (cal > userGoal.targetCalories) {
            textPlanSuggestion.setText("Kế hoạch hôm nay đã vượt mục tiêu " + (int)(cal - userGoal.targetCalories) + " kcal.");
            return;
        }
        if (userGoal.targetProtein - p > 30) {
            textPlanSuggestion.setText("Kế hoạch hôm nay còn thiếu khoảng " + (int)(userGoal.targetProtein - p) + " g protein.");
            return;
        }
        textPlanSuggestion.setText("Kế hoạch hôm nay đang khá cân đối.");
    }

    private void askToAddToLog(MealPlanEntity plan) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Đã ăn")
                .setMessage("Bạn có muốn thêm món này vào Nhật ký ngày " + plan.planDate + " không?")
                .setPositiveButton("Thêm", (dialog, which) -> {
                    MealLogEntity log = new MealLogEntity();
                    log.mealLogId = UUID.randomUUID().toString();
                    log.userId = userId;
                    log.logDate = plan.planDate;
                    log.mealType = plan.mealType;
                    log.foodName = plan.foodName;
                    log.calories = plan.calories;
                    log.protein = plan.protein;
                    log.carbs = plan.carbs;
                    log.fat = plan.fat;
                    log.serving = plan.portion;
                    log.isSynced = false;
                    log.createdAt = System.currentTimeMillis();

                    mealRepository.saveMealLog(log, new RepositoryCallback<MealLogEntity>() {
                        @Override
                        public void onSuccess(MealLogEntity result) {
                            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                                showToast("Đã thêm vào Nhật ký");
                                loadPlanForDate();
                            });
                        }
                        @Override
                        public void onError(String message) {
                            showToast(message);
                        }
                    });
                })
                .setNegativeButton("Không", (dialog, which) -> loadPlanForDate())
                .show();
    }

    private void showCopyPlanDialog() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar sourceCal = Calendar.getInstance();
            sourceCal.set(year, month, dayOfMonth);
            copyPlanFromDate(DateHelper.formatDate(sourceCal.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void copyPlanFromDate(String sourceDate) {
        String destDate = DateHelper.formatDate(currentPlanCalendar.getTime());
        if (sourceDate.equals(destDate)) {
            showToast("Không thể sao chép vào cùng một ngày");
            return;
        }

        mealRepository.getPlansByDate(userId, sourceDate, new RepositoryCallback<List<MealPlanEntity>>() {
            @Override
            public void onSuccess(List<MealPlanEntity> plans) {
                if (plans.isEmpty()) {
                    showToast("Ngày nguồn không có kế hoạch");
                    return;
                }
                for (MealPlanEntity p : plans) {
                    MealPlanEntity newPlan = new MealPlanEntity();
                    newPlan.mealPlanId = UUID.randomUUID().toString();
                    newPlan.userId = userId;
                    newPlan.planDate = destDate;
                    newPlan.mealType = p.mealType;
                    newPlan.foodName = p.foodName;
                    newPlan.portion = p.portion;
                    newPlan.calories = p.calories;
                    newPlan.protein = p.protein;
                    newPlan.carbs = p.carbs;
                    newPlan.fat = p.fat;
                    newPlan.note = p.note;
                    newPlan.isCompleted = false;
                    newPlan.createdAt = System.currentTimeMillis();
                    mealRepository.saveMealPlan(newPlan, null);
                }
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    showToast("Đã sao chép " + plans.size() + " món");
                    loadPlanForDate();
                });
            }
            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void showAddPlanDialog(MealPlanEntity planToEdit, String mealType) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_meal_plan, null);
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

        if (planToEdit != null) {
            title.setText("Sửa kế hoạch");
            editName.setText(planToEdit.foodName);
            editCal.setText(String.valueOf(planToEdit.calories));
            editPortion.setText(planToEdit.portion);
            editProt.setText(String.valueOf(planToEdit.protein));
            editCarb.setText(String.valueOf(planToEdit.carbs));
            editFat.setText(String.valueOf(planToEdit.fat));
            editNote.setText(planToEdit.note);
        }

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

            MealPlanEntity plan = planToEdit != null ? planToEdit : new MealPlanEntity();
            if (planToEdit == null) {
                plan.mealPlanId = UUID.randomUUID().toString();
                plan.userId = userId;
                plan.planDate = DateHelper.formatDate(currentPlanCalendar.getTime());
                plan.mealType = mealType;
                plan.createdAt = System.currentTimeMillis();
            }
            plan.foodName = name;
            plan.calories = ValidationHelper.parseDoubleOrZero(editCal.getText().toString());
            plan.protein = ValidationHelper.parseDoubleOrZero(editProt.getText().toString());
            plan.carbs = ValidationHelper.parseDoubleOrZero(editCarb.getText().toString());
            plan.fat = ValidationHelper.parseDoubleOrZero(editFat.getText().toString());
            plan.portion = editPortion.getText().toString().trim();
            plan.note = editNote.getText().toString().trim();

            mealRepository.saveMealPlan(plan, new RepositoryCallback<MealPlanEntity>() {
                @Override public void onSuccess(MealPlanEntity result) {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        dialog.dismiss();
                        loadPlanForDate();
                    });
                }
                @Override public void onError(String message) { showToast(message); }
            });
        });

        dialog.show();
    }

    private void showToast(String message) {
        if (getActivity() != null) getActivity().runOnUiThread(() -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
    }
}
