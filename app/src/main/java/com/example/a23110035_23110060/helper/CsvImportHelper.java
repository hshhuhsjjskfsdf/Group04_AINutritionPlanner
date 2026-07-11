package com.example.a23110035_23110060.helper;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.example.a23110035_23110060.data.local.FoodEntity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CsvImportHelper {
    private static final String TAG = "CsvImportHelper";
    public static final String CSV_NAME = "nutrition_database.csv";

    private CsvImportHelper() {
    }

    public static List<FoodEntity> readFoodsFromAssets(Context context) {
        Map<String, FoodEntity> deduped = new LinkedHashMap<>();
        try {
            AssetManager assets = context.getAssets();
            BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(CSV_NAME), StandardCharsets.UTF_8));
            String line;
            boolean first = true;
            long now = System.currentTimeMillis();
            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                List<String> columns = parseCsvLine(line);
                if (columns.size() < 7) {
                    continue;
                }
                String name = columns.get(0).trim();
                if (name.isEmpty()) {
                    continue;
                }
                FoodEntity food = new FoodEntity();
                food.dishName = name;
                food.calories = parseDouble(columns.get(1));
                food.protein = parseDouble(columns.get(2));
                food.fat = parseDouble(columns.get(3));
                food.carbs = parseDouble(columns.get(4));
                food.serving = columns.get(5).trim();
                food.datasetSource = columns.get(6).trim();
                food.createdAt = now;
                food.updatedAt = now;
                deduped.put(name.toLowerCase(Locale.US), food);
            }
            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "Cannot read nutrition CSV", e);
        }
        return new ArrayList<>(deduped.values());
    }

    public static List<String> readLabels(Context context) {
        List<String> labels = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("labels.txt"), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    labels.add(line.trim());
                }
            }
            reader.close();
        } catch (Exception ignored) {
            for (FoodEntity food : readFoodsFromAssets(context)) {
                labels.add(food.dishName);
            }
        }
        return labels;
    }

    public static String normalizeFoodKey(String label) {
        if (label == null) {
            return "";
        }
        String asciiLabel = Normalizer.normalize(label.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceFirst("^\\d+\\s+", "");
        String normalized = asciiLabel
                .toLowerCase(Locale.US)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        return normalized;
    }

    public static String formatFoodLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            return "Unknown";
        }
        
        // Dictionary for Vietnamese food names that often appear in labels
        Map<String, String> vietNameseMap = new java.util.HashMap<>();
        vietNameseMap.put("banh_beo", "Bánh Bèo");
        vietNameseMap.put("banh_bot_loc", "Bánh Bột Lọc");
        vietNameseMap.put("banh_can", "Bánh Căn");
        vietNameseMap.put("banh_canh", "Bánh Canh");
        vietNameseMap.put("banh_chung", "Bánh Chưng");
        vietNameseMap.put("banh_cuon", "Bánh Cuốn");
        vietNameseMap.put("banh_duc", "Bánh Đúc");
        vietNameseMap.put("banh_gio", "Bánh Giò");
        vietNameseMap.put("banh_khot", "Bánh Khọt");
        vietNameseMap.put("banh_mi", "Bánh Mì");
        vietNameseMap.put("banh_pia", "Bánh Pía");
        vietNameseMap.put("banh_tet", "Bánh Tét");
        vietNameseMap.put("banh_trang_nuong", "Bánh Tráng Nướng");
        vietNameseMap.put("banh_xeo", "Bánh Xèo");
        vietNameseMap.put("bun_bo_hue", "Bún Bò Huế");
        vietNameseMap.put("bun_dau_mam_tom", "Bún Đậu Mắm Tôm");
        vietNameseMap.put("bun_mam", "Bún Mắm");
        vietNameseMap.put("bun_rieu", "Bún Rêu");
        vietNameseMap.put("bun_thit_nuong", "Bún Thịt Nướng");
        vietNameseMap.put("ca_kho_to", "Cá Kho Tộ");
        vietNameseMap.put("canh_chua", "Canh Chua");
        vietNameseMap.put("cao_lau", "Cao Lầu");
        vietNameseMap.put("chao_long", "Cháo Lòng");
        vietNameseMap.put("com_tam", "Cơm Tấm");
        vietNameseMap.put("goi_cuon", "Gỏi Cuốn");
        vietNameseMap.put("hu_tiu", "Hủ Tiếu");
        vietNameseMap.put("nem_ran", "Nem Rán");
        vietNameseMap.put("pho", "Phở");
        vietNameseMap.put("xoi_xien", "Xôi Xiên");

        String lowerLabel = label.trim().toLowerCase(Locale.US).replace(" ", "_");
        if (vietNameseMap.containsKey(lowerLabel)) {
            return vietNameseMap.get(lowerLabel);
        }

        String[] parts = label.trim().split("[_\\s]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase(Locale.US));
            }
        }
        return builder.length() == 0 ? "Unknown" : builder.toString();
    }

    public static JSONObject readNutritionJson(Context context) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("nutrition_data.json"), StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            return new JSONObject(builder.toString());
        } catch (Exception ignored) {
            JSONObject root = new JSONObject();
            try {
                for (FoodEntity food : readFoodsFromAssets(context)) {
                    JSONObject item = new JSONObject();
                    item.put("calories", food.calories);
                    item.put("protein", food.protein);
                    item.put("fat", food.fat);
                    item.put("carbs", food.carbs);
                    item.put("serving", food.serving);
                    root.put(food.dishName, item);
                }
            } catch (Exception e) {
                Log.e(TAG, "Cannot build fallback nutrition JSON", e);
            }
            return root;
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
