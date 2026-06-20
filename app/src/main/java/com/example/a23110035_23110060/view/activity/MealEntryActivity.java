package com.example.a23110035_23110060.view.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.controller.MealEntryController;
import com.example.a23110035_23110060.data.local.FoodEntity;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.data.repository.RepositoryCallback;
import com.example.a23110035_23110060.helper.DateHelper;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.ImageHelper;
import com.example.a23110035_23110060.helper.ValidationHelper;
import com.example.a23110035_23110060.service.FoodAnalysisService;
import com.example.a23110035_23110060.view.adapter.FoodSearchAdapter;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MealEntryActivity extends AppCompatActivity {
    private static final int REQ_PICK_IMAGE = 4001;
    private static final int REQ_CAPTURE_IMAGE = 4002;
    private static final int REQ_CAMERA_PERMISSION = 4003;

    private MealEntryController controller;
    private FoodSearchAdapter foodAdapter;
    private ImageView imagePreview;
    private EditText editSearch;
    private EditText editFoodName;
    private EditText editCalories;
    private EditText editProtein;
    private EditText editCarbs;
    private EditText editFat;
    private EditText editServing;
    private Spinner spinnerMealType;
    private TextView textRecognitionResult;
    private View loadingView;
    private String imagePath;
    private String source = "MANUAL";

    private final BroadcastReceiver analysisReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleAnalysisResult(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_entry);
        controller = new MealEntryController(this);
        bindViews();
        setupSpinner();
        setupRecycler();
        setupClicks();
        setupSearch();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(FoodAnalysisService.ACTION_RESULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(analysisReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(analysisReceiver, filter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(analysisReceiver);
        } catch (Exception ignored) {
        }
    }

    private void bindViews() {
        imagePreview = findViewById(R.id.imagePreview);
        editSearch = findViewById(R.id.editSearchFood);
        editFoodName = findViewById(R.id.editFoodName);
        editCalories = findViewById(R.id.editCalories);
        editProtein = findViewById(R.id.editProtein);
        editCarbs = findViewById(R.id.editCarbs);
        editFat = findViewById(R.id.editFat);
        editServing = findViewById(R.id.editServing);
        spinnerMealType = findViewById(R.id.spinnerMealType);
        textRecognitionResult = findViewById(R.id.textRecognitionResult);
        loadingView = findViewById(R.id.loadingView);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Breakfast", "Lunch", "Dinner", "Snack"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealType.setAdapter(adapter);
    }

    private void setupRecycler() {
        RecyclerView recyclerView = findViewById(R.id.recyclerFoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodAdapter = new FoodSearchAdapter(this::fillFoodFields);
        recyclerView.setAdapter(foodAdapter);
    }

    private void setupClicks() {
        Button choose = findViewById(R.id.buttonChooseImage);
        Button capture = findViewById(R.id.buttonCaptureImage);
        Button analyze = findViewById(R.id.buttonAnalyzeFood);
        Button save = findViewById(R.id.buttonSaveMeal);
        choose.setOnClickListener(v -> chooseImage());
        capture.setOnClickListener(v -> captureImage());
        analyze.setOnClickListener(v -> analyzeImage());
        save.setOnClickListener(v -> saveMeal());
    }

    private void setupSearch() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                controller.searchFoods(s.toString(), new RepositoryCallback<List<FoodEntity>>() {
                    @Override
                    public void onSuccess(List<FoodEntity> result) {
                        runOnUiThread(() -> foodAdapter.submitList(result));
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> Toast.makeText(MealEntryActivity.this, message, Toast.LENGTH_SHORT).show());
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        controller.searchFoods("", new RepositoryCallback<List<FoodEntity>>() {
            @Override
            public void onSuccess(List<FoodEntity> result) {
                runOnUiThread(() -> foodAdapter.submitList(result));
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_PICK_IMAGE);
    }

    private void captureImage() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQ_CAPTURE_IMAGE);
    }

    private void analyzeImage() {
        if (imagePath == null || imagePath.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn hoặc chụp ảnh trước", Toast.LENGTH_SHORT).show();
            return;
        }
        loadingView.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, FoodAnalysisService.class);
        intent.putExtra(FoodAnalysisService.EXTRA_IMAGE_PATH, imagePath);
        startService(intent);
    }

    private void handleAnalysisResult(Intent intent) {
        loadingView.setVisibility(View.GONE);
        boolean success = intent.getBooleanExtra(FoodAnalysisService.EXTRA_SUCCESS, false);
        String message = intent.getStringExtra(FoodAnalysisService.EXTRA_MESSAGE);
        if (!success) {
            textRecognitionResult.setText(message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return;
        }
        source = "AI";
        String label = intent.getStringExtra(FoodAnalysisService.EXTRA_LABEL);
        double calories = intent.getDoubleExtra(FoodAnalysisService.EXTRA_CALORIES, 0);
        double protein = intent.getDoubleExtra(FoodAnalysisService.EXTRA_PROTEIN, 0);
        double carbs = intent.getDoubleExtra(FoodAnalysisService.EXTRA_CARBS, 0);
        double fat = intent.getDoubleExtra(FoodAnalysisService.EXTRA_FAT, 0);
        String serving = intent.getStringExtra(FoodAnalysisService.EXTRA_SERVING);
        editFoodName.setText(label);
        editCalories.setText(String.format(Locale.US, "%.0f", calories));
        editProtein.setText(String.format(Locale.US, "%.1f", protein));
        editCarbs.setText(String.format(Locale.US, "%.1f", carbs));
        editFat.setText(String.format(Locale.US, "%.1f", fat));
        editServing.setText(serving);
        textRecognitionResult.setText("AI: " + label + " - " + String.format(Locale.US, "%.0f kcal", calories));
    }

    private void fillFoodFields(FoodEntity food) {
        source = "MANUAL";
        editFoodName.setText(food.dishName);
        editCalories.setText(String.format(Locale.US, "%.0f", food.calories));
        editProtein.setText(String.format(Locale.US, "%.1f", food.protein));
        editCarbs.setText(String.format(Locale.US, "%.1f", food.carbs));
        editFat.setText(String.format(Locale.US, "%.1f", food.fat));
        editServing.setText(food.serving);
    }

    private void saveMeal() {
        String userId = FirebaseHelper.getCurrentUserId();
        String foodName = editFoodName.getText().toString().trim();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        if (foodName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên món ăn", Toast.LENGTH_SHORT).show();
            return;
        }
        MealLogEntity mealLog = new MealLogEntity();
        mealLog.mealLogId = UUID.randomUUID().toString();
        mealLog.userId = userId;
        mealLog.foodName = foodName;
        mealLog.mealType = spinnerMealType.getSelectedItem().toString();
        mealLog.calories = ValidationHelper.parseDoubleOrZero(editCalories.getText().toString());
        mealLog.protein = ValidationHelper.parseDoubleOrZero(editProtein.getText().toString());
        mealLog.carbs = ValidationHelper.parseDoubleOrZero(editCarbs.getText().toString());
        mealLog.fat = ValidationHelper.parseDoubleOrZero(editFat.getText().toString());
        mealLog.serving = editServing.getText().toString().trim();
        mealLog.ingredientsJson = "[]";
        mealLog.imagePath = imagePath;
        mealLog.imageUrl = "";
        mealLog.source = source;
        mealLog.logDate = DateHelper.today();
        loadingView.setVisibility(View.VISIBLE);
        controller.saveMeal(mealLog, new RepositoryCallback<MealLogEntity>() {
            @Override
            public void onSuccess(MealLogEntity result) {
                runOnUiThread(() -> {
                    loadingView.setVisibility(View.GONE);
                    Toast.makeText(MealEntryActivity.this, "Đã lưu bữa ăn", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    loadingView.setVisibility(View.GONE);
                    Toast.makeText(MealEntryActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        if (requestCode == REQ_PICK_IMAGE && data.getData() != null) {
            Uri uri = data.getData();
            imagePreview.setImageURI(uri);
            imagePath = ImageHelper.copyUriToCache(this, uri);
        } else if (requestCode == REQ_CAPTURE_IMAGE) {
            Object bitmap = data.getExtras() == null ? null : data.getExtras().get("data");
            if (bitmap instanceof Bitmap) {
                imagePreview.setImageBitmap((Bitmap) bitmap);
                imagePath = ImageHelper.saveBitmapToCache(this, (Bitmap) bitmap);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            captureImage();
        }
    }
}
