package com.example.vocabmaster.controller;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.vocabmaster.R;

/**
 * BroadcastReceiver nhận tín hiệu báo thức từ AlarmManager đến giờ nhắc nhở học từ vựng.
 * Tạo NotificationChannel và hiển thị thông báo nhắc học hằng ngày cho người dùng.
 */
public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "vocab_reminder_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) return;

        DailyProgressManager progressManager = new DailyProgressManager(context);
        int todayCount = progressManager.getTodayWordsCount();
        int dailyGoal = progressManager.getDailyGoal();

        String message;
        if (todayCount >= dailyGoal) {
            message = "Bạn đã hoàn thành mục tiêu " + todayCount + "/" + dailyGoal + " từ hôm nay. Hãy ôn thêm để nhớ lâu hơn nhé!";
        } else {
            message = "Bạn mới học " + todayCount + "/" + dailyGoal + " từ hôm nay. Hãy dành vài phút học thêm từ vựng nhé!";
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Nhắc nhở học từ vựng",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Kênh thông báo nhắc nhở học VocabMaster hằng ngày");
            notificationManager.createNotificationChannel(channel);
        }

        Intent clickIntent = new Intent(context, MainActivity.class);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chuong)
                .setContentTitle("Đã đến giờ học từ vựng!")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
