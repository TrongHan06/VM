package com.example.vocabmaster.controller;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class SpeechController {

    private TextToSpeech textToSpeech;
    private boolean isTtsReady = false;
    private final DailyProgressManager dailyProgressManager;

    public SpeechController(Context context, DailyProgressManager dailyProgressManager) {
        this.dailyProgressManager = dailyProgressManager;
        initTextToSpeech(context);
    }

    private void initTextToSpeech(Context context) {
        textToSpeech = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true;
                applyTtsSettings();
            }
        });
    }

    public void applyTtsSettings() {
        if (textToSpeech == null || dailyProgressManager == null) return;
        String localeStr = dailyProgressManager.getTtsLocale();
        Locale locale = resolveLocale(localeStr);
        textToSpeech.setLanguage(locale);
        textToSpeech.setSpeechRate(dailyProgressManager.getTtsSpeed());
    }

    public void speak(String text) {
        if (text != null && textToSpeech != null && isTtsReady) {
            applyTtsSettings();
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public Intent createSpeechRecognitionIntent(String targetWord) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        Locale locale = resolveLocale(dailyProgressManager != null ? dailyProgressManager.getTtsLocale() : null);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toString());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy phát âm từ: " + targetWord);
        return intent;
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    /**
     * Resolves a locale code string ("UK" / "US") to a Locale.
     * Defaults to Locale.US for null, empty, or unrecognized codes.
     * Package-visible static helper used by SpeechAndReminderLogicTest.
     */
    public static Locale resolveLocale(String localeCode) {
        if ("UK".equalsIgnoreCase(localeCode)) return Locale.UK;
        return Locale.US;
    }
}
