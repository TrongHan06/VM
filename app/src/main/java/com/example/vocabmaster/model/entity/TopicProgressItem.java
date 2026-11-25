package com.example.vocabmaster.model.entity;

public class TopicProgressItem {

    private final int topicId;
    private final int totalWords;
    private final int flashcardCount;
    private final int quizCount;

    public TopicProgressItem(int topicId, int totalWords, int flashcardCount, int quizCount) {
        this.topicId = topicId;
        this.totalWords = totalWords;
        this.flashcardCount = flashcardCount;
        this.quizCount = quizCount;
    }

    public int getTopicId() {
        return topicId;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public int getFlashcardCount() {
        return flashcardCount;
    }

    public int getQuizCount() {
        return quizCount;
    }

    public int getProgressPercent() {
        if (totalWords <= 0) return 0;
        int flashcardPercent = (int) Math.min(50, Math.round((flashcardCount * 50.0) / totalWords));
        int quizPercent = (int) Math.min(50, Math.round((quizCount * 50.0) / totalWords));
        int total = flashcardPercent + quizPercent;
        return Math.max(0, Math.min(100, total));
    }
}
