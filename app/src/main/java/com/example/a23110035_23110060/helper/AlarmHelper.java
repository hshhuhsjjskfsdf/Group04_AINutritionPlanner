package com.example.a23110035_23110060.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.a23110035_23110060.receiver.MealReminderReceiver;

import java.util.Calendar;

public class AlarmHelper {
    public static final String PREFS = "reminder_settings";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_BREAKFAST = "breakfast";
    public static final String KEY_LUNCH = "lunch";
    public static final String KEY_DINNER = "dinner";

    private AlarmHelper() {
    }

    public static void scheduleBreakfastReminder(Context context, String time) {
        scheduleReminder(context, time, 3101);
        saveTime(context, KEY_BREAKFAST, time);
    }

    public static void scheduleLunchReminder(Context context, String time) {
        scheduleReminder(context, time, 3102);
        saveTime(context, KEY_LUNCH, time);
    }

    public static void scheduleDinnerReminder(Context context, String time) {
        scheduleReminder(context, time, 3103);
        saveTime(context, KEY_DINNER, time);
    }

    public static void cancelReminders(Context context) {
        cancel(context, 3101);
        cancel(context, 3102);
        cancel(context, 3103);
        prefs(context).edit().putBoolean(KEY_ENABLED, false).apply();
    }

    public static void scheduleSavedReminders(Context context) {
        SharedPreferences preferences = prefs(context);
        if (!preferences.getBoolean(KEY_ENABLED, false)) {
            return;
        }
        scheduleReminder(context, preferences.getString(KEY_BREAKFAST, "07:00"), 3101);
        scheduleReminder(context, preferences.getString(KEY_LUNCH, "12:00"), 3102);
        scheduleReminder(context, preferences.getString(KEY_DINNER, "18:00"), 3103);
    }

    public static void saveEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    private static void scheduleReminder(Context context, String time, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        int[] parsed = parseTime(time);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, parsed[0]);
        calendar.set(Calendar.MINUTE, parsed[1]);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        PendingIntent pendingIntent = pendingIntent(context, requestCode);
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    private static void cancel(Context context, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent(context, requestCode));
        }
    }

    private static PendingIntent pendingIntent(Context context, int requestCode) {
        Intent intent = new Intent(context, MealReminderReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static void saveTime(Context context, String key, String time) {
        prefs(context).edit().putString(key, time).putBoolean(KEY_ENABLED, true).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static int[] parseTime(String time) {
        try {
            String[] parts = time.split(":");
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (Exception e) {
            return new int[]{7, 0};
        }
    }
}
