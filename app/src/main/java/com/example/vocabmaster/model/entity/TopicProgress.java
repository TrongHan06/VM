package com.example.vocabmaster.model.entity;

public class TopicProgress {

    private int totalWords;
    private int flashcardCount;
    private int quizCount;

    public TopicProgress(int totalWords, int flashcardCount, int quizCount) {
        this.totalWords = totalWords;
        this.flashcardCount = flashcardCount;
        this.quizCount = quizCount;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public void setTotalWords(int totalWords) {
        this.totalWords = totalWords;
    }

    public int getFlashcardCount() {
        return flashcardCount;
    }

    public void setFlashcardCount(int flashcardCount) {
        this.flashcardCount = flashcardCount;
    }

    public int getQuizCount() {
        return quizCount;
    }

    public void setQuizCount(int quizCount) {
        this.quizCount = quizCount;
    }

    public int getFlashcardPercent() {
        if (totalWords <= 0) return 0;
        return (int) Math.min(50, Math.round((flashcardCount * 50.0) / totalWords));
    }

    public int getQuizPercent() {
        if (totalWords <= 0) return 0;
        return (int) Math.min(50, Math.round((quizCount * 50.0) / totalWords));
    }

    public int getTotalProgressPercent() {
        return getFlashcardPercent() + getQuizPercent();
    }

    public int getProgressPercent() {
        return getTotalProgressPercent();
    }
}
