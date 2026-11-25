package com.example.vocabmaster.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "quizzes",
        foreignKeys = @ForeignKey(entity = TopicEntity.class,
                                  parentColumns = "topic_id",
                                  childColumns = "topic_id",
                                  onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"topic_id"})})
public class QuizEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "quiz_id")
    private int quizId;

    @ColumnInfo(name = "quiz_name")
    private String quizName;

    @ColumnInfo(name = "topic_id")
    private Integer topicId;

    @ColumnInfo(name = "total_questions")
    private int totalQuestions;

    public QuizEntity(String quizName, Integer topicId, int totalQuestions) {
        this.quizName = quizName;
        this.topicId = topicId;
        this.totalQuestions = totalQuestions;
    }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getQuizName() { return quizName; }
    public void setQuizName(String quizName) { this.quizName = quizName; }

    public Integer getTopicId() { return topicId; }
    public void setTopicId(Integer topicId) { this.topicId = topicId; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
}
