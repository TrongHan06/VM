package com.example.vocabmaster.controller;

import com.example.vocabmaster.model.entity.VocabEntity;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Kiem thu FlashcardController:
 * - Danh sach rong tra ve null thay vi tao the gia "Family" (id = 0).
 * - Bam nextVocab() khong tu dong danh dau da hoc; chi danh dau khi nguoi dung lat the xem nghia.
 */
public class FlashcardControllerTest {

    @Test
    public void getCurrentVocab_whenListIsEmpty_returnsNull() {
        FlashcardController controller = new FlashcardController(Collections.emptyList(), null);

        assertNull(controller.getCurrentVocab());
        assertEquals(0, controller.getTotalCount());
        assertFalse(controller.nextVocab());
        assertFalse(controller.previousVocab());
    }

    @Test
    public void nextVocab_movesToNextCardWithoutMarkingCurrentAsLearned() {
        VocabEntity v1 = createVocab(1, "Apple", false);
        VocabEntity v2 = createVocab(2, "Banana", false);
        List<VocabEntity> list = Arrays.asList(v1, v2);

        FlashcardController controller = new FlashcardController(list, null);

        assertNotNull(controller.getCurrentVocab());
        assertEquals("Apple", controller.getCurrentVocab().getWord());

        boolean moved = controller.nextVocab();
        assertTrue(moved);
        assertEquals("Banana", controller.getCurrentVocab().getWord());
        // Khong tu dong danh dau da hoc chi vi bam Next
        assertFalse(v1.isLearned());
        assertFalse(v2.isLearned());
    }

    @Test
    public void markCurrentAsLearned_whenCardFlipped_marksOnlyCurrentCard() {
        VocabEntity v1 = createVocab(1, "Apple", false);
        VocabEntity v2 = createVocab(2, "Banana", false);

        FlashcardController controller = new FlashcardController(Arrays.asList(v1, v2), null);

        controller.markCurrentAsLearned();

        assertTrue(v1.isLearned());
        assertFalse(v2.isLearned());
    }

    @Test
    public void navigationBounds_respectFirstAndLastCard() {
        VocabEntity v1 = createVocab(1, "First", false);
        VocabEntity v2 = createVocab(2, "Second", false);

        FlashcardController controller = new FlashcardController(Arrays.asList(v1, v2), null);

        assertFalse(controller.previousVocab());
        assertEquals(0, controller.getCurrentIndex());

        assertTrue(controller.nextVocab());
        assertEquals(1, controller.getCurrentIndex());

        assertFalse(controller.nextVocab());
        assertEquals(1, controller.getCurrentIndex());

        assertTrue(controller.previousVocab());
        assertEquals(0, controller.getCurrentIndex());
    }

    private VocabEntity createVocab(int id, String word, boolean learned) {
        VocabEntity v = new VocabEntity(1, word, "", "Nghia " + word, null, null, learned, false);
        v.setId(id);
        return v;
    }
}
