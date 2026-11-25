package com.example.vocabmaster.model.entity;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Kiem thu cot is_custom (isCustom) thay the magic number id > 120.
 */
public class VocabEntityCustomFlagTest {

    @Test
    public void newVocabEntity_defaultIsCustomIsFalse() {
        VocabEntity vocab = new VocabEntity(
                1, "Algorithm", "/ˈælɡərɪðəm/", "Thuat toan",
                "This algorithm is fast.", "Thuat toan nay nhanh.",
                false, false
        );
        vocab.setId(150); // Du ID > 120 nhung chua setCustom(true) -> khong phai tu tuy chinh

        assertFalse(vocab.isCustom());
    }

    @Test
    public void setCustomTrue_marksVocabAsCustomRegardlessOfId() {
        VocabEntity vocab = new VocabEntity(
                1, "Refactor", "/ˌriːˈfæktər/", "Tai cau truc ma",
                "We refactored the controller.", "Chung toi da tai cau truc controller.",
                false, false
        );
        vocab.setId(12); // ID <= 120 nhung duoc gan isCustom = true
        vocab.setCustom(true);

        assertTrue(vocab.isCustom());
    }

    @Test
    public void setCustomFalse_unmarksCustomFlag() {
        VocabEntity vocab = new VocabEntity(
                2, "Migration", "/maɪˈɡreɪʃn/", "Di tru du lieu",
                null, null, false, false
        );
        vocab.setCustom(true);
        assertTrue(vocab.isCustom());

        vocab.setCustom(false);
        assertFalse(vocab.isCustom());
    }
}
