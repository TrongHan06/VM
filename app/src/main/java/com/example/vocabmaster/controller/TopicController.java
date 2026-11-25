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
import com.example.vocabmaster.model.entity.TopicProgressItem;
import com.example.vocabmaster.model.entity.VocabEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TopicController {

    public enum VocabFilterType {
        ALL, LEARNED, FAVORITE, WRONG, CUSTOM
    }

    public static List<VocabEntity> filterVocabList(
            List<VocabEntity> allVocabs,
            Set<Integer> wrongIds,
            VocabFilterType filterType,
            int selectedTopicId) {
        if (allVocabs == null || allVocabs.isEmpty()) {
            return new ArrayList<>();
        }
        List<VocabEntity> filtered = new ArrayList<>();
        for (VocabEntity v : allVocabs) {
            if (v == null) continue;

            if (selectedTopicId > 0 && v.getTopicId() != selectedTopicId) {
                continue;
            }

            boolean matchesType = false;
            if (filterType == null || filterType == VocabFilterType.ALL) {
                matchesType = true;
            } else if (filterType == VocabFilterType.LEARNED) {
                matchesType = v.isLearned();
            } else if (filterType == VocabFilterType.FAVORITE) {
                matchesType = v.isFavorite();
            } else if (filterType == VocabFilterType.WRONG) {
                matchesType = wrongIds != null && wrongIds.contains(v.getId());
            } else if (filterType == VocabFilterType.CUSTOM) {
                matchesType = v.isCustom();
            }

            if (matchesType) {
                filtered.add(v);
            }
        }
        return filtered;
    }

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

    public List<VocabEntity> filterVocabs(VocabFilterType filterType, int selectedTopicId) {
        List<VocabEntity> allVocabs = getAllVocabs();
        List<VocabEntity> wrongVocabs = getWrongVocabs();
        java.util.Set<Integer> wrongIds = new java.util.HashSet<>();
        if (wrongVocabs != null) {
            for (VocabEntity v : wrongVocabs) {
                wrongIds.add(v.getId());
            }
        }
        return filterVocabList(allVocabs, wrongIds, filterType, selectedTopicId);
    }

    public List<VocabEntity> filterVocabs(int checkedMainChipId, int selectedSubTag, List<TopicEntity> allTopics) {
        VocabFilterType filterType = VocabFilterType.ALL;
        if (checkedMainChipId == R.id.chipLearned) filterType = VocabFilterType.LEARNED;
        else if (checkedMainChipId == R.id.chipFavorite) filterType = VocabFilterType.FAVORITE;
        else if (checkedMainChipId == R.id.chipWrong) filterType = VocabFilterType.WRONG;
        else if (checkedMainChipId == R.id.chipAdded) filterType = VocabFilterType.CUSTOM;

        int selectedTopicId = -1;
        if (selectedSubTag == -2) {
            filterType = VocabFilterType.CUSTOM;
        } else if (allTopics != null && selectedSubTag >= 1 && selectedSubTag <= allTopics.size()) {
            selectedTopicId = allTopics.get(selectedSubTag - 1).getId();
        } else if (selectedSubTag > 0) {
            selectedTopicId = selectedSubTag;
        }

        return filterVocabs(filterType, selectedTopicId);
    }

    public LiveData<List<TopicProgressItem>> getAllTopicProgressLiveData() {
        return db.vocabDao().getAllTopicProgressLiveData();
    }

    public List<TopicProgressItem> getAllTopicProgressList() {
        return db.vocabDao().getAllTopicProgressList();
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
        TopicProgressItem tp = new TopicProgressItem(topicId, totalWords, flashcardCount, quizCount);
        return tp.getProgressPercent();
    }

    private VocabEntity findVocabById(int vocabId) {
        return db.vocabDao().getVocabById(vocabId);
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
            if (vocab != null) {
                vocab.setCustom(true);
            }
            long insertedId = db.vocabDao().insertVocab(vocab);
            if (insertedId > 0 && vocab != null) {
                vocab.setId((int) insertedId);
            }
            if (vocab != null) {
                db.topicDao().updateTopicLearnedCount(vocab.getTopicId());
                db.topicDao().updateTopicTotalCount(vocab.getTopicId());

                FirebaseSyncController.getInstance().pushCustomVocab(vocab);
                FirebaseSyncController.getInstance().pushVocabState(vocab, null);
            }

            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void deleteVocab(VocabEntity vocab, Runnable onComplete) {
        executor.execute(() -> {
            if (vocab != null) {
                db.vocabDao().deleteVocab(vocab);
                db.learningProgressDao().deleteProgressByVocabId(vocab.getId());
                db.topicDao().updateTopicLearnedCount(vocab.getTopicId());
                db.topicDao().updateTopicTotalCount(vocab.getTopicId());

                FirebaseSyncController.getInstance().deleteCustomVocabOnCloud(vocab);
            }

            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void deleteVocab(VocabEntity vocab) {
        deleteVocab(vocab, null);
    }

    public void toggleFavorite(int vocabId, boolean isFavorite, Runnable onComplete) {
        executor.execute(() -> {
            db.vocabDao().updateFavoriteStatus(vocabId, isFavorite);
            VocabEntity v = findVocabById(vocabId);
            LearningProgressEntity lp = db.learningProgressDao().getProgressByVocabId(vocabId);
            if (v != null) {
                FirebaseSyncController.getInstance().pushVocabState(v, lp);
            }
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void toggleFavorite(int vocabId, boolean isFavorite) {
        toggleFavorite(vocabId, isFavorite, null);
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

    public List<TopicEntity> getAllTopics() {
        return db.topicDao().getAllTopics();
    }

    public List<VocabEntity> getVocabsForTopic(String topicTitle) {
        if (topicTitle == null) topicTitle = "Gia đình";

        TopicEntity topicEntity = db.topicDao().getTopicByTitle(topicTitle);
        if (topicEntity != null) {
            return db.vocabDao().getVocabsByTopicId(topicEntity.getId());
        }
        return java.util.Collections.emptyList();
    }

    public List<VocabEntity> getAllVocabs() {
        return db.vocabDao().getAllVocabs();
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
            db.quizDao().clearAllQuizzes();
            db.vocabDao().resetAllVocabStates();
            db.topicDao().resetAllTopicLearnedCounts();

            if (dailyProgressManager != null) {
                dailyProgressManager.resetAllDailyProgress();
                FirebaseSyncController.getInstance().pushDailySettings(dailyProgressManager);
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
