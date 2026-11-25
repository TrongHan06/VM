package com.example.vocabmaster.model.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Kiem thu cong thuc tien do thong nhat:
 * 50% Flashcard (isLearned = 1) + 50% Quiz (status = 'MASTERED').
 */
public class TopicProgressFormulaTest {

    @Test
    public void getProgressPercent_whenTotalWordsIsZero_returnsZero() {
        TopicProgressItem item = new TopicProgressItem(1, 0, 0, 0);
        assertEquals(0, item.getProgressPercent());
    }

    @Test
    public void getProgressPercent_allFlashcardsOnly_returns50() {
        TopicProgressItem item = new TopicProgressItem(1, 20, 20, 0);
        assertEquals(50, item.getProgressPercent());
    }

    @Test
    public void getProgressPercent_allQuizMasteredOnly_returns50() {
        TopicProgressItem item = new TopicProgressItem(1, 20, 0, 20);
        assertEquals(50, item.getProgressPercent());
    }

    @Test
    public void getProgressPercent_bothFlashcardsAndQuizComplete_returns100() {
        TopicProgressItem item = new TopicProgressItem(1, 20, 20, 20);
        assertEquals(100, item.getProgressPercent());
    }

    @Test
    public void getProgressPercent_partialProgress_calculatesWeighted5050() {
        // 10/20 flashcard = 25% + 4/20 quiz MASTERED = 10% => 35%
        TopicProgressItem item = new TopicProgressItem(2, 20, 10, 4);
        assertEquals(35, item.getProgressPercent());
    }

    @Test
    public void getProgressPercent_countsExceedingTotal_clampsTo100() {
        TopicProgressItem item = new TopicProgressItem(3, 10, 15, 12);
        assertEquals(100, item.getProgressPercent());
    }

    @Test
    public void getProgressPercent_negativeOrZeroProgress_neverBelowZero() {
        TopicProgressItem item = new TopicProgressItem(4, 10, 0, 0);
        assertEquals(0, item.getProgressPercent());
    }
}
