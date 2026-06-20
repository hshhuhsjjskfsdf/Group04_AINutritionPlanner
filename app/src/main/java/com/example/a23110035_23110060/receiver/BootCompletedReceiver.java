package com.example.a23110035_23110060.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.a23110035_23110060.helper.AlarmHelper;
import com.example.a23110035_23110060.helper.NetworkHelper;
import com.example.a23110035_23110060.service.FirebaseSyncService;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            AlarmHelper.scheduleSavedReminders(context);
            if (NetworkHelper.isNetworkAvailable(context)) {
                try {
                    context.startService(new Intent(context, FirebaseSyncService.class));
                } catch (Exception ignored) {
                }
            }
        }
    }
}
