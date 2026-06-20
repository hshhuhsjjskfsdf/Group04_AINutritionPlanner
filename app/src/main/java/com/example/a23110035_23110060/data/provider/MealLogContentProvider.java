package com.example.a23110035_23110060.data.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.a23110035_23110060.data.local.AppDatabase;
import com.example.a23110035_23110060.data.local.MealLogEntity;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MealLogContentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.example.a23110035_23110060.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/meal_logs");
    private static final int MEAL_LOGS = 1;
    private static final int MEAL_LOG_ID = 2;
    private static final String[] COLUMNS = {
            "mealLogId", "userId", "foodName", "mealType", "calories", "protein",
            "carbs", "fat", "serving", "ingredientsJson", "imageUrl", "imagePath",
            "source", "logDate", "isSynced", "createdAt", "updatedAt"
    };

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI(AUTHORITY, "meal_logs", MEAL_LOGS);
        MATCHER.addURI(AUTHORITY, "meal_logs/*", MEAL_LOG_ID);
    }

    private AppDatabase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public boolean onCreate() {
        if (getContext() == null) {
            return false;
        }
        database = AppDatabase.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return await(() -> {
            MatrixCursor cursor = new MatrixCursor(COLUMNS);
            if (MATCHER.match(uri) == MEAL_LOGS) {
                List<MealLogEntity> logs = database.mealLogDao().getAll();
                for (MealLogEntity log : logs) {
                    addRow(cursor, log);
                }
            } else if (MATCHER.match(uri) == MEAL_LOG_ID) {
                String id = uri.getLastPathSegment();
                for (MealLogEntity log : database.mealLogDao().getAll()) {
                    if (log.mealLogId.equals(id)) {
                        addRow(cursor, log);
                        break;
                    }
                }
            }
            return cursor;
        });
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        if (MATCHER.match(uri) == MEAL_LOG_ID) {
            return "vnd.android.cursor.item/vnd.com.example.a23110035_23110060.meal_log";
        }
        return "vnd.android.cursor.dir/vnd.com.example.a23110035_23110060.meal_log";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (MATCHER.match(uri) != MEAL_LOGS || values == null) {
            return null;
        }
        MealLogEntity entity = fromValues(values);
        if (entity.mealLogId == null || entity.mealLogId.isEmpty()) {
            entity.mealLogId = UUID.randomUUID().toString();
        }
        await(() -> {
            database.mealLogDao().insert(entity);
            return true;
        });
        notifyChange(CONTENT_URI);
        return Uri.withAppendedPath(CONTENT_URI, entity.mealLogId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (MATCHER.match(uri) != MEAL_LOG_ID) {
            return 0;
        }
        String id = uri.getLastPathSegment();
        await(() -> {
            database.mealLogDao().deleteById(id);
            return true;
        });
        notifyChange(CONTENT_URI);
        return 1;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        if (MATCHER.match(uri) != MEAL_LOG_ID || values == null) {
            return 0;
        }
        MealLogEntity entity = fromValues(values);
        entity.mealLogId = uri.getLastPathSegment();
        await(() -> {
            database.mealLogDao().insert(entity);
            return true;
        });
        notifyChange(CONTENT_URI);
        return 1;
    }

    private void addRow(MatrixCursor cursor, MealLogEntity log) {
        cursor.addRow(new Object[]{
                log.mealLogId, log.userId, log.foodName, log.mealType, log.calories, log.protein,
                log.carbs, log.fat, log.serving, log.ingredientsJson, log.imageUrl, log.imagePath,
                log.source, log.logDate, log.isSynced ? 1 : 0, log.createdAt, log.updatedAt
        });
    }

    private MealLogEntity fromValues(ContentValues values) {
        MealLogEntity entity = new MealLogEntity();
        entity.mealLogId = values.getAsString("mealLogId");
        entity.userId = values.getAsString("userId");
        entity.foodName = values.getAsString("foodName");
        entity.mealType = values.getAsString("mealType");
        entity.calories = getDouble(values, "calories");
        entity.protein = getDouble(values, "protein");
        entity.carbs = getDouble(values, "carbs");
        entity.fat = getDouble(values, "fat");
        entity.serving = values.getAsString("serving");
        entity.ingredientsJson = values.getAsString("ingredientsJson");
        entity.imageUrl = values.getAsString("imageUrl");
        entity.imagePath = values.getAsString("imagePath");
        entity.source = values.getAsString("source");
        entity.logDate = values.getAsString("logDate");
        Boolean synced = values.getAsBoolean("isSynced");
        entity.isSynced = synced != null && synced;
        Long createdAt = values.getAsLong("createdAt");
        Long updatedAt = values.getAsLong("updatedAt");
        entity.createdAt = createdAt == null ? System.currentTimeMillis() : createdAt;
        entity.updatedAt = updatedAt == null ? entity.createdAt : updatedAt;
        return entity;
    }

    private double getDouble(ContentValues values, String key) {
        Double value = values.getAsDouble(key);
        return value == null ? 0 : value;
    }

    private void notifyChange(Uri uri) {
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    private <T> T await(Callable<T> callable) {
        try {
            return executor.submit(callable).get();
        } catch (Exception e) {
            return null;
        }
    }
}
