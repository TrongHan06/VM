package com.example.vocabmaster.controller;

import com.example.vocabmaster.model.entity.TopicEntity;
import com.example.vocabmaster.model.entity.VocabEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Controller điều phối luồng làm bài tập trắc nghiệm / kiểm tra từ vựng.
 * Quản lý danh sách câu hỏi, sinh ngẫu nhiên 4 đáp án A/B/C/D, kiểm tra kết quả và ghi nhận điểm số.
 */
public class PracticeController {

    private final TopicEntity topic;
    private final boolean isExam;
    private final TopicController topicController;
    private final LearningProgressController learningProgressController;
    private final DailyProgressManager dailyProgressManager;
    private final List<VocabEntity> customVocabs;
    private final List<VocabEntity> sessionVocabs;
    private final List<VocabEntity> allVocabs;
    private int questionIndex = 0;
    private int correctAnswersCount = 0;
    private boolean sessionFinished = false;

    /**
     * Khởi tạo PracticeController theo chủ đề hoặc bài kiểm tra tổng hợp.
     */
    public PracticeController(TopicEntity topic, boolean isExam, TopicController topicController, LearningProgressController learningProgressController, DailyProgressManager dailyProgressManager) {
        this(topic, isExam, topicController, learningProgressController, dailyProgressManager, null);
    }

    /**
     * Khởi tạo PracticeController với danh sách từ vựng tùy chỉnh (ví dụ: từ vựng SRS, từ vựng hay sai).
     */
    public PracticeController(TopicEntity topic, boolean isExam, TopicController topicController, LearningProgressController learningProgressController, DailyProgressManager dailyProgressManager, List<VocabEntity> customVocabs) {
        this.topic = topic;
        this.isExam = isExam;
        this.topicController = topicController;
        this.learningProgressController = learningProgressController;
        this.dailyProgressManager = dailyProgressManager;
        this.customVocabs = customVocabs;
        this.allVocabs = topicController != null
                ? topicController.getAllVocabs()
                : (customVocabs != null ? new ArrayList<>(customVocabs) : new ArrayList<>());

        List<VocabEntity> rawVocabs;
        if (customVocabs != null && !customVocabs.isEmpty()) {
            rawVocabs = customVocabs;
        } else if (topic != null && topicController != null) {
            rawVocabs = topicController.getVocabsForTopic(topic.getTitle());
        } else {
            rawVocabs = allVocabs;
        }

        List<VocabEntity> filtered = new ArrayList<>();
        if (isExam) {
            for (VocabEntity v : rawVocabs) {
                if (v.isLearned()) {
                    filtered.add(v);
                }
            }
            if (filtered.isEmpty()) {
                filtered.addAll(rawVocabs);
            }
        } else {
            filtered.addAll(rawVocabs);
        }

        Collections.shuffle(filtered);
        int maxQuestions = isExam ? 20 : 10;
        if (filtered.size() > maxQuestions) {
            filtered = filtered.subList(0, maxQuestions);
        }
        this.sessionVocabs = filtered;
    }

    /**
     * Tạo một PracticeController mới cho việc làm lại (Retry) với giữ nguyên danh sách từ vựng ban đầu.
     */
    public PracticeController createRetryController() {
        return new PracticeController(topic, isExam, topicController, learningProgressController, dailyProgressManager, customVocabs != null ? customVocabs : sessionVocabs);
    }

    /**
     * Kiểm tra phiên kiểm tra có câu hỏi hay không.
     */
    public boolean hasQuestions() {
        return sessionVocabs != null && !sessionVocabs.isEmpty();
    }

    /**
     * Kiểm tra phiên kiểm tra đã hoàn thành tất cả câu hỏi chưa.
     */
    public boolean isSessionCompleted() {
        return sessionVocabs == null || sessionVocabs.isEmpty() || questionIndex >= sessionVocabs.size();
    }

    /**
     * Lấy thực thể từ vựng của câu hỏi hiện tại.
     */
    public VocabEntity getCurrentQuestion() {
        if (!isSessionCompleted()) {
            return sessionVocabs.get(questionIndex);
        }
        return null;
    }

    /**
     * Tạo ngẫu nhiên 4 đáp án trắc nghiệm A/B/C/D (1 đáp án đúng + 3 đáp án sai xáo trộn).
     */
    public List<String> generateOptionsForCurrentQuestion() {
        VocabEntity current = getCurrentQuestion();
        if (current == null) return new ArrayList<>();

        List<String> options = new ArrayList<>();
        String correctAnswer = current.getMeaning();
        if (correctAnswer != null && !correctAnswer.trim().isEmpty()) {
            options.add(correctAnswer);
        }

        List<String> wrongAnswers = new ArrayList<>();
        if (allVocabs != null) {
            for (VocabEntity v : allVocabs) {
                if (v == null) continue;
                String meaning = v.getMeaning();
                if (meaning != null && !meaning.trim().isEmpty()
                        && !Objects.equals(meaning, correctAnswer)
                        && !wrongAnswers.contains(meaning)) {
                    wrongAnswers.add(meaning);
                }
            }
        }
        Collections.shuffle(wrongAnswers);
        for (int i = 0; i < Math.min(3, wrongAnswers.size()); i++) {
            options.add(wrongAnswers.get(i));
        }
        Collections.shuffle(options);
        return options;
    }

    /**
     * Kiểm tra đáp án người dùng chọn đúng hay sai và lưu vết kết quả vào DB.
     */
    public boolean checkAnswer(String selectedAnswer) {
        VocabEntity current = getCurrentQuestion();
        if (current == null) return false;

        String correctAnswer = current.getMeaning();
        boolean isCorrect = correctAnswer != null
                && !correctAnswer.trim().isEmpty()
                && Objects.equals(correctAnswer, selectedAnswer);
        if (isCorrect) {
            correctAnswersCount++;
        }
        if (learningProgressController != null) {
            learningProgressController.recordAnswer(current.getId(), current.getTopicId(), isCorrect);
        }
        return isCorrect;
    }

    /**
     * Tăng vị trí câu hỏi sang câu tiếp theo. Tự động kết thúc bài thi nếu làm xong câu cuối.
     */
    public void advanceToNextQuestion() {
        questionIndex++;
        if (isSessionCompleted()) {
            finishSession();
        }
    }

    /**
     * Kết thúc phiên học: lưu bài quiz và kết quả làm bài vào Room DB, tăng bộ đếm bài làm.
     */
    public void finishSession() {
        if (sessionFinished) {
            return;
        }
        sessionFinished = true;
        if (topicController != null && getTotalQuestions() > 0) {
            int score = (correctAnswersCount * 100) / getTotalQuestions();
            Integer topicId = (topic != null && topic.getId() > 0) ? topic.getId() : null;
            String name = isExam ? "Kiểm tra tổng hợp" : ("Ôn tập " + (topic != null ? topic.getTitle() : "tổng hợp"));
            topicController.saveQuizResult(name, topicId, getTotalQuestions(), score, correctAnswersCount);
        }
        if (dailyProgressManager != null) {
            dailyProgressManager.incrementQuizCompleted();
        }
    }

    /**
     * Kiểm tra câu hỏi hiện tại có phải câu cuối cùng không.
     */
    public boolean isLastQuestion() {
        return sessionVocabs != null && questionIndex == sessionVocabs.size() - 1;
    }

    public TopicEntity getTopic() {
        return topic;
    }

    public boolean isExam() {
        return isExam;
    }

    public List<VocabEntity> getCustomVocabs() {
        return customVocabs;
    }

    public int getQuestionIndex() {
        return questionIndex;
    }

    public int getTotalQuestions() {
        return sessionVocabs != null ? sessionVocabs.size() : 0;
    }

    public int getCorrectAnswersCount() {
        return correctAnswersCount;
    }
}
