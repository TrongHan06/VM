package com.example.vocabmaster.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_results",
        foreignKeys = @ForeignKey(entity = QuizEntity.class,
                                  parentColumns = "quiz_id",
                                  childColumns = "quiz_id",
                                  onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"quiz_id"})})
public class QuizResultEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "result_id")
    private int resultId;

    @ColumnInfo(name = "quiz_id")
    private int quizId;

    private int score;

    @ColumnInfo(name = "correct_answers")
    private int correctAnswers;

    @ColumnInfo(name = "completed_at")
    private long completedAt;

    public QuizResultEntity(int quizId, int score, int correctAnswers, long completedAt) {
        this.quizId = quizId;
        this.score = score;
        this.correctAnswers = correctAnswers;
        this.completedAt = completedAt;
    }

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
}
