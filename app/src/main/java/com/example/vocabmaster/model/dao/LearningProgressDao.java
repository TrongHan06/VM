package com.example.vocabmaster.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.vocabmaster.model.entity.LearningProgressEntity;

import java.util.List;

@Dao
public interface LearningProgressDao {

    @Query("SELECT * FROM learning_progress")
    List<LearningProgressEntity> getAllProgress();

    @Query("SELECT * FROM learning_progress")
    LiveData<List<LearningProgressEntity>> getAllProgressLiveData();

    @Query("SELECT * FROM learning_progress WHERE vocabulary_id = :vocabId")
    LearningProgressEntity getProgressByVocabId(int vocabId);

    @Query("SELECT * FROM learning_progress WHERE vocabulary_id = :vocabId")
    LiveData<LearningProgressEntity> getProgressByVocabIdLiveData(int vocabId);

    @Query("SELECT * FROM learning_progress WHERE next_review <= :currentTime")
    List<LearningProgressEntity> getDueReviews(long currentTime);

    @Query("SELECT COUNT(*) FROM learning_progress WHERE UPPER(status) = UPPER(:status)")
    int getCountByStatus(String status);

    @Query("SELECT COUNT(*) FROM learning_progress WHERE wrong_count > 0 AND UPPER(status) != 'MASTERED'")
    int getWrongWordsCount();

    @Query("SELECT COUNT(*) FROM learning_progress WHERE wrong_count > 0 AND UPPER(status) != 'MASTERED'")
    LiveData<Integer> getWrongWordsCountLiveData();

    @Query("SELECT COALESCE(SUM(correct_count), 0) FROM learning_progress")
    int getTotalCorrectCount();

    @Query("SELECT COALESCE(SUM(wrong_count), 0) FROM learning_progress")
    int getTotalWrongCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProgress(LearningProgressEntity progress);

    @Query("DELETE FROM learning_progress WHERE vocabulary_id = :vocabId")
    void deleteProgressByVocabId(int vocabId);

    @Query("DELETE FROM learning_progress")
    void clearAllProgress();
}
