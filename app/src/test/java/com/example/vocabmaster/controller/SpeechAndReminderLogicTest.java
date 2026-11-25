package com.example.vocabmaster.controller;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Kiem thu cau hinh giong UK/US cho SpeechController va dieu kien dat lai nhac hoc sau reboot.
 */
public class SpeechAndReminderLogicTest {

    private DailyProgressManager dailyProgressManager;

    @Before
    public void setUp() {
        dailyProgressManager = new DailyProgressManager(new InMemorySharedPreferences());
    }

    @Test
    public void resolveSpeechLocale_matchesDailyProgressSettingUkAndUs() {
        dailyProgressManager.setTtsLocale("UK");
        assertEquals(Locale.UK, SpeechController.resolveLocale(dailyProgressManager.getTtsLocale()));

        dailyProgressManager.setTtsLocale("US");
        assertEquals(Locale.US, SpeechController.resolveLocale(dailyProgressManager.getTtsLocale()));
    }

    @Test
    public void resolveSpeechLocale_defaultsToUsOnUnknownOrNull() {
        assertEquals(Locale.US, SpeechController.resolveLocale(null));
        assertEquals(Locale.US, SpeechController.resolveLocale(""));
        assertEquals(Locale.US, SpeechController.resolveLocale("FR"));
    }

    @Test
    public void reminderBootRestoreCondition_onlyReschedulesWhenEnabled() {
        assertFalse(dailyProgressManager.isReminderEnabled());

        dailyProgressManager.setReminderTime(20, 45, true);
        assertTrue(dailyProgressManager.isReminderEnabled());
        assertEquals(20, dailyProgressManager.getReminderHour());
        assertEquals(45, dailyProgressManager.getReminderMinute());

        dailyProgressManager.setReminderTime(20, 45, false);
        assertFalse(dailyProgressManager.isReminderEnabled());
    }
}
