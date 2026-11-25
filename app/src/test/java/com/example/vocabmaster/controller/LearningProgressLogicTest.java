package com.example.vocabmaster.controller;

import com.example.vocabmaster.model.entity.PronunciationResult;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LearningProgressLogicTest {

    private LearningProgressController controller;

    @Before
    public void setUp() {
        controller = new LearningProgressController(null);
    }

    @Test
    public void evaluatePronunciation_emptyRecognizedText_shouldNotBeAlmostCorrect() {
        PronunciationResult emptyResult = controller.evaluatePronunciation("Hardware", "");
        assertFalse(emptyResult.isCorrect());
        assertEquals("#DC2626", emptyResult.getColorHex());
        assertEquals("Chưa chính xác! Thử lại xem sao nhé.", emptyResult.getStatusMessage());

        PronunciationResult whitespaceResult = controller.evaluatePronunciation("Hardware", "   ");
        assertFalse(whitespaceResult.isCorrect());
        assertEquals("#DC2626", whitespaceResult.getColorHex());

        PronunciationResult nullResult = controller.evaluatePronunciation("Hardware", null);
        assertFalse(nullResult.isCorrect());
        assertEquals("#DC2626", nullResult.getColorHex());
    }

    @Test
    public void evaluatePronunciation_singleCharOrShortNoise_shouldNotBeAlmostCorrect() {
        PronunciationResult singleChar = controller.evaluatePronunciation("Hardware", "a");
        assertFalse(singleChar.isCorrect());
        assertEquals("#DC2626", singleChar.getColorHex());

        PronunciationResult shortSubstring = controller.evaluatePronunciation("Hardware", "re");
        assertFalse(shortSubstring.isCorrect());
        assertEquals("#DC2626", shortSubstring.getColorHex());
    }

    @Test
    public void evaluatePronunciation_exactMatch_returnsCorrect() {
        PronunciationResult exact = controller.evaluatePronunciation("Hardware", "  hardware ");
        assertTrue(exact.isCorrect());
        assertEquals("#16A34A", exact.getColorHex());
        assertEquals("Chính xác 100%!", exact.getStatusMessage());
    }

    @Test
    public void evaluatePronunciation_containsTargetOrHighSimilarity_returnsAlmostCorrect() {
        PronunciationResult sentenceMatch = controller.evaluatePronunciation("Hardware", "the hardware device");
        assertFalse(sentenceMatch.isCorrect());
        assertEquals("#D97706", sentenceMatch.getColorHex());
        assertEquals("Gần đúng! Phát âm chuẩn hơn nữa nhé.", sentenceMatch.getStatusMessage());

        PronunciationResult closeSpelling = controller.evaluatePronunciation("Hardware", "hardwar");
        assertFalse(closeSpelling.isCorrect());
        assertEquals("#D97706", closeSpelling.getColorHex());
    }

    @Test
    public void evaluatePronunciation_emptyOrNullTarget_returnsIncorrect() {
        PronunciationResult emptyTarget = controller.evaluatePronunciation("", "Hardware");
        assertFalse(emptyTarget.isCorrect());
        assertEquals("#DC2626", emptyTarget.getColorHex());

        PronunciationResult nullTarget = controller.evaluatePronunciation(null, "Hardware");
        assertFalse(nullTarget.isCorrect());
        assertEquals("#DC2626", nullTarget.getColorHex());
    }
}
