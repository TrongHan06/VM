package com.example.vocabmaster.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.vocabmaster.model.entity.QuizEntity;

import java.util.List;

@Dao
public interface QuizDao {

    @Query("SELECT * FROM quizzes")
    List<QuizEntity> getAllQuizzes();

    @Query("SELECT * FROM quizzes WHERE topic_id = :topicId")
    List<QuizEntity> getQuizzesByTopicId(int topicId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertQuiz(QuizEntity quiz);

    @Query("DELETE FROM quizzes")
    void clearAllQuizzes();
}
