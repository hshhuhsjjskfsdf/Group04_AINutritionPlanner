package com.example.a23110035_23110060.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.a23110035_23110060.helper.DateHelper;
import com.example.a23110035_23110060.helper.FirebaseHelper;
import com.example.a23110035_23110060.helper.NotificationHelper;
import com.example.a23110035_23110060.data.local.AppDatabase;
import com.example.a23110035_23110060.data.local.MealPlanEntity;

import java.util.Calendar;
import java.util.List;

public class PreparationReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final BroadcastReceiver.PendingResult pendingResult = goAsync();
        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) {
            pendingResult.finish();
            return;
        }

        // Get tomorrow's date
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        String dateStr = DateHelper.formatDate(tomorrow.getTime());

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                List<MealPlanEntity> plans = db.mealPlanDao().getByUserAndDate(userId, dateStr);
                
                if (plans != null && !plans.isEmpty()) {
                    StringBuilder menu = new StringBuilder("Kế hoạch mai có: ");
                    for (int i = 0; i < Math.min(plans.size(), 3); i++) {
                        menu.append(plans.get(i).foodName);
                        if (i < Math.min(plans.size(), 3) - 1) menu.append(", ");
                    }
                    if (plans.size() > 3) menu.append("...");
                    
                    NotificationHelper.showGeneralNotification(
                        context, 
                        2002, 
                        "Chuẩn bị cho ngày mai", 
                        menu.toString()
                    );
                }
            } finally {
                pendingResult.finish();
            }
        }).start();
    }
}
