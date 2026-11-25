package com.example.vocabmaster.controller;

import com.example.vocabmaster.model.entity.TopicEntity;
import com.example.vocabmaster.model.entity.VocabEntity;

import java.util.ArrayList;
import java.util.List;

public class FlashcardController {

    private final TopicEntity topic;
    private final TopicController topicController;
    private final DailyProgressManager dailyProgressManager;
    private final List<VocabEntity> vocabs;
    private int currentIndex = 0;

    public FlashcardController(TopicEntity topic, TopicController topicController, DailyProgressManager dailyProgressManager) {
        this.topic = topic;
        this.topicController = topicController;
        this.dailyProgressManager = dailyProgressManager;
        List<VocabEntity> list = topicController != null && topic != null ? topicController.getVocabsForTopic(topic.getTitle()) : null;
        this.vocabs = list != null ? list : new ArrayList<>();
    }

    public FlashcardController(List<VocabEntity> vocabs, DailyProgressManager dailyProgressManager) {
        this.topic = null;
        this.topicController = null;
        this.dailyProgressManager = dailyProgressManager;
        this.vocabs = vocabs != null ? vocabs : new ArrayList<>();
    }

    public void markCurrentAsLearned() {
        VocabEntity current = getCurrentVocab();
        if (current != null) {
            current.setLearned(true);
            if (topicController != null) {
                topicController.markVocabAsLearned(current.getId(), current.getTopicId(), dailyProgressManager);
            }
        }
    }

    public TopicEntity getTopic() {
        return topic;
    }

    public VocabEntity getCurrentVocab() {
        if (vocabs != null && !vocabs.isEmpty() && currentIndex >= 0 && currentIndex < vocabs.size()) {
            return vocabs.get(currentIndex);
        }
        return null;
    }

    public boolean nextVocab() {
        if (vocabs != null && currentIndex < vocabs.size() - 1) {
            currentIndex++;
            return true;
        }
        return false;
    }

    public boolean previousVocab() {
        if (currentIndex > 0) {
            currentIndex--;
            return true;
        }
        return false;
    }

    public boolean isLastVocab() {
        return vocabs == null || vocabs.isEmpty() || currentIndex == vocabs.size() - 1;
    }

    public boolean isFirstVocab() {
        return currentIndex == 0;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getTotalCount() {
        return vocabs != null ? vocabs.size() : 0;
    }
}
