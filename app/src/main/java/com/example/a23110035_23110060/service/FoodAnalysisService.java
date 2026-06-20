package com.example.a23110035_23110060.service;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.a23110035_23110060.helper.TensorFlowHelper;
import com.example.a23110035_23110060.model.NutritionInfo;
import com.example.a23110035_23110060.model.RecognitionResult;

public class FoodAnalysisService extends IntentService {
    public static final String ACTION_ANALYZE = "com.example.a23110035_23110060.ANALYZE_FOOD";
    public static final String ACTION_RESULT = "com.example.a23110035_23110060.ANALYZE_RESULT";
    public static final String EXTRA_IMAGE_PATH = "image_path";
    public static final String EXTRA_SUCCESS = "success";
    public static final String EXTRA_LABEL = "label";
    public static final String EXTRA_CONFIDENCE = "confidence";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_CALORIES = "calories";
    public static final String EXTRA_PROTEIN = "protein";
    public static final String EXTRA_CARBS = "carbs";
    public static final String EXTRA_FAT = "fat";
    public static final String EXTRA_SERVING = "serving";

    public FoodAnalysisService() {
        super("FoodAnalysisService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }
        String imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH);
        RecognitionResult result = new TensorFlowHelper(this).analyzeImage(imagePath);
        Intent broadcast = new Intent(ACTION_RESULT);
        broadcast.setPackage(getPackageName());
        broadcast.putExtra(EXTRA_SUCCESS, result.success);
        broadcast.putExtra(EXTRA_LABEL, result.label);
        broadcast.putExtra(EXTRA_CONFIDENCE, result.confidence);
        broadcast.putExtra(EXTRA_MESSAGE, result.message);
        NutritionInfo info = result.nutritionInfo;
        if (info != null) {
            broadcast.putExtra(EXTRA_CALORIES, info.calories);
            broadcast.putExtra(EXTRA_PROTEIN, info.protein);
            broadcast.putExtra(EXTRA_CARBS, info.carbs);
            broadcast.putExtra(EXTRA_FAT, info.fat);
            broadcast.putExtra(EXTRA_SERVING, info.serving);
        }
        sendBroadcast(broadcast);
    }
}
