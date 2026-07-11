package com.example.a23110035_23110060.helper;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.data.local.AppDatabase;
import com.example.a23110035_23110060.data.local.MealPlanEntity;
import com.example.a23110035_23110060.view.activity.MealEntryActivity;

import java.util.List;

public class NotificationHelper {
    public static final String CHANNEL_ID = "meal_reminders";

    private NotificationHelper() {
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Nhắc bữa ăn",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showMealReminderNotification(Context context, String mealType) {
        createNotificationChannel(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String title = "Đến giờ ghi nhận bữa ăn";
        String content = "Đừng quên cập nhật những gì bạn đã ăn để theo dõi tiến độ nhé.";

        // Attempt to find planned food for this meal type
        try {
            String userId = FirebaseHelper.getCurrentUserId();
            if (userId != null && mealType != null && !mealType.isEmpty()) {
                String today = DateHelper.today();
                AppDatabase db = AppDatabase.getInstance(context);
                // We use a thread because we're likely in a BroadcastReceiver (Main Thread)
                new Thread(() -> {
                    List<MealPlanEntity> plans = db.mealPlanDao().getByUserAndDate(userId, today);
                    String plannedFood = "";
                    for (MealPlanEntity p : plans) {
                        if (mealType.equalsIgnoreCase(p.mealType)) {
                            plannedFood = p.foodName;
                            break;
                        }
                    }

                    String finalContent = content;
                    if (!plannedFood.isEmpty()) {
                        finalContent = "Hôm nay bạn dự định ăn: " + plannedFood + ". Bấm để ghi nhận ngay!";
                    }
                    
                    sendNotification(context, title, finalContent);
                }).start();
                return;
            }
        } catch (Exception ignored) {}

        sendNotification(context, title, content);
    }

    public static void showGeneralNotification(Context context, int notificationId, String title, String content) {
        createNotificationChannel(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_plan)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    private static void sendNotification(Context context, String title, String content) {
        Intent intent = new Intent(context, MealEntryActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_diary)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(2001, builder.build());
        }
    }
}
