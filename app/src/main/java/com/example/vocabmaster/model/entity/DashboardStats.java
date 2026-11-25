package com.example.vocabmaster.model.entity;

public class DashboardStats {
    private final int learnedCount;
    private final int quizCompleted;
    private final int totalCorrectQuiz;
    private final int accuracy;
    private final int wrongCount;
    private final int customCount;
    private final int todayCount;
    private final int dailyGoal;
    private final int dailyPercent;
    private final String dailyGoalStatus;

    public DashboardStats(int learnedCount, int quizCompleted, int totalCorrectQuiz, int accuracy,
                          int wrongCount, int customCount, int todayCount, int dailyGoal,
                          int dailyPercent, String dailyGoalStatus) {
        this.learnedCount = learnedCount;
        this.quizCompleted = quizCompleted;
        this.totalCorrectQuiz = totalCorrectQuiz;
        this.accuracy = accuracy;
        this.wrongCount = wrongCount;
        this.customCount = customCount;
        this.todayCount = todayCount;
        this.dailyGoal = dailyGoal;
        this.dailyPercent = dailyPercent;
        this.dailyGoalStatus = dailyGoalStatus;
    }

    public int getLearnedCount() { return learnedCount; }
    public int getQuizCompleted() { return quizCompleted; }
    public int getTotalCorrectQuiz() { return totalCorrectQuiz; }
    public int getAccuracy() { return accuracy; }
    public int getWrongCount() { return wrongCount; }
    public int getCustomCount() { return customCount; }
    public int getTodayCount() { return todayCount; }
    public int getDailyGoal() { return dailyGoal; }
    public int getDailyPercent() { return dailyPercent; }
    public String getDailyGoalStatus() { return dailyGoalStatus; }
}
