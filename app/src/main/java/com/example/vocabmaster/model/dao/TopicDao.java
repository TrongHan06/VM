package com.example.vocabmaster.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.vocabmaster.model.entity.TopicEntity;

import java.util.List;

@Dao
public interface TopicDao {

    @Query("SELECT * FROM topics")
    List<TopicEntity> getAllTopics();

    @Query("SELECT * FROM topics")
    LiveData<List<TopicEntity>> getAllTopicsLiveData();

    @Query("SELECT * FROM topics WHERE topic_name = :title")
    TopicEntity getTopicByTitle(String title);

    @Query("UPDATE topics SET learnedCount = (SELECT COUNT(*) FROM vocabularies WHERE topic_id = :topicId AND isLearned = 1) WHERE topic_id = :topicId")
    void updateTopicLearnedCount(int topicId);

    @Query("UPDATE topics SET totalCount = (SELECT COUNT(*) FROM vocabularies WHERE topic_id = :topicId) WHERE topic_id = :topicId")
    void updateTopicTotalCount(int topicId);

    @Update
    void updateTopic(TopicEntity topic);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTopic(TopicEntity topic);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllTopics(List<TopicEntity> topics);

    @Query("UPDATE topics SET learnedCount = 0")
    void resetAllTopicLearnedCounts();
}
