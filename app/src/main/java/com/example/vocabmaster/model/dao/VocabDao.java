package com.example.vocabmaster.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.vocabmaster.model.entity.TopicProgress;
import com.example.vocabmaster.model.entity.TopicProgressItem;
import com.example.vocabmaster.model.entity.VocabEntity;

import java.util.List;

@Dao
public interface VocabDao {

    @Query("SELECT * FROM vocabularies")
    List<VocabEntity> getAllVocabs();

    @Query("SELECT * FROM vocabularies")
    LiveData<List<VocabEntity>> getAllVocabsLiveData();

    @Query("SELECT * FROM vocabularies WHERE topic_id = :topicId")
    List<VocabEntity> getVocabsByTopicId(int topicId);

    @Query("SELECT * FROM vocabularies WHERE topic_id = :topicId")
    LiveData<List<VocabEntity>> getVocabsByTopicIdLiveData(int topicId);

    @Query("SELECT * FROM vocabularies WHERE topic_id = :topicId AND LOWER(word) = LOWER(:word) LIMIT 1")
    VocabEntity findByTopicAndWord(int topicId, String word);

    @Query("SELECT COUNT(*) FROM vocabularies WHERE isLearned = 1")
    int getLearnedWordsCount();

    @Query("UPDATE vocabularies SET isLearned = :isLearned WHERE vocabulary_id = :vocabId")
    void updateLearnedStatus(int vocabId, boolean isLearned);

    @Query("UPDATE vocabularies SET isLearned = 1 WHERE vocabulary_id = :vocabId AND isLearned = 0")
    int markAsLearnedIfUnlearned(int vocabId);

    @Query("UPDATE vocabularies SET is_favorite = :isFavorite WHERE vocabulary_id = :vocabId")
    void updateFavoriteStatus(int vocabId, boolean isFavorite);

    @Update
    void updateVocab(VocabEntity vocab);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertVocab(VocabEntity vocab);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllVocabs(List<VocabEntity> vocabs);

    @Delete
    void deleteVocab(VocabEntity vocab);

    @Query("SELECT * FROM vocabularies WHERE is_favorite = 1")
    List<VocabEntity> getFavoriteVocabs();

    @Query("SELECT * FROM vocabularies WHERE is_favorite = 1")
    LiveData<List<VocabEntity>> getFavoriteVocabsLiveData();

    @Query("SELECT v.* FROM vocabularies v INNER JOIN learning_progress lp ON v.vocabulary_id = lp.vocabulary_id WHERE lp.wrong_count > 0 AND UPPER(lp.status) != 'MASTERED'")
    List<VocabEntity> getWrongVocabs();

    @Query("SELECT v.* FROM vocabularies v INNER JOIN learning_progress lp ON v.vocabulary_id = lp.vocabulary_id WHERE lp.wrong_count > 0 AND UPPER(lp.status) != 'MASTERED'")
    LiveData<List<VocabEntity>> getWrongVocabsLiveData();

    @Query("SELECT * FROM vocabularies WHERE word LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%'")
    List<VocabEntity> searchVocabs(String query);

    @Query("SELECT COUNT(DISTINCT v.vocabulary_id) AS totalWords, " +
           "COUNT(DISTINCT CASE WHEN v.isLearned = 1 THEN v.vocabulary_id END) AS flashcardCount, " +
           "COUNT(DISTINCT CASE WHEN UPPER(lp.status) = 'MASTERED' THEN v.vocabulary_id END) AS quizCount " +
           "FROM vocabularies v " +
           "LEFT JOIN learning_progress lp ON v.vocabulary_id = lp.vocabulary_id " +
           "WHERE v.topic_id = :topicId")
    LiveData<TopicProgress> getTopicProgressLiveData(int topicId);

    @Query("SELECT v.topic_id AS topicId, " +
           "COUNT(DISTINCT v.vocabulary_id) AS totalWords, " +
           "COUNT(DISTINCT CASE WHEN v.isLearned = 1 THEN v.vocabulary_id END) AS flashcardCount, " +
           "COUNT(DISTINCT CASE WHEN UPPER(lp.status) = 'MASTERED' THEN v.vocabulary_id END) AS quizCount " +
           "FROM vocabularies v " +
           "LEFT JOIN learning_progress lp ON v.vocabulary_id = lp.vocabulary_id " +
           "GROUP BY v.topic_id")
    LiveData<List<TopicProgressItem>> getAllTopicProgressLiveData();

    @Query("SELECT v.topic_id AS topicId, " +
           "COUNT(DISTINCT v.vocabulary_id) AS totalWords, " +
           "COUNT(DISTINCT CASE WHEN v.isLearned = 1 THEN v.vocabulary_id END) AS flashcardCount, " +
           "COUNT(DISTINCT CASE WHEN UPPER(lp.status) = 'MASTERED' THEN v.vocabulary_id END) AS quizCount " +
           "FROM vocabularies v " +
           "LEFT JOIN learning_progress lp ON v.vocabulary_id = lp.vocabulary_id " +
           "GROUP BY v.topic_id")
    List<TopicProgressItem> getAllTopicProgressList();

    @Query("SELECT * FROM vocabularies WHERE vocabulary_id = :vocabId LIMIT 1")
    VocabEntity getVocabById(int vocabId);

    @Query("SELECT v.* FROM vocabularies v INNER JOIN learning_progress lp ON v.vocabulary_id = lp.vocabulary_id WHERE lp.next_review <= :currentTime")
    List<VocabEntity> getDueReviewVocabs(long currentTime);

    @Query("UPDATE vocabularies SET isLearned = 0, is_favorite = 0")
    void resetAllVocabStates();
}
