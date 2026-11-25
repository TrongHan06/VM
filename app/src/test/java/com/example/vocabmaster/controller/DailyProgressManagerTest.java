package com.example.vocabmaster.controller;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DailyProgressManagerTest {

    private InMemorySharedPreferences prefs;
    private DailyProgressManager manager;

    @Before
    public void setUp() {
        prefs = new InMemorySharedPreferences();
        manager = new DailyProgressManager(prefs);
    }

    @Test
    public void freshDevice_doesNotSetSettingsUpdatedAtOnInit() {
        assertEquals(0L, manager.getSettingsUpdatedAt());
        assertEquals(10, manager.getDailyGoal());
        assertEquals(0, manager.getTodayWordsCount());
        assertEquals(0, manager.getTotalQuizCompleted());
        assertEquals("US", manager.getTtsLocale());
        assertEquals(1.0f, manager.getTtsSpeed(), 0.001f);
        assertFalse(manager.isReminderEnabled());
    }

    @Test
    public void dateReset_preservesExistingSettingsUpdatedAt() {
        prefs.edit()
                .putString("TODAY_DATE", "2000-01-01")
                .putInt("TODAY_WORDS_COUNT", 7)
                .putLong("SETTINGS_UPDATED_AT", 12345L)
                .apply();

        DailyProgressManager reopened = new DailyProgressManager(prefs);
        assertEquals(0, reopened.getTodayWordsCount());
        assertEquals(12345L, reopened.getSettingsUpdatedAt());
    }

    @Test
    public void setters_updateSettingsUpdatedAtAndValidateInputs() {
        manager.setDailyGoal(25);
        long tGoal = manager.getSettingsUpdatedAt();
        assertTrue(tGoal > 0L);
        assertEquals(25, manager.getDailyGoal());

        manager.setDailyGoal(0);
        manager.setDailyGoal(-5);
        assertEquals(25, manager.getDailyGoal());

        manager.setTodayWordsCount(8);
        assertEquals(8, manager.getTodayWordsCount());
        assertEquals(manager.getTodayDate(), prefs.getString("TODAY_DATE", ""));
        manager.setTodayWordsCount(-1);
        assertEquals(8, manager.getTodayWordsCount());

        manager.setTotalQuizCompleted(4);
        assertEquals(4, manager.getTotalQuizCompleted());
        manager.setTotalQuizCompleted(-2);
        assertEquals(4, manager.getTotalQuizCompleted());

        manager.setTtsLocale("UK");
        assertEquals("UK", manager.getTtsLocale());
        manager.setTtsLocale("   ");
        manager.setTtsLocale(null);
        assertEquals("UK", manager.getTtsLocale());

        manager.setTtsSpeed(1.25f);
        assertEquals(1.25f, manager.getTtsSpeed(), 0.001f);
        manager.setTtsSpeed(0f);
        manager.setTtsSpeed(-1.0f);
        assertEquals(1.25f, manager.getTtsSpeed(), 0.001f);

        manager.setReminderTime(21, 30, true);
        assertEquals(21, manager.getReminderHour());
        assertEquals(30, manager.getReminderMinute());
        assertTrue(manager.isReminderEnabled());

        manager.setReminderTime(25, 0, false);
        manager.setReminderTime(10, 60, false);
        assertEquals(21, manager.getReminderHour());
        assertEquals(30, manager.getReminderMinute());
        assertTrue(manager.isReminderEnabled());
    }

    @Test
    public void incrementsAndReset_updateTimestamp() {
        manager.setSettingsUpdatedAt(100L);
        manager.incrementTodayWordsCount();
        assertEquals(1, manager.getTodayWordsCount());
        assertTrue(manager.getSettingsUpdatedAt() > 100L);

        manager.setSettingsUpdatedAt(100L);
        manager.incrementQuizCompleted();
        assertEquals(1, manager.getTotalQuizCompleted());
        assertTrue(manager.getSettingsUpdatedAt() > 100L);

        manager.setSettingsUpdatedAt(100L);
        manager.resetAllDailyProgress();
        assertEquals(0, manager.getTodayWordsCount());
        assertEquals(0, manager.getTotalQuizCompleted());
        assertTrue(manager.getSettingsUpdatedAt() > 100L);
    }

    @Test
    public void resetQuizCompleted_setsTotalQuizCompletedToZero() {
        manager.setTotalQuizCompleted(5);
        assertEquals(5, manager.getTotalQuizCompleted());

        manager.setTotalQuizCompleted(0);
        assertEquals(0, manager.getTotalQuizCompleted());
    }
}
