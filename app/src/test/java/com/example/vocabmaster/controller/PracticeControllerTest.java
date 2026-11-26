package com.example.vocabmaster.controller;

import com.example.vocabmaster.model.entity.VocabEntity;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PracticeControllerTest {

    private DailyProgressManager dailyProgressManager;

    @Before
    public void setUp() {
        dailyProgressManager = new DailyProgressManager(new InMemorySharedPreferences());
    }

    @Test
    public void generateOptions_filtersNullAndEmptyAndHandlesFewerThanFourWords() {
        VocabEntity v1 = new VocabEntity(1, "Apple", "/ˈæpl/", "Quả táo", null, null, false, false);
        VocabEntity v2 = new VocabEntity(1, "Banana", "/bəˈnɑːnə/", "Quả chuối", null, null, false, false);
        VocabEntity v3NullMeaning = new VocabEntity(1, "NullMeaning", "", null, null, null, false, false);
        VocabEntity v4BlankMeaning = new VocabEntity(1, "BlankMeaning", "", "   ", null, null, false, false);

        PracticeController controller = new PracticeController(
                null, false, null, null, dailyProgressManager,
                Arrays.asList(v1, v2, v3NullMeaning, v4BlankMeaning)
        );

        for (int i = 0; i < controller.getTotalQuestions(); i++) {
            List<String> options = controller.generateOptionsForCurrentQuestion();
            assertTrue(options.size() <= 2);
            for (String option : options) {
                assertFalse(option == null || option.trim().isEmpty());
            }
            controller.advanceToNextQuestion();
        }
    }

    @Test
    public void checkAnswer_handlesNullSelectedAnswerAndNullMeaningWithoutNpe() {
        VocabEntity nullMeaningWord = new VocabEntity(1, "Mystery", "", null, null, null, false, false);
        PracticeController nullController = new PracticeController(
                null, false, null, null, dailyProgressManager,
                Collections.singletonList(nullMeaningWord)
        );
        assertFalse(nullController.checkAnswer(null));
        assertFalse(nullController.checkAnswer("Bất kỳ"));

        VocabEntity validWord = new VocabEntity(1, "Book", "/bʊk/", "Quyển sách", null, null, false, false);
        PracticeController validController = new PracticeController(
                null, false, null, null, dailyProgressManager,
                Collections.singletonList(validWord)
        );
        assertFalse(validController.checkAnswer(null));
        assertTrue(validController.checkAnswer("Quyển sách"));
        assertEquals(1, validController.getCorrectAnswersCount());
    }

    @Test
    public void finishSession_isIdempotentAndIncrementsQuizCountOnlyOnce() {
        VocabEntity v1 = new VocabEntity(1, "Book", "/bʊk/", "Quyển sách", null, null, false, false);
        PracticeController controller = new PracticeController(
                null, false, null, null, dailyProgressManager,
                Collections.singletonList(v1)
        );

        controller.advanceToNextQuestion();
        controller.finishSession();
        controller.finishSession();
        controller.advanceToNextQuestion();

        assertEquals(1, dailyProgressManager.getTotalQuizCompleted());
    }

    @Test
    public void sessionTitle_isPreservedAndPassedCorrectly() {
        VocabEntity v1 = new VocabEntity(1, "Book", "/bʊk/", "Quyển sách", null, null, false, false);
        PracticeController srsController = new PracticeController(
                null, false, null, null, dailyProgressManager,
                Collections.singletonList(v1), "Ôn tập ngắt quãng (SRS)"
        );
        assertEquals("Ôn tập ngắt quãng (SRS)", srsController.getSessionTitle());

        PracticeController retryController = srsController.createRetryController();
        assertEquals("Ôn tập ngắt quãng (SRS)", retryController.getSessionTitle());
    }
}
