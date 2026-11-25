package com.example.vocabmaster.controller;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyProgressManager {
    private static final String PREF_NAME = "vocab_daily_progress";
    private static final String KEY_DATE = "TODAY_DATE";
    private static final String KEY_WORDS_COUNT = "TODAY_WORDS_COUNT";
    private static final String KEY_DAILY_GOAL = "DAILY_GOAL";
    private static final String KEY_QUIZ_COMPLETED = "TOTAL_QUIZ_COMPLETED";
    private static final String KEY_TTS_LOCALE = "TTS_LOCALE";
    private static final String KEY_TTS_SPEED = "TTS_SPEED";
    private static final String KEY_REMINDER_HOUR = "REMINDER_HOUR";
    private static final String KEY_REMINDER_MINUTE = "REMINDER_MINUTE";
    private static final String KEY_REMINDER_ENABLED = "REMINDER_ENABLED";
    private static final String KEY_SETTINGS_UPDATED_AT = "SETTINGS_UPDATED_AT";

    private final SharedPreferences prefs;

    //hàm khởi tạo
    public DailyProgressManager(Context context) {
        this(context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE));
    }

    DailyProgressManager(SharedPreferences prefs) {
        this.prefs = prefs;
        checkAndResetDate();
    }

    private String getCurrentDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date());
    }

    //kiểm tra ngày và reset số từ đã học
    private void checkAndResetDate() {
        String today = getCurrentDateString();
        String savedDate = prefs.getString(KEY_DATE, "");
        if (!today.equals(savedDate)) {
            prefs.edit()
                    .putString(KEY_DATE, today)
                    .putInt(KEY_WORDS_COUNT, 0)
                    .apply();
        }
    }

    public String getTodayDate() {
        checkAndResetDate();
        return prefs.getString(KEY_DATE, getCurrentDateString());
    }

    public long getSettingsUpdatedAt() {
        return prefs.getLong(KEY_SETTINGS_UPDATED_AT, 0L);
    }

    public void setSettingsUpdatedAt(long updatedAt) {
        if (updatedAt < 0L) return;
        prefs.edit().putLong(KEY_SETTINGS_UPDATED_AT, updatedAt).apply();
    }

    //lấy số lượng người dùng đã lật thẻ trong ngày
    public int getTodayWordsCount() {
        checkAndResetDate();
        return prefs.getInt(KEY_WORDS_COUNT, 0);
    }

    public void setTodayWordsCount(int count) {
        if (count < 0) return;
        prefs.edit()
                .putString(KEY_DATE, getCurrentDateString())
                .putInt(KEY_WORDS_COUNT, count)
                .putLong(KEY_SETTINGS_UPDATED_AT, System.currentTimeMillis())
                .apply();
    }

    //hàm tăng số lượng từ vựng lật thẻ trong ngày
    public void incrementTodayWordsCount() {
        checkAndResetDate();
        int current = prefs.getInt(KEY_WORDS_COUNT, 0);
        prefs.edit()
                .putInt(KEY_WORDS_COUNT, current + 1)
                .putLong(KEY_SETTINGS_UPDATED_AT, System.currentTimeMillis())
                .apply();
    }

    //hàm lấy mục tiêu từ vựng mỗi ngày
    public int getDailyGoal() {
        return prefs.getInt(KEY_DAILY_GOAL, 10);
    }

    //thay đổi mục tiêu học tập
    public void setDailyGoal(int goal) {
        if (goal <= 0) return;
        prefs.edit()
                .putInt(KEY_DAILY_GOAL, goal)
                .putLong(KEY_SETTINGS_UPDATED_AT, System.currentTimeMillis())
                .apply();
    }

    //lấy số quiz đã hoàn thành
    public int getTotalQuizCompleted() {
        return prefs.getInt(KEY_QUIZ_COMPLETED, 0);
    }

    public void setTotalQuizCompleted(int count) {
        if (count < 0) return;
        prefs.edit()
                .putInt(KEY_QUIZ_COMPLETED, count)
                .putLong(KEY_SETTINGS_UPDATED_AT, System.currentTimeMillis())
                .apply();
    }

    //tăng số quiz đã hoàn thành
    public void incrementQuizCompleted() {
        int current = getTotalQuizCompleted();
        prefs.edit()
                .putInt(KEY_QUIZ_COMPLETED, current + 1)
                .putLong(KEY_SETTINGS_UPDATED_AT, System.currentTimeMillis())
                .apply();
    }

    //reset mucj tiêu hằng ngày
    public void resetAllDailyProgress() {
        prefs.edit()
                .putString(KEY_DATE, getCurrentDateString())
                .putInt(KEY_WORDS_COUNT, 0)
                .putInt(KEY_QUIZ_COMPLETED, 0)
                .putLong(KEY_SETTINGS_UPDATED_AT, System.currentTimeMillis())
                .apply();
    }

    //tùy chọn giọng tts us/uk
    public String getTtsLocale() {
        return prefs.getString(KEY_TTS_LOCALE, "US");
    }

    public void setTtsLocale(String locale) {
        if (locale == null || locale.trim().isEmpty()) return;
        prefs.edit()
                .putString(KEY_TTS_LOCALE, locale.trim())
                .putLong(KEY_SETTINGS_UPDATED_AT, System.currentTimeMillis())
                .apply();
    }

    //lấy giongj đọc us/uk
    public float getTtsSpeed() {
        return prefs.getFloat(KEY_TTS_SPEED, 1.0f);
    }

    // chọn giọng đọc us/uk
    public void setTtsSpeed(float speed) {
        if (speed <= 0f) return;
        prefs.edit()
                .putFloat(KEY_TTS_SPEED, speed)
                .putLong(KEY_SETTINGS_UPDATED_AT, System.currentTimeMillis())
                .apply();
    }

    //LẤY giờ thông báo
    public int getReminderHour() {
        return prefs.getInt(KEY_REMINDER_HOUR, 20);
    }

    //lấy phút thông báo
    public int getReminderMinute() {
        return prefs.getInt(KEY_REMINDER_MINUTE, 0);
    }

    //kiểm tra trạng thái thông báo
    public boolean isReminderEnabled() {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, false);
    }

    //lưu thời gian thông báo
    public void setReminderTime(int hour, int minute, boolean enabled) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return;
        prefs.edit()
                .putInt(KEY_REMINDER_HOUR, hour)
                .putInt(KEY_REMINDER_MINUTE, minute)
                .putBoolean(KEY_REMINDER_ENABLED, enabled)
                .putLong(KEY_SETTINGS_UPDATED_AT, System.currentTimeMillis())
                .apply();
    }
}
