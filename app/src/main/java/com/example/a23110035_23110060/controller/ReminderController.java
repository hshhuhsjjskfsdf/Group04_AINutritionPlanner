package com.example.a23110035_23110060.controller;

import android.content.Context;

import com.example.a23110035_23110060.helper.AlarmHelper;

public class ReminderController {
    private final Context context;

    public ReminderController(Context context) {
        this.context = context.getApplicationContext();
    }

    public void scheduleReminders(String breakfastTime, String lunchTime, String dinnerTime) {
        AlarmHelper.saveEnabled(context, true);
        AlarmHelper.scheduleBreakfastReminder(context, breakfastTime);
        AlarmHelper.scheduleLunchReminder(context, lunchTime);
        AlarmHelper.scheduleDinnerReminder(context, dinnerTime);
    }

    public void cancelReminders() {
        AlarmHelper.cancelReminders(context);
    }
}
