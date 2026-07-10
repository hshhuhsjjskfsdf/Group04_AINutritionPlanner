package com.example.a23110035_23110060.view.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
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
import com.example.a23110035_23110060.helper.CsvImportHelper;
import com.example.a23110035_23110060.helper.ValidationHelper;
import com.example.a23110035_23110060.service.FoodAnalysisService;
import com.example.a23110035_23110060.view.adapter.FoodSearchAdapter;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MealEntryActivity extends AppCompatActivity {
    private static final int REQ_PICK_IMAGE = 4001;
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
    private RadioGroup groupMealType;
    private TextView textCameraResultTitle;
    private TextView textCameraResultSubtitle;
    private TextView textRecognitionResult;
    private View loadingView;
    private String imagePath;
    private String source = "MANUAL";

    private ImageView capturedImagePreview;
    private View btnRetake;
    private View btnShutter;
    private View layoutMacroResults;
    private TextView textResultCalories;
    private TextView textResultProtein;
    private TextView textResultCarbs;
    private TextView textResultFat;
    private Button buttonSaveCameraMeal;
    private View panelManualEntry;
    private View btnCloseManual;
    private Button buttonAnalyzeSelectedFood;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private boolean isFlashOn = false;
    private Camera camera;

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
        cameraExecutor = Executors.newSingleThreadExecutor();
        bindViews();
        setupMealTypeChips();
        setupRecycler();
        setupClicks();
        setupSearch();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
        }
    }

    private boolean allPermissionsGranted() {
        return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setFlashMode(isFlashOn ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF)
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build();

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Không thể khởi động máy ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getCacheDir(), "camera_" + UUID.randomUUID().toString() + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        loadingView.setVisibility(View.VISIBLE);
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                loadingView.setVisibility(View.GONE);
                imagePath = photoFile.getAbsolutePath();
                runOnUiThread(() -> {
                    capturedImagePreview.setImageURI(Uri.fromFile(photoFile));
                    capturedImagePreview.setVisibility(View.VISIBLE);
                    btnRetake.setVisibility(View.VISIBLE);
                    btnShutter.setVisibility(View.GONE);
                    buttonAnalyzeSelectedFood.setVisibility(View.VISIBLE);
                    buttonSaveCameraMeal.setVisibility(View.GONE);
                    layoutMacroResults.setVisibility(View.GONE);
                    showRecognitionResult("Ảnh đã sẵn sàng", "Bấm 'Phân tích AI' để nhận diện.");
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                loadingView.setVisibility(View.GONE);
                Toast.makeText(MealEntryActivity.this, "Lỗi khi chụp ảnh: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFlash() {
        if (imageCapture == null) return;
        isFlashOn = !isFlashOn;
        imageCapture.setFlashMode(isFlashOn ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF);
        if (camera != null) {
            camera.getCameraControl().enableTorch(isFlashOn);
        }
        Toast.makeText(this, isFlashOn ? "Đã bật Flash" : "Đã tắt Flash", Toast.LENGTH_SHORT).show();
    }

    private void switchCamera() {
        lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK) ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
        startCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
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
        previewView = findViewById(R.id.preview_view_camera);
        imagePreview = findViewById(R.id.imagePreview); // Legacy bindings
        editSearch = findViewById(R.id.editSearchFood);
        editFoodName = findViewById(R.id.editFoodName);
        editCalories = findViewById(R.id.editCalories);
        editProtein = findViewById(R.id.editProtein);
        editCarbs = findViewById(R.id.editCarbs);
        editFat = findViewById(R.id.editFat);
        editServing = findViewById(R.id.editServing);
        groupMealType = findViewById(R.id.groupMealType);
        textCameraResultTitle = findViewById(R.id.text_camera_result_title);
        textCameraResultSubtitle = findViewById(R.id.text_camera_result_subtitle);
        textRecognitionResult = findViewById(R.id.textRecognitionResult);
        loadingView = findViewById(R.id.loadingView);
        
        capturedImagePreview = findViewById(R.id.captured_image_preview);
        btnRetake = findViewById(R.id.btn_retake_photo);
        btnShutter = findViewById(R.id.btn_shutter);
        layoutMacroResults = findViewById(R.id.layout_macro_results);
        textResultCalories = findViewById(R.id.text_result_calories);
        textResultProtein = findViewById(R.id.text_result_protein);
        textResultCarbs = findViewById(R.id.text_result_carbs);
        textResultFat = findViewById(R.id.text_result_fat);
        buttonSaveCameraMeal = findViewById(R.id.buttonSaveCameraMeal);
        panelManualEntry = findViewById(R.id.panel_manual_entry);
        btnCloseManual = findViewById(R.id.btn_close_manual);
        buttonAnalyzeSelectedFood = findViewById(R.id.buttonAnalyzeSelectedFood);
    }

    private void setupMealTypeChips() {
        groupMealType.check(R.id.radioBreakfast);
    }

    private void setupRecycler() {
        RecyclerView recyclerView = findViewById(R.id.recyclerFoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodAdapter = new FoodSearchAdapter(this::fillFoodFields);
        recyclerView.setAdapter(foodAdapter);
    }

    private void setupClicks() {
        Button save = findViewById(R.id.buttonSaveMeal);
        save.setOnClickListener(v -> saveMeal());
        if (buttonAnalyzeSelectedFood != null) {
            buttonAnalyzeSelectedFood.setOnClickListener(v -> analyzeImage());
        }
        if (buttonSaveCameraMeal != null) {
            buttonSaveCameraMeal.setOnClickListener(v -> saveMeal());
        }

        View gallery = findViewById(R.id.btn_open_gallery);
        View manual = findViewById(R.id.btn_manual_entry);
        View flash = findViewById(R.id.btn_flash_toggle);
        View switchCamera = findViewById(R.id.btn_switch_camera);
        View closeCamera = findViewById(R.id.btn_close_camera);
        if (closeCamera != null) {
            closeCamera.setOnClickListener(v -> finish());
        }
        if (btnShutter != null) {
            btnShutter.setOnClickListener(v -> takePhoto());
        }
        if (btnRetake != null) {
            btnRetake.setOnClickListener(v -> retakePhoto());
        }
        if (gallery != null) {
            gallery.setOnClickListener(v -> chooseImage());
        }
        if (manual != null) {
            manual.setOnClickListener(v -> panelManualEntry.setVisibility(View.VISIBLE));
        }
        if (btnCloseManual != null) {
            btnCloseManual.setOnClickListener(v -> panelManualEntry.setVisibility(View.GONE));
        }
        if (flash != null) {
            flash.setOnClickListener(v -> toggleFlash());
        }
        if (switchCamera != null) {
            switchCamera.setOnClickListener(v -> switchCamera());
        }
    }

    private void retakePhoto() {
        imagePath = null;
        capturedImagePreview.setVisibility(View.GONE);
        btnRetake.setVisibility(View.GONE);
        btnShutter.setVisibility(View.VISIBLE);
        layoutMacroResults.setVisibility(View.GONE);
        buttonSaveCameraMeal.setVisibility(View.GONE);
        buttonAnalyzeSelectedFood.setVisibility(View.VISIBLE);
        showRecognitionResult("Phân tích bữa ăn", "Nhấn Chụp để bắt đầu, sau đó bấm Phân tích.");
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
            showRecognitionResult("AI: Unknown", message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return;
        }
        source = "AI";
        String label = intent.getStringExtra(FoodAnalysisService.EXTRA_LABEL);
        String displayLabel = normalizeDisplayLabel(label);
        double calories = intent.getDoubleExtra(FoodAnalysisService.EXTRA_CALORIES, 0);
        double protein = intent.getDoubleExtra(FoodAnalysisService.EXTRA_PROTEIN, 0);
        double carbs = intent.getDoubleExtra(FoodAnalysisService.EXTRA_CARBS, 0);
        double fat = intent.getDoubleExtra(FoodAnalysisService.EXTRA_FAT, 0);
        String serving = intent.getStringExtra(FoodAnalysisService.EXTRA_SERVING);
        editFoodName.setText(displayLabel);
        editCalories.setText(String.format(Locale.US, "%.0f", calories));
        editProtein.setText(String.format(Locale.US, "%.1f", protein));
        editCarbs.setText(String.format(Locale.US, "%.1f", carbs));
        editFat.setText(String.format(Locale.US, "%.1f", fat));
        editServing.setText(serving);
        
        // Cập nhật giao diện thẻ kết quả Camera
        if (layoutMacroResults != null) {
            layoutMacroResults.setVisibility(View.VISIBLE);
            buttonSaveCameraMeal.setVisibility(View.VISIBLE);
            buttonAnalyzeSelectedFood.setVisibility(View.GONE);
            textResultCalories.setText(String.format(Locale.US, "%.0f", calories));
            textResultProtein.setText(String.format(Locale.US, "%.1fg", protein));
            textResultCarbs.setText(String.format(Locale.US, "%.1fg", carbs));
            textResultFat.setText(String.format(Locale.US, "%.1fg", fat));
        }
        
        String servingText = serving == null || serving.trim().isEmpty() ? "" : "Khẩu phần: " + serving.trim();
        showRecognitionResult("AI: " + displayLabel, servingText);
    }

    private void fillFoodFields(FoodEntity food) {
        source = "MANUAL";
        String displayLabel = CsvImportHelper.formatFoodLabel(food.dishName);
        editFoodName.setText(displayLabel);
        editCalories.setText(String.format(Locale.US, "%.0f", food.calories));
        editProtein.setText(String.format(Locale.US, "%.1f", food.protein));
        editCarbs.setText(String.format(Locale.US, "%.1f", food.carbs));
        editFat.setText(String.format(Locale.US, "%.1f", food.fat));
        editServing.setText(food.serving);
        showRecognitionResult(displayLabel, formatNutritionSummary(food.calories, food.protein, food.carbs, food.fat, food.serving));
    }

    private String normalizeDisplayLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            return "Unknown";
        }
        return label.trim();
    }

    private String formatNutritionSummary(double calories, double protein, double carbs, double fat, String serving) {
        String servingText = serving == null || serving.trim().isEmpty() ? "" : " | " + serving.trim();
        return String.format(Locale.US, "%.0f kcal | Protein %.1fg | Carbs %.1fg | Fat %.1fg%s",
                calories, protein, carbs, fat, servingText);
    }

    private void showRecognitionResult(String title, String subtitle) {
        if (textRecognitionResult != null) {
            textRecognitionResult.setText(title + "\n" + subtitle);
        }
        if (textCameraResultTitle != null) {
            textCameraResultTitle.setText(title);
        }
        if (textCameraResultSubtitle != null) {
            textCameraResultSubtitle.setText(subtitle);
        }
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
        mealLog.mealType = selectedMealType();
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

    private String selectedMealType() {
        int checkedId = groupMealType.getCheckedRadioButtonId();
        if (checkedId == R.id.radioLunch) {
            return "Lunch";
        }
        if (checkedId == R.id.radioDinner) {
            return "Dinner";
        }
        if (checkedId == R.id.radioSnack) {
            return "Snack";
        }
        return "Breakfast";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        if (requestCode == REQ_PICK_IMAGE && data.getData() != null) {
            Uri uri = data.getData();
            imagePath = ImageHelper.copyUriToCache(this, uri);
            
            capturedImagePreview.setImageURI(uri);
            capturedImagePreview.setVisibility(View.VISIBLE);
            btnRetake.setVisibility(View.VISIBLE);
            btnShutter.setVisibility(View.GONE);
            buttonAnalyzeSelectedFood.setVisibility(View.VISIBLE);
            buttonSaveCameraMeal.setVisibility(View.GONE);
            layoutMacroResults.setVisibility(View.GONE);
            showRecognitionResult("Đã chọn ảnh", "Bấm 'Phân tích AI' để nhận diện.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }
}
