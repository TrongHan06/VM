package com.example.vocabmaster.model.entity;

public class ProficiencyReport {
    private final String level;
    private final int accuracy;
    private final int masteredCount;
    private final int totalVocabs;
    private final String feedbackMessage;

    public ProficiencyReport(String level, int accuracy, int masteredCount, int totalVocabs, String feedbackMessage) {
        this.level = level;
        this.accuracy = accuracy;
        this.masteredCount = masteredCount;
        this.totalVocabs = totalVocabs;
        this.feedbackMessage = feedbackMessage;
    }

    public String getLevel() { return level; }
    public int getAccuracy() { return accuracy; }
    public int getMasteredCount() { return masteredCount; }
    public int getTotalVocabs() { return totalVocabs; }
    public String getFeedbackMessage() { return feedbackMessage; }
}
