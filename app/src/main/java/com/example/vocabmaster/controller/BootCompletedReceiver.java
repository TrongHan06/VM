package com.example.vocabmaster.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            DailyProgressManager manager = new DailyProgressManager(context);
            if (manager.isReminderEnabled()) {
                ReminderScheduler.scheduleDailyReminder(
                        context,
                        manager.getReminderHour(),
                        manager.getReminderMinute()
                );
            }
        }
    }
}
