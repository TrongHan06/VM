package com.example.vocabmaster.controller;

import com.example.vocabmaster.model.entity.TopicEntity;
import com.example.vocabmaster.model.entity.VocabEntity;

import java.util.List;

public class FlashcardController {

    private final TopicEntity topic;
    private final TopicController topicController;
    private final DailyProgressManager dailyProgressManager;
    private final List<VocabEntity> vocabs;
    private int currentIndex = 0;
     //hàm khởi tạo luồng học flashcard
    public FlashcardController(TopicEntity topic, TopicController topicController, DailyProgressManager dailyProgressManager) {
        this.topic = topic;
        this.topicController = topicController;
        this.dailyProgressManager = dailyProgressManager;
        this.vocabs = topicController.getVocabsForTopic(topic != null ? topic.getTitle() : "");
    }
    // đánh dấu card hiện tại là đã học
    public void markCurrentAsLearned() {
        VocabEntity current = getCurrentVocab();
        if (current != null && topicController != null) {
            topicController.markVocabAsLearned(current.getId(), current.getTopicId(), dailyProgressManager);
        }
    }

    public TopicEntity getTopic() {
        return topic;
    }

    public VocabEntity getCurrentVocab() {
        if (vocabs != null && !vocabs.isEmpty() && currentIndex >= 0 && currentIndex < vocabs.size()) {
            return vocabs.get(currentIndex);
        }
        return new VocabEntity(1, "Family", "/ˈfæməli/", "Gia đình", "I love my family.", "Tôi yêu gia đình tôi.", false, false);
    }

    public boolean nextVocab() {
        markCurrentAsLearned();
        if (currentIndex < vocabs.size() - 1) {
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
