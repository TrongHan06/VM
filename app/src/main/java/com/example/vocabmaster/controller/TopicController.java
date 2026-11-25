package com.example.vocabmaster.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.vocabmaster.R;
import com.example.vocabmaster.model.database.AppDatabase;
import com.example.vocabmaster.model.entity.LearningProgressEntity;
import com.example.vocabmaster.model.entity.QuizEntity;
import com.example.vocabmaster.model.entity.QuizResultEntity;
import com.example.vocabmaster.model.entity.QuizResultWithQuiz;
import com.example.vocabmaster.model.entity.TopicEntity;
import com.example.vocabmaster.model.entity.TopicProgress;
import com.example.vocabmaster.model.entity.VocabEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TopicController {

    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public TopicController(Context context) {
        this.db = AppDatabase.getInstance(context);
        setupDatabaseIfNeeded();
    }

    public LiveData<List<TopicEntity>> getAllTopicsLiveData() {
        return db.topicDao().getAllTopicsLiveData();
    }

    public LiveData<List<VocabEntity>> getAllVocabsLiveData() {
        return db.vocabDao().getAllVocabsLiveData();
    }

    public LiveData<TopicProgress> getTopicProgressLiveData(int topicId) {
        return db.vocabDao().getTopicProgressLiveData(topicId);
    }

    public LiveData<List<QuizResultWithQuiz>> getAllQuizResultsWithDetailsLiveData() {
        return db.quizResultDao().getAllResultsWithQuizDetailsLiveData();
    }

    public List<QuizResultWithQuiz> getAllQuizResultsWithDetails() {
        return db.quizResultDao().getAllResultsWithQuizDetails();
    }

    public List<QuizResultEntity> getAllQuizResults() {
        return db.quizResultDao().getAllResults();
    }

    public void clearQuizHistory() {
        executor.execute(() -> db.quizResultDao().clearAllResults());
    }

    private void setupDatabaseIfNeeded() {
        if (db.topicDao().getAllTopics().isEmpty()) {
            long famId = db.topicDao().insertTopic(new TopicEntity("Gia đình", "Family", 0, 2, R.drawable.ic_family));
            db.vocabDao().insertVocab(new VocabEntity((int) famId, "Family", "/ˈfæməli/", "Gia đình", "I love my family.", "Tôi yêu gia đình tôi.", false, false));
            db.vocabDao().insertVocab(new VocabEntity((int) famId, "Father", "/ˈfɑːðər/", "Bố / Cha", "My father is a doctor.", "Bố tôi là bác sĩ.", false, false));

            long schoolId = db.topicDao().insertTopic(new TopicEntity("Trường học", "School", 0, 2, R.drawable.ic_school));
            db.vocabDao().insertVocab(new VocabEntity((int) schoolId, "Teacher", "/ˈtiːtʃər/", "Giáo viên", "My teacher is very helpful.", "Giáo viên của tôi rất tận tụy.", false, false));
            db.vocabDao().insertVocab(new VocabEntity((int) schoolId, "Student", "/ˈstuːdnt/", "Học sinh", "All students are studying.", "Tất cả học sinh đang học bài.", false, false));

            long workId = db.topicDao().insertTopic(new TopicEntity("Công việc", "Work", 0, 2, R.drawable.ic_congviec));
            db.vocabDao().insertVocab(new VocabEntity((int) workId, "Job", "/dʒɑːb/", "Công việc", "She got a new job today.", "Hôm nay cô ấy có công việc mới.", false, false));
            db.vocabDao().insertVocab(new VocabEntity((int) workId, "Office", "/ˈɔːfɪs/", "Văn phòng", "He works in an office.", "Anh ấy làm việc trong văn phòng.", false, false));

            long foodId = db.topicDao().insertTopic(new TopicEntity("Đồ ăn", "Food", 0, 2, R.drawable.ic_doan));
            db.vocabDao().insertVocab(new VocabEntity((int) foodId, "Delicious", "/dɪˈlɪʃəs/", "Thơm ngon", "This pizza is delicious.", "Món bánh pizza này rất ngon.", false, false));
            db.vocabDao().insertVocab(new VocabEntity((int) foodId, "Water", "/ˈwɔːtər/", "Nước", "Drink plenty of water.", "Hãy uống nhiều nước.", false, false));

            long travelId = db.topicDao().insertTopic(new TopicEntity("Du lịch", "Travel", 0, 2, R.drawable.ic_dulich));
            db.vocabDao().insertVocab(new VocabEntity((int) travelId, "Journey", "/ˈdʒɜːrni/", "Chuyến đi", "Have a safe journey!", "Chúc bạn có một chuyến đi an toàn!", false, false));
            db.vocabDao().insertVocab(new VocabEntity((int) travelId, "Airport", "/ˈerpɔːrt/", "Sân bay", "We are heading to the airport.", "Chúng tôi đang đi ra sân bay.", false, false));

            long techId = db.topicDao().insertTopic(new TopicEntity("Công nghệ", "Technology", 0, 2, R.drawable.ic_tech));
            db.vocabDao().insertVocab(new VocabEntity((int) techId, "Computer", "/kəmˈpjuːtər/", "Máy tính", "I work on my computer.", "Tôi làm việc trên máy tính.", false, false));
            db.vocabDao().insertVocab(new VocabEntity((int) techId, "Phone", "/foʊn/", "Điện thoại", "My phone is ringing.", "Điện thoại của tôi đang reo.", false, false));
        }
    }

    public List<VocabEntity> filterVocabs(int checkedMainChipId, int selectedSubTag, List<TopicEntity> allTopics) {
        List<VocabEntity> allVocabs = getAllVocabs();
        List<VocabEntity> baseList = new ArrayList<>();

        if (checkedMainChipId == R.id.chipTopic) {
            baseList.addAll(allVocabs);
        } else if (checkedMainChipId == R.id.chipLearned) {
            for (VocabEntity v : allVocabs) {
                if (v.isLearned()) baseList.add(v);
            }
        } else if (checkedMainChipId == R.id.chipFavorite) {
            for (VocabEntity v : allVocabs) {
                if (v.isFavorite()) baseList.add(v);
            }
        } else if (checkedMainChipId == R.id.chipWrong) {
            List<VocabEntity> wrongList = getWrongVocabs();
            if (wrongList != null) {
                baseList.addAll(wrongList);
            }
        } else if (checkedMainChipId == R.id.chipAdded) {
            for (VocabEntity v : allVocabs) {
                if (v.getId() > 120) baseList.add(v);
            }
        } else {
            baseList.addAll(allVocabs);
        }

        List<VocabEntity> filteredList = new ArrayList<>();
        if (selectedSubTag == -1) {
            filteredList.addAll(baseList);
        } else if (selectedSubTag == -2) {
            for (VocabEntity v : baseList) {
                if (v.getId() > 120 || (allTopics != null && v.getTopicId() > allTopics.size())) {
                    filteredList.add(v);
                }
            }
        } else {
            TopicEntity selectedTopic = null;
            if (allTopics != null && selectedSubTag >= 1 && selectedSubTag <= allTopics.size()) {
                selectedTopic = allTopics.get(selectedSubTag - 1);
            }
            if (selectedTopic != null) {
                List<VocabEntity> topicVocabs = getVocabsForTopic(selectedTopic.getTitle());
                for (VocabEntity v : baseList) {
                    for (VocabEntity tv : topicVocabs) {
                        if (tv.getId() == v.getId()) {
                            filteredList.add(v);
                            break;
                        }
                    }
                }
            } else {
                filteredList.addAll(baseList);
            }
        }
        return filteredList;
    }

    public int calculateTopicProgressPercent(int topicId) {
        List<VocabEntity> vocabs = db.vocabDao().getVocabsByTopicId(topicId);
        if (vocabs == null || vocabs.isEmpty()) return 0;

        int totalWords = vocabs.size();
        int flashcardCount = 0;
        int quizCount = 0;

        for (VocabEntity vocab : vocabs) {
            if (vocab.isLearned()) {
                flashcardCount++;
            }
            LearningProgressEntity progress = db.learningProgressDao().getProgressByVocabId(vocab.getId());
            if (progress != null && ("MASTERED".equalsIgnoreCase(progress.getStatus()) || "mastered".equalsIgnoreCase(progress.getStatus()))) {
                quizCount++;
            }
        }
        TopicProgress tp = new TopicProgress(totalWords, flashcardCount, quizCount);
        return tp.getProgressPercent();
    }

    private VocabEntity findVocabById(int vocabId) {
        List<VocabEntity> all = db.vocabDao().getAllVocabs();
        for (VocabEntity v : all) {
            if (v.getId() == vocabId) return v;
        }
        return null;
    }

    public void markAsLearnedIfUnlearned(int vocabId, int topicId, DailyProgressManager dailyProgressManager) {
        executor.execute(() -> {
            int updatedRows = db.vocabDao().markAsLearnedIfUnlearned(vocabId);
            if (updatedRows > 0) {
                db.topicDao().updateTopicLearnedCount(topicId);
                if (dailyProgressManager != null) {
                    dailyProgressManager.incrementTodayWordsCount();
                }

                VocabEntity v = findVocabById(vocabId);
                LearningProgressEntity lp = db.learningProgressDao().getProgressByVocabId(vocabId);
                if (v != null) {
                    FirebaseSyncController.getInstance().pushVocabState(v, lp);
                }
                if (dailyProgressManager != null) {
                    FirebaseSyncController.getInstance().pushDailySettings(dailyProgressManager);
                }
            }
        });
    }

    public void markVocabAsLearned(int vocabId, int topicId) {
        markAsLearnedIfUnlearned(vocabId, topicId, null);
    }

    public void markVocabAsLearned(int vocabId, int topicId, DailyProgressManager dailyProgressManager) {
        markAsLearnedIfUnlearned(vocabId, topicId, dailyProgressManager);
    }

    public void addCustomVocab(VocabEntity vocab, Runnable onComplete) {
        executor.execute(() -> {
            long insertedId = db.vocabDao().insertVocab(vocab);
            if (insertedId > 0) {
                vocab.setId((int) insertedId);
            }
            db.topicDao().updateTopicLearnedCount(vocab.getTopicId());
            db.topicDao().updateTopicTotalCount(vocab.getTopicId());

            FirebaseSyncController.getInstance().pushCustomVocab(vocab);
            FirebaseSyncController.getInstance().pushVocabState(vocab, null);

            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void deleteVocab(VocabEntity vocab, Runnable onComplete) {
        executor.execute(() -> {
            db.vocabDao().deleteVocab(vocab);
            db.learningProgressDao().deleteProgressByVocabId(vocab.getId());
            db.topicDao().updateTopicLearnedCount(vocab.getTopicId());
            db.topicDao().updateTopicTotalCount(vocab.getTopicId());

            FirebaseSyncController.getInstance().deleteCustomVocabOnCloud(vocab.getId());

            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void deleteVocab(VocabEntity vocab) {
        deleteVocab(vocab, null);
    }

    public void toggleFavorite(int vocabId, boolean isFavorite) {
        executor.execute(() -> {
            db.vocabDao().updateFavoriteStatus(vocabId, isFavorite);
            VocabEntity v = findVocabById(vocabId);
            LearningProgressEntity lp = db.learningProgressDao().getProgressByVocabId(vocabId);
            if (v != null) {
                FirebaseSyncController.getInstance().pushVocabState(v, lp);
            }
        });
    }

    public void saveQuizResult(String quizName, Integer topicId, int totalQuestions, int score, int correctAnswers) {
        executor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                Integer validTopicId = (topicId != null && topicId > 0) ? topicId : null;
                QuizEntity quiz = new QuizEntity(quizName, validTopicId, totalQuestions);
                long quizId = db.quizDao().insertQuiz(quiz);

                QuizResultEntity result = new QuizResultEntity((int) quizId, score, correctAnswers, now);
                db.quizResultDao().insertResult(result);

                FirebaseSyncController.getInstance().pushQuizResult(quizName, validTopicId, totalQuestions, score, correctAnswers, now);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void updateLearningProgress(int vocabId, int topicId, boolean isCorrect) {
        executor.execute(() -> {
            LearningProgressEntity progress = db.learningProgressDao().getProgressByVocabId(vocabId);
            long now = System.currentTimeMillis();

            if (progress == null) {
                int correct = isCorrect ? 1 : 0;
                int wrong = isCorrect ? 0 : 1;
                String status = isCorrect ? "MASTERED" : "LEARNING";
                long nextReview = now + (isCorrect ? 86400000L : 3600000L);
                progress = new LearningProgressEntity(vocabId, status, correct, wrong, nextReview);
            } else {
                if (isCorrect) {
                    progress.setCorrectCount(progress.getCorrectCount() + 1);
                    progress.setStatus("MASTERED");
                    progress.setNextReview(now + (86400000L * progress.getCorrectCount()));
                } else {
                    progress.setWrongCount(progress.getWrongCount() + 1);
                    progress.setStatus("LEARNING");
                    progress.setNextReview(now + 3600000L);
                }
            }
            db.learningProgressDao().insertProgress(progress);

            VocabEntity v = findVocabById(vocabId);
            if (v != null) {
                FirebaseSyncController.getInstance().pushVocabState(v, progress);
            }
        });
    }

    public List<TopicEntity> getAllTopics() {
        return db.topicDao().getAllTopics();
    }

    public List<VocabEntity> getVocabsForTopic(String topicTitle) {
        if (topicTitle == null) topicTitle = "Gia đình";

        TopicEntity topicEntity = db.topicDao().getTopicByTitle(topicTitle);
        if (topicEntity != null) {
            return db.vocabDao().getVocabsByTopicId(topicEntity.getId());
        }
        List<VocabEntity> list = new ArrayList<>();
        list.add(new VocabEntity(1, "Family", "/ˈfæməli/", "Gia đình", "I love my family.", "Tôi yêu gia đình tôi.", false, false));
        return list;
    }

    public List<VocabEntity> getAllVocabs() {
        List<VocabEntity> vocabEntities = db.vocabDao().getAllVocabs();
        if (vocabEntities.isEmpty()) {
            vocabEntities.add(new VocabEntity(1, "Family", "/ˈfæməli/", "Gia đình", "I love my family.", "Tôi yêu gia đình tôi.", false, false));
        }
        return vocabEntities;
    }

    public List<VocabEntity> getWrongVocabs() {
        return db.vocabDao().getWrongVocabs();
    }

    public List<VocabEntity> getFavoriteVocabs() {
        return db.vocabDao().getFavoriteVocabs();
    }

    public List<VocabEntity> searchVocabs(String query) {
        return db.vocabDao().searchVocabs(query);
    }

    public void clearAllData(DailyProgressManager dailyProgressManager, Runnable onComplete) {
        executor.execute(() -> {
            db.learningProgressDao().clearAllProgress();
            db.quizResultDao().clearAllResults();
            List<VocabEntity> allVocabs = db.vocabDao().getAllVocabs();
            for (VocabEntity v : allVocabs) {
                db.vocabDao().updateLearnedStatus(v.getId(), false);
                db.vocabDao().updateFavoriteStatus(v.getId(), false);
            }
            List<TopicEntity> topics = db.topicDao().getAllTopics();
            for (TopicEntity t : topics) {
                t.setLearnedCount(0);
                db.topicDao().updateTopic(t);
            }
            if (dailyProgressManager != null) {
                dailyProgressManager.resetAllDailyProgress();
            }
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void clearAllData() {
        clearAllData(null, null);
    }
}
