package com.example.vocabmaster.controller;

import android.content.Context;

import com.example.vocabmaster.model.database.AppDatabase;
import com.example.vocabmaster.model.entity.DashboardStats;
import com.example.vocabmaster.model.entity.LearningProgressEntity;
import com.example.vocabmaster.model.entity.ProficiencyReport;
import com.example.vocabmaster.model.entity.PronunciationResult;
import com.example.vocabmaster.model.entity.VocabEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LearningProgressController {

    private final AppDatabase db;

    public LearningProgressController(Context context) {
        this.db = context != null ? AppDatabase.getInstance(context) : null;
    }

    public void recordAnswer(int vocabId, int topicId, boolean isCorrect) {
        long now = System.currentTimeMillis();
        LearningProgressEntity progress = db.learningProgressDao().getProgressByVocabId(vocabId);

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

        List<VocabEntity> allVocabs = db.vocabDao().getAllVocabs();
        for (VocabEntity v : allVocabs) {
            if (v.getId() == vocabId) {
                FirebaseSyncController.getInstance().pushVocabState(v, progress);
                break;
            }
        }
    }

    public List<VocabEntity> getDueReviewVocabs() {
        long now = System.currentTimeMillis();
        List<LearningProgressEntity> dueList = db.learningProgressDao().getDueReviews(now);
        List<VocabEntity> vocabs = new ArrayList<>();

        List<VocabEntity> allVocabs = db.vocabDao().getAllVocabs();
        for (LearningProgressEntity progress : dueList) {
            for (VocabEntity vocab : allVocabs) {
                if (vocab.getId() == progress.getVocabularyId()) {
                    vocabs.add(vocab);
                    break;
                }
            }
        }
        return vocabs;
    }

    public int getCountByStatus(String status) {
        return db.learningProgressDao().getCountByStatus(status);
    }

    public int calculateOverallAccuracy() {
        int totalCorrect = db.learningProgressDao().getTotalCorrectCount();
        int totalWrong = db.learningProgressDao().getTotalWrongCount();
        int totalAttempts = totalCorrect + totalWrong;
        if (totalAttempts == 0) return 0;
        return (totalCorrect * 100) / totalAttempts;
    }

    public DashboardStats getDashboardStats(TopicController topicController, DailyProgressManager dailyProgressManager) {
        if (topicController == null || dailyProgressManager == null) {
            return new DashboardStats(0, 0, 0, 0, 0, 0, 0, 10, 0, "");
        }

        List<VocabEntity> allVocabs = topicController.getAllVocabs();
        int learnedCount = 0;
        int customCount = 0;
        for (VocabEntity v : allVocabs) {
            if (v.isLearned()) learnedCount++;
            if (v.isCustom()) customCount++;
        }

        List<VocabEntity> wrongVocabs = topicController.getWrongVocabs();
        int wrongCount = wrongVocabs != null ? wrongVocabs.size() : 0;

        int quizCompleted = dailyProgressManager.getTotalQuizCompleted();
        int actualTotalCorrectQuiz = db.learningProgressDao().getTotalCorrectCount();
        int accuracy = calculateOverallAccuracy();

        int todayCount = dailyProgressManager.getTodayWordsCount();
        int dailyGoal = dailyProgressManager.getDailyGoal();
        int dailyPercent = Math.min(100, (todayCount * 100) / (dailyGoal > 0 ? dailyGoal : 10));

        String statusStr;
        if (todayCount >= dailyGoal) {
            statusStr = "Đã hoàn thành mục tiêu hôm nay!";
        } else {
            statusStr = "Hoàn thành thêm " + (dailyGoal - todayCount) + " từ hôm nay";
        }

        return new DashboardStats(learnedCount, quizCompleted, actualTotalCorrectQuiz, accuracy,
                wrongCount, customCount, todayCount, dailyGoal, dailyPercent, statusStr);
    }

    public ProficiencyReport getProficiencyReport(int totalVocabs) {
        int accuracy = calculateOverallAccuracy();
        int masteredCount = getCountByStatus("mastered");

        String level = "Mới bắt đầu (Beginner)";
        if (accuracy >= 85 && masteredCount >= 10) {
            level = "Nâng cao (Advanced)";
        } else if (accuracy >= 60 && masteredCount >= 5) {
            level = "Trung cấp (Intermediate)";
        }

        String feedback = "Lời khuyên: Hãy duy trì ôn tập hằng ngày và làm thêm bài kiểm tra để tăng tốc độ ghi nhớ!";

        return new ProficiencyReport(level, accuracy, masteredCount, totalVocabs, feedback);
    }

    public PronunciationResult evaluatePronunciation(String targetWord, String recognizedText) {
        String target = targetWord != null ? targetWord.trim() : "";
        String recognized = recognizedText != null ? recognizedText.trim() : "";

        if (target.isEmpty() || recognized.isEmpty()) {
            return new PronunciationResult("Chưa chính xác! Thử lại xem sao nhé.", "#DC2626", recognized, target, false);
        }

        if (recognized.equalsIgnoreCase(target)) {
            return new PronunciationResult("Chính xác 100%!", "#16A34A", recognized, target, true);
        }

        String targetLower = target.toLowerCase(Locale.ROOT);
        String recognizedLower = recognized.toLowerCase(Locale.ROOT);
        if (recognized.length() >= 2
                && (recognizedLower.contains(targetLower) || calculateSimilarity(targetLower, recognizedLower) >= 0.6)) {
            return new PronunciationResult("Gần đúng! Phát âm chuẩn hơn nữa nhé.", "#D97706", recognized, target, false);
        }

        return new PronunciationResult("Chưa chính xác! Thử lại xem sao nhé.", "#DC2626", recognized, target, false);
    }

    static double calculateSimilarity(String a, String b) {
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;
        int distance = levenshteinDistance(a, b);
        return (double) (maxLen - distance) / maxLen;
    }

    static int levenshteinDistance(String a, String b) {
        int lenA = a.length();
        int lenB = b.length();
        int[] prev = new int[lenB + 1];
        int[] curr = new int[lenB + 1];

        for (int j = 0; j <= lenB; j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= lenA; i++) {
            curr[0] = i;
            char charA = a.charAt(i - 1);
            for (int j = 1; j <= lenB; j++) {
                int cost = (charA == b.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        return prev[lenB];
    }

    public void resetProgress(int vocabId) {
        db.learningProgressDao().deleteProgressByVocabId(vocabId);
        db.vocabDao().updateLearnedStatus(vocabId, false);
    }

    /**
     * Pure SRS update logic extracted for unit-testability (no DB access).
     * Intervals (correct answers): 1 -> 3 -> 7 -> 14 -> 30 days (capped at 30d).
     * Wrong answer: demote to LEARNING, nextReview = now + 1 hour.
     */
    public static void applySrsResult(
            com.example.vocabmaster.model.entity.LearningProgressEntity progress,
            boolean isCorrect,
            long now) {
        if (isCorrect) {
            int newCorrect = progress.getCorrectCount() + 1;
            progress.setCorrectCount(newCorrect);
            progress.setStatus(newCorrect >= 2 ? "MASTERED" : "LEARNING");
            long interval = srsIntervalMs(newCorrect);
            progress.setNextReview(now + interval);
        } else {
            progress.setWrongCount(progress.getWrongCount() + 1);
            progress.setStatus("LEARNING");
            progress.setNextReview(now + 3_600_000L);
        }
    }

    /** Returns the SRS review interval in milliseconds for a given correct-answer count. */
    private static long srsIntervalMs(int correctCount) {
        // 1->1d, 2->3d, 3->7d, 4->14d, 5+->30d
        int[] DAYS = {1, 3, 7, 14, 30};
        int idx = Math.min(correctCount - 1, DAYS.length - 1);
        return DAYS[idx] * 86_400_000L;
    }
}
