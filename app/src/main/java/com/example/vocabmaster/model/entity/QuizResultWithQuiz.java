package com.example.vocabmaster.model.entity;

import androidx.room.ColumnInfo;

public class QuizResultWithQuiz {

    @ColumnInfo(name = "result_id")
    private int resultId;

    @ColumnInfo(name = "quiz_id")
    private int quizId;

    @ColumnInfo(name = "quiz_name")
    private String quizName;

    @ColumnInfo(name = "total_questions")
    private int totalQuestions;

    private int score;

    @ColumnInfo(name = "correct_answers")
    private int correctAnswers;

    @ColumnInfo(name = "completed_at")
    private long completedAt;

    public QuizResultWithQuiz(int resultId, int quizId, String quizName, int totalQuestions, int score, int correctAnswers, long completedAt) {
        this.resultId = resultId;
        this.quizId = quizId;
        this.quizName = quizName;
        this.totalQuestions = totalQuestions;
        this.score = score;
        this.correctAnswers = correctAnswers;
        this.completedAt = completedAt;
    }

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getQuizName() { return quizName; }
    public void setQuizName(String quizName) { this.quizName = quizName; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
}
