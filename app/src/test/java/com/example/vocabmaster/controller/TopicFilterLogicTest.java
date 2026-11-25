package com.example.vocabmaster.controller;

import com.example.vocabmaster.model.entity.VocabEntity;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Kiem thu logic loc tu vung theo VocabFilterType (ALL, LEARNED, FAVORITE, WRONG, CUSTOM)
 * va selectedTopicId ma khong phu thuoc vao R.id hay magic number id > 120.
 */
public class TopicFilterLogicTest {

    @Test
    public void filterVocabsList_filtersAllTypesAndTopicIdAccurately() {
        VocabEntity v1 = buildVocab(1, 1, true, false, false);
        VocabEntity v2 = buildVocab(2, 1, false, true, false);
        VocabEntity v3 = buildVocab(3, 2, false, false, false); // wrong word
        // ID <= 120 nhung isCustom = true -> thuoc nhom CUSTOM
        VocabEntity v4 = buildVocab(40, 2, false, false, true);
        // ID > 120 nhung isCustom = false -> KHONG thuoc nhom CUSTOM
        VocabEntity v5 = buildVocab(150, 1, false, false, false);

        List<VocabEntity> all = Arrays.asList(v1, v2, v3, v4, v5);
        Set<Integer> wrongIds = new HashSet<>(Collections.singletonList(3));

        List<VocabEntity> allTopic1 = TopicController.filterVocabList(
                all, wrongIds, TopicController.VocabFilterType.ALL, 1);
        assertEquals(3, allTopic1.size());

        List<VocabEntity> learnedAll = TopicController.filterVocabList(
                all, wrongIds, TopicController.VocabFilterType.LEARNED, -1);
        assertEquals(1, learnedAll.size());
        assertEquals(1, learnedAll.get(0).getId());

        List<VocabEntity> favoriteTopic1 = TopicController.filterVocabList(
                all, wrongIds, TopicController.VocabFilterType.FAVORITE, 1);
        assertEquals(1, favoriteTopic1.size());
        assertEquals(2, favoriteTopic1.get(0).getId());

        List<VocabEntity> wrongAll = TopicController.filterVocabList(
                all, wrongIds, TopicController.VocabFilterType.WRONG, -1);
        assertEquals(1, wrongAll.size());
        assertEquals(3, wrongAll.get(0).getId());

        List<VocabEntity> customAll = TopicController.filterVocabList(
                all, wrongIds, TopicController.VocabFilterType.CUSTOM, -1);
        assertEquals(1, customAll.size());
        assertEquals(40, customAll.get(0).getId());

        List<VocabEntity> customTopic1 = TopicController.filterVocabList(
                all, wrongIds, TopicController.VocabFilterType.CUSTOM, 1);
        assertTrue(customTopic1.isEmpty());
    }

    @Test
    public void filterVocabsList_handlesNullOrEmptyInputsSafely() {
        List<VocabEntity> result = TopicController.filterVocabList(
                Collections.emptyList(), Collections.emptySet(),
                TopicController.VocabFilterType.ALL, -1
        );
        assertTrue(result.isEmpty());
    }

    private VocabEntity buildVocab(int id, int topicId, boolean learned,
                                   boolean favorite, boolean isCustom) {
        VocabEntity v = new VocabEntity(topicId, "Word" + id, "", "Nghia" + id,
                null, null, learned, favorite);
        v.setId(id);
        v.setCustom(isCustom);
        return v;
    }
}
