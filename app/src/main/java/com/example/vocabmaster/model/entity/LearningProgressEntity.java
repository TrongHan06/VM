package com.example.vocabmaster.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "learning_progress",
        indices = {@Index(value = {"vocabulary_id"}, unique = true)},
        foreignKeys = @ForeignKey(entity = VocabEntity.class,
                                  parentColumns = "vocabulary_id",
                                  childColumns = "vocabulary_id",
                                  onDelete = ForeignKey.CASCADE))
public class LearningProgressEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "progress_id")
    private int progressId;

    @ColumnInfo(name = "vocabulary_id")
    private int vocabularyId;

    private String status;

    @ColumnInfo(name = "correct_count")
    private int correctCount;

    @ColumnInfo(name = "wrong_count")
    private int wrongCount;

    @ColumnInfo(name = "next_review")
    private long nextReview;

    public LearningProgressEntity(int vocabularyId, String status, int correctCount, int wrongCount, long nextReview) {
        this.vocabularyId = vocabularyId;
        this.status = status;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.nextReview = nextReview;
    }

    @Ignore
    public LearningProgressEntity(int vocabularyId, int correctCount, int wrongCount, long nextReview, String status) {
        this.vocabularyId = vocabularyId;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.nextReview = nextReview;
        this.status = status;
    }

    public int getProgressId() { return progressId; }
    public void setProgressId(int progressId) { this.progressId = progressId; }

    public int getVocabularyId() { return vocabularyId; }
    public void setVocabularyId(int vocabularyId) { this.vocabularyId = vocabularyId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public int getWrongCount() { return wrongCount; }
    public void setWrongCount(int wrongCount) { this.wrongCount = wrongCount; }

    public long getNextReview() { return nextReview; }
    public void setNextReview(long nextReview) { this.nextReview = nextReview; }
}
