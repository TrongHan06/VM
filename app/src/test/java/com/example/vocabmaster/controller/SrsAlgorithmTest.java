package com.example.vocabmaster.controller;

import com.example.vocabmaster.model.entity.LearningProgressEntity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Kiem thu thuat toan SRS trong LearningProgressController:
 * - Tra loi sai: tang wrongCount, status = "LEARNING", nextReview = now + 1 gio (3_600_000L).
 * - Tra loi dung lan 1: status van giu "LEARNING", nextReview = now + 1 ngay.
 * - Tra loi dung >= 2 lan: status = "MASTERED".
 * - Khoang thoi gian on tap luy tien: 1 -> 3 -> 7 -> 14 -> 30 ngay (toi da 30 ngay).
 */
public class SrsAlgorithmTest {

    private static final long ONE_HOUR_MS = 3_600_000L;
    private static final long ONE_DAY_MS = 86_400_000L;
    private static final long NOW = 1_700_000_000_000L;

    @Test
    public void applySrsUpdate_firstCorrectAnswer_staysLearningWith1DayInterval() {
        LearningProgressEntity progress = new LearningProgressEntity(10, 0, 0, 0L, "NEW");

        LearningProgressController.applySrsResult(progress, true, NOW);

        assertEquals(1, progress.getCorrectCount());
        assertEquals(0, progress.getWrongCount());
        assertEquals("LEARNING", progress.getStatus());
        assertEquals(NOW + ONE_DAY_MS, progress.getNextReview());
    }

    @Test
    public void applySrsUpdate_secondCorrectAnswer_becomesMasteredWith3DaysInterval() {
        LearningProgressEntity progress = new LearningProgressEntity(10, 1, 0, NOW, "LEARNING");

        LearningProgressController.applySrsResult(progress, true, NOW);

        assertEquals(2, progress.getCorrectCount());
        assertEquals("MASTERED", progress.getStatus());
        assertEquals(NOW + 3 * ONE_DAY_MS, progress.getNextReview());
    }

    @Test
    public void applySrsUpdate_progressiveIntervals_1_3_7_14_30_andCapAt30Days() {
        long[] expectedIntervals = {
                ONE_DAY_MS,
                3 * ONE_DAY_MS,
                7 * ONE_DAY_MS,
                14 * ONE_DAY_MS,
                30 * ONE_DAY_MS,
                30 * ONE_DAY_MS,
                30 * ONE_DAY_MS
        };

        LearningProgressEntity progress = new LearningProgressEntity(10, 0, 0, 0L, "NEW");
        for (int i = 0; i < expectedIntervals.length; i++) {
            LearningProgressController.applySrsResult(progress, true, NOW);
            assertEquals("Step " + (i + 1), i + 1, progress.getCorrectCount());
            assertEquals("Interval step " + (i + 1), NOW + expectedIntervals[i], progress.getNextReview());
            String expectedStatus = (i + 1 >= 2) ? "MASTERED" : "LEARNING";
            assertEquals("Status step " + (i + 1), expectedStatus, progress.getStatus());
        }
    }

    @Test
    public void applySrsUpdate_wrongAnswer_demotesToLearningAndSchedules1Hour() {
        LearningProgressEntity progress = new LearningProgressEntity(10, 4, 1, NOW + 14 * ONE_DAY_MS, "MASTERED");

        LearningProgressController.applySrsResult(progress, false, NOW);

        assertEquals(2, progress.getWrongCount());
        assertEquals("LEARNING", progress.getStatus());
        assertEquals(NOW + ONE_HOUR_MS, progress.getNextReview());
    }
}
