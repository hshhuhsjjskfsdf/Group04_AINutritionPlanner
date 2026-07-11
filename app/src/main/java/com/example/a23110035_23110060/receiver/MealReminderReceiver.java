package com.example.a23110035_23110060.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.a23110035_23110060.helper.NotificationHelper;

public class MealReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String mealType = intent.getStringExtra("meal_type");
        NotificationHelper.showMealReminderNotification(context, mealType);
    }
}
