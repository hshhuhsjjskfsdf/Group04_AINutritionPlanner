package com.example.a23110035_23110060.helper;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.example.a23110035_23110060.data.local.FoodEntity;
import com.example.a23110035_23110060.model.NutritionInfo;
import com.example.a23110035_23110060.model.RecognitionResult;

import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class TensorFlowHelper {
    public static final String MODEL_MISSING_MESSAGE = "Model AI chưa được thêm. Vui lòng train và thay file app/src/main/assets/food_model.tflite. Bạn vẫn có thể nhập món thủ công.";
    private static final String MODEL_NAME = "food_model.tflite";

    private final Context context;

    public TensorFlowHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public RecognitionResult analyzeImage(String imagePathOrUri) {
        if (!assetExists(MODEL_NAME)) {
            return RecognitionResult.error(MODEL_MISSING_MESSAGE);
        }
        Interpreter interpreter = null;
        try {
            interpreter = new Interpreter(loadModelFile());
            int[] inputShape = interpreter.getInputTensor(0).shape();
            if (inputShape.length < 4) {
                return RecognitionResult.error("Model AI không đúng định dạng ảnh.");
            }
            int height = inputShape[1];
            int width = inputShape[2];
            int channels = inputShape[3];
            Bitmap bitmap = decodeBitmap(imagePathOrUri);
            if (bitmap == null) {
                return RecognitionResult.error("Không đọc được ảnh món ăn.");
            }
            ByteBuffer input = bitmapToInput(bitmap, width, height, channels);
            int outputSize = interpreter.getOutputTensor(0).shape()[1];
            float[][] output = new float[1][outputSize];
            interpreter.run(input, output);
            int bestIndex = 0;
            float bestConfidence = output[0][0];
            for (int i = 1; i < outputSize; i++) {
                if (output[0][i] > bestConfidence) {
                    bestConfidence = output[0][i];
                    bestIndex = i;
                }
            }
            List<String> labels = CsvImportHelper.readLabels(context);
            String label = bestIndex < labels.size() ? labels.get(bestIndex) : "unknown";
            return RecognitionResult.success(label, bestConfidence, findNutrition(label));
        } catch (Throwable throwable) {
            return RecognitionResult.error("Không thể phân tích ảnh: " + throwable.getMessage());
        } finally {
            if (interpreter != null) {
                interpreter.close();
            }
        }
    }

    private boolean assetExists(String name) {
        try {
            context.getAssets().openFd(name).close();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private MappedByteBuffer loadModelFile() throws Exception {
        AssetFileDescriptor descriptor = context.getAssets().openFd(MODEL_NAME);
        FileInputStream inputStream = new FileInputStream(descriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, descriptor.getStartOffset(), descriptor.getDeclaredLength());
    }

    private Bitmap decodeBitmap(String imagePathOrUri) {
        try {
            if (imagePathOrUri != null && (imagePathOrUri.startsWith("content://") || imagePathOrUri.startsWith("file://"))) {
                InputStream stream = context.getContentResolver().openInputStream(Uri.parse(imagePathOrUri));
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                if (stream != null) {
                    stream.close();
                }
                return bitmap;
            }
            return BitmapFactory.decodeFile(imagePathOrUri);
        } catch (Exception e) {
            return null;
        }
    }

    private ByteBuffer bitmapToInput(Bitmap source, int width, int height, int channels) {
        Bitmap bitmap = Bitmap.createScaledBitmap(source, width, height, true);
        int bytesPerChannel = 4;
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * channels * bytesPerChannel);
        buffer.order(ByteOrder.nativeOrder());
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            putChannel(buffer, r);
            if (channels > 1) {
                putChannel(buffer, g);
            }
            if (channels > 2) {
                putChannel(buffer, b);
            }
        }
        buffer.rewind();
        return buffer;
    }

    private void putChannel(ByteBuffer buffer, int value) {
        buffer.putFloat(value / 255.0f);
    }

    private NutritionInfo findNutrition(String label) {
        try {
            JSONObject root = CsvImportHelper.readNutritionJson(context);
            JSONObject item = root.optJSONObject(label);
            if (item != null) {
                return new NutritionInfo(
                        label,
                        item.optDouble("calories"),
                        item.optDouble("protein"),
                        item.optDouble("fat"),
                        item.optDouble("carbs"),
                        item.optString("serving")
                );
            }
        } catch (Exception ignored) {
        }
        for (FoodEntity food : CsvImportHelper.readFoodsFromAssets(context)) {
            if (food.dishName.equals(label)) {
                return new NutritionInfo(food.dishName, food.calories, food.protein, food.fat, food.carbs, food.serving);
            }
        }
        return new NutritionInfo(label, 0, 0, 0, 0, "");
    }
}
