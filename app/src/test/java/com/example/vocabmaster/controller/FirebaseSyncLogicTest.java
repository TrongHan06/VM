package com.example.vocabmaster.controller;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FirebaseSyncLogicTest {

    private DailyProgressManager manager;

    @Before
    public void setUp() {
        manager = new DailyProgressManager(new InMemorySharedPreferences());
    }

    @Test
    public void buildCustomVocabDocKey_isStableNormalizedAndDistinct() {
        String key1 = FirebaseSyncController.buildCustomVocabDocKey(1, "Apple");
        String key2 = FirebaseSyncController.buildCustomVocabDocKey(1, "  apple ");
        String keyDifferentTopic = FirebaseSyncController.buildCustomVocabDocKey(2, "Apple");
        String keyDifferentWord = FirebaseSyncController.buildCustomVocabDocKey(1, "Banana");

        assertNotNull(key1);
        assertEquals(64, key1.length());
        assertEquals(key1, key2);
        assertNotEquals(key1, keyDifferentTopic);
        assertNotEquals(key1, keyDifferentWord);

        assertNull(FirebaseSyncController.buildCustomVocabDocKey(1, null));
        assertNull(FirebaseSyncController.buildCustomVocabDocKey(1, "   "));
    }

    @Test
    public void settingsSyncDirection_respectsTimestampsAndNewDeviceDefaults() {
        // Fresh device (localUpdatedAt == 0) vs existing cloud doc (cloudUpdatedAt == 5000) -> pull
        assertTrue(FirebaseSyncController.shouldPullSettings(true, 5000L, 0L));
        assertFalse(FirebaseSyncController.shouldPushSettings(true, 5000L, 0L));

        // Missing cloud doc -> push
        assertFalse(FirebaseSyncController.shouldPullSettings(false, 0L, 0L));
        assertTrue(FirebaseSyncController.shouldPushSettings(false, 0L, 0L));

        // Local newer than cloud -> push
        assertFalse(FirebaseSyncController.shouldPullSettings(true, 5000L, 8000L));
        assertTrue(FirebaseSyncController.shouldPushSettings(true, 5000L, 8000L));

        // Equal timestamps -> no-op
        assertFalse(FirebaseSyncController.shouldPullSettings(true, 5000L, 5000L));
        assertFalse(FirebaseSyncController.shouldPushSettings(true, 5000L, 5000L));
    }

    @Test
    public void applyCloudSettings_mergesAllFieldsAndSchedulesReminder() {
        manager.setTodayWordsCount(3);
        manager.setTotalQuizCompleted(6);
        manager.setSettingsUpdatedAt(0L);

        AtomicBoolean scheduled = new AtomicBoolean(false);
        AtomicBoolean cancelled = new AtomicBoolean(false);

        long cloudUpdatedAt = 987654321L;
        FirebaseSyncController.applyCloudSettings(
                manager,
                30,
                "UK",
                1.25f,
                manager.getTodayDate(),
                12,
                4, // local is 6 -> Math.max keeps 6
                7,
                45,
                true,
                cloudUpdatedAt,
                () -> scheduled.set(true),
                () -> cancelled.set(true)
        );

        assertEquals(30, manager.getDailyGoal());
        assertEquals("UK", manager.getTtsLocale());
        assertEquals(1.25f, manager.getTtsSpeed(), 0.001f);
        assertEquals(12, manager.getTodayWordsCount());
        assertEquals(6, manager.getTotalQuizCompleted());
        assertEquals(7, manager.getReminderHour());
        assertEquals(45, manager.getReminderMinute());
        assertTrue(manager.isReminderEnabled());
        assertTrue(scheduled.get());
        assertFalse(cancelled.get());
        assertEquals(cloudUpdatedAt, manager.getSettingsUpdatedAt());
    }

    @Test
    public void applyCloudSettings_skipsStaleTodayWordsAndCancelsDisabledReminder() {
        manager.setTodayWordsCount(2);
        manager.setTotalQuizCompleted(1);

        AtomicBoolean scheduled = new AtomicBoolean(false);
        AtomicBoolean cancelled = new AtomicBoolean(false);

        long cloudUpdatedAt = 555555L;
        FirebaseSyncController.applyCloudSettings(
                manager,
                20,
                "US",
                0.75f,
                "1999-12-31", // stale date from cloud
                19,
                9,
                20,
                15,
                false,
                cloudUpdatedAt,
                () -> scheduled.set(true),
                () -> cancelled.set(true)
        );

        assertEquals(20, manager.getDailyGoal());
        assertEquals(2, manager.getTodayWordsCount());
        assertEquals(9, manager.getTotalQuizCompleted());
        assertFalse(manager.isReminderEnabled());
        assertFalse(scheduled.get());
        assertTrue(cancelled.get());
        assertEquals(cloudUpdatedAt, manager.getSettingsUpdatedAt());
    }
}
