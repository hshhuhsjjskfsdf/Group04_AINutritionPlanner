package com.example.a23110035_23110060.model;

public class RecognitionResult {
    public boolean success;
    public String label;
    public float confidence;
    public String message;
    public NutritionInfo nutritionInfo;

    public static RecognitionResult error(String message) {
        RecognitionResult result = new RecognitionResult();
        result.success = false;
        result.message = message;
        return result;
    }

    public static RecognitionResult success(String label, float confidence, NutritionInfo nutritionInfo) {
        RecognitionResult result = new RecognitionResult();
        result.success = true;
        result.label = label;
        result.confidence = confidence;
        result.nutritionInfo = nutritionInfo;
        result.message = "Nhận diện thành công";
        return result;
    }
}
