package com.example.vocabmaster.controller;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class SpeechController {

    private TextToSpeech textToSpeech;
    private final DailyProgressManager dailyProgressManager;

    public SpeechController(Context context, DailyProgressManager dailyProgressManager) {
        this.dailyProgressManager = dailyProgressManager;
        initTextToSpeech(context);
    }

    private void initTextToSpeech(Context context) {
        textToSpeech = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                applyTtsSettings();
            }
        });
    }

    public void applyTtsSettings() {
        if (textToSpeech == null || dailyProgressManager == null) return;
        String localeStr = dailyProgressManager.getTtsLocale();
        Locale locale = "UK".equalsIgnoreCase(localeStr) ? Locale.UK : Locale.US;
        textToSpeech.setLanguage(locale);
        textToSpeech.setSpeechRate(dailyProgressManager.getTtsSpeed());
    }

    public void speak(String text) {
        if (text != null && textToSpeech != null) {
            applyTtsSettings();
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public Intent createSpeechRecognitionIntent(String targetWord) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString());
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
}
