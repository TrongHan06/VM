package com.example.vocabmaster.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.vocabmaster.model.entity.QuizResultEntity;
import com.example.vocabmaster.model.entity.QuizResultWithQuiz;

import java.util.List;

@Dao
public interface QuizResultDao {

    @Query("SELECT * FROM quiz_results ORDER BY completed_at DESC")
    List<QuizResultEntity> getAllResults();

    @Query("SELECT * FROM quiz_results ORDER BY completed_at DESC")
    LiveData<List<QuizResultEntity>> getAllResultsLiveData();

    @Query("SELECT r.result_id, r.quiz_id, q.quiz_name, q.total_questions, r.score, r.correct_answers, r.completed_at " +
           "FROM quiz_results r " +
           "INNER JOIN quizzes q ON r.quiz_id = q.quiz_id " +
           "ORDER BY r.completed_at DESC")
    List<QuizResultWithQuiz> getAllResultsWithQuizDetails();

    @Query("SELECT r.result_id, r.quiz_id, q.quiz_name, q.total_questions, r.score, r.correct_answers, r.completed_at " +
           "FROM quiz_results r " +
           "INNER JOIN quizzes q ON r.quiz_id = q.quiz_id " +
           "ORDER BY r.completed_at DESC")
    LiveData<List<QuizResultWithQuiz>> getAllResultsWithQuizDetailsLiveData();

    @Query("SELECT * FROM quiz_results WHERE quiz_id = :quizId ORDER BY completed_at DESC")
    List<QuizResultEntity> getResultsByQuizId(int quizId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertResult(QuizResultEntity result);

    @Query("DELETE FROM quiz_results")
    void clearAllResults();
}
