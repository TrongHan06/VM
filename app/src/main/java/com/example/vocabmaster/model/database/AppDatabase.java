package com.example.vocabmaster.model.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.vocabmaster.model.dao.LearningProgressDao;
import com.example.vocabmaster.model.dao.QuizDao;
import com.example.vocabmaster.model.dao.QuizResultDao;
import com.example.vocabmaster.model.dao.TopicDao;
import com.example.vocabmaster.model.dao.VocabDao;
import com.example.vocabmaster.model.entity.LearningProgressEntity;
import com.example.vocabmaster.model.entity.QuizEntity;
import com.example.vocabmaster.model.entity.QuizResultEntity;
import com.example.vocabmaster.model.entity.TopicEntity;
import com.example.vocabmaster.model.entity.VocabEntity;

@Database(entities = {
        TopicEntity.class,
        VocabEntity.class,
        LearningProgressEntity.class,
        QuizEntity.class,
        QuizResultEntity.class
}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TopicDao topicDao();
    public abstract VocabDao vocabDao();
    public abstract LearningProgressDao learningProgressDao();
    public abstract QuizDao quizDao();
    public abstract QuizResultDao quizResultDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "vocab_master_db")
                            .createFromAsset("databases/vocabulary_app.db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
