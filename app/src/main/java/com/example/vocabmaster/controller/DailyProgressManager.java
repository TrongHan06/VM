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

    private final SharedPreferences prefs;

    //hàm khởi tạo
     public DailyProgressManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        checkAndResetDate();
    }
    //kiểm tra ngày và reset số từ đã học
    private void checkAndResetDate() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String savedDate = prefs.getString(KEY_DATE, "");
        if (!today.equals(savedDate)) {
            prefs.edit()
                    .putString(KEY_DATE, today)
                    .putInt(KEY_WORDS_COUNT, 0)
                    .apply();
        }
    }
    //lấy số lượng người dùng đã lật thẻ trong ngày
    public int getTodayWordsCount() {
        checkAndResetDate();
        return prefs.getInt(KEY_WORDS_COUNT, 0);
    }
    //hàm tăng số lượng từ vựng lật thẻ trong ngày
    public void incrementTodayWordsCount() {
        checkAndResetDate();
        int current = getTodayWordsCount();
        prefs.edit().putInt(KEY_WORDS_COUNT, current + 1).apply();
    }
   //hàm lấy mục tiêu từ vựng mỗi ngày
    public int getDailyGoal() {
        return prefs.getInt(KEY_DAILY_GOAL, 10);
    }
    //thay đổi mục tiêu học tập
    public void setDailyGoal(int goal) {
        prefs.edit().putInt(KEY_DAILY_GOAL, goal).apply();
    }
  //lấy số quiz đã hoàn thành
    public int getTotalQuizCompleted() {
        return prefs.getInt(KEY_QUIZ_COMPLETED, 0);
    }
  //tăng số quiz đã hoàn thành
    public void incrementQuizCompleted() {
        int current = getTotalQuizCompleted();
        prefs.edit().putInt(KEY_QUIZ_COMPLETED, current + 1).apply();
    }
  //reset mucj tiêu hằng ngày
    public void resetAllDailyProgress() {
        prefs.edit()
                .putInt(KEY_WORDS_COUNT, 0)
                .putInt(KEY_QUIZ_COMPLETED, 0)
                .apply();
    }
    //tùy chọn giọng tts us/uk
    public String getTtsLocale() {
        return prefs.getString(KEY_TTS_LOCALE, "US");
    }

    public void setTtsLocale(String locale) {
        prefs.edit().putString(KEY_TTS_LOCALE, locale).apply();
    }
    //lấy giongj đọc us/uk
    public float getTtsSpeed() {
        return prefs.getFloat(KEY_TTS_SPEED, 1.0f);
    }
     // chọn giọng đọc us/uk
    public void setTtsSpeed(float speed) {
        prefs.edit().putFloat(KEY_TTS_SPEED, speed).apply();
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
        prefs.edit()
                .putInt(KEY_REMINDER_HOUR, hour)
                .putInt(KEY_REMINDER_MINUTE, minute)
                .putBoolean(KEY_REMINDER_ENABLED, enabled)
                .apply();
    }
}
