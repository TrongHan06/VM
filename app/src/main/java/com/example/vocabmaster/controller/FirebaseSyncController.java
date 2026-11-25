package com.example.vocabmaster.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.vocabmaster.R;
import com.example.vocabmaster.model.database.AppDatabase;
import com.example.vocabmaster.model.entity.LearningProgressEntity;
import com.example.vocabmaster.model.entity.QuizEntity;
import com.example.vocabmaster.model.entity.QuizResultEntity;
import com.example.vocabmaster.model.entity.TopicEntity;
import com.example.vocabmaster.model.entity.VocabEntity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller quản lý đăng nhập Firebase Authentication và đồng bộ dữ liệu 2 chiều (Push & Pull/Merge)
 * giữa Room Database cục bộ và Cloud Firestore theo mô hình Offline-First.
 */
public class FirebaseSyncController {

    private static FirebaseSyncController instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private GoogleSignInClient googleSignInClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String errorMessage);
    }

    public interface SyncCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public static synchronized FirebaseSyncController getInstance() {
        if (instance == null) {
            instance = new FirebaseSyncController();
        }
        return instance;
    }

    private FirebaseSyncController() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public String getCurrentUid() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public void signIn(String email, String password, AuthCallback callback) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            if (callback != null) callback.onError("Vui lòng nhập Email và Mật khẩu!");
            return;
        }
        auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> {
                    if (callback != null) callback.onSuccess(authResult.getUser());
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage());
                });
    }

    public void signUp(String email, String password, AuthCallback callback) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            if (callback != null) callback.onError("Vui lòng nhập Email và Mật khẩu!");
            return;
        }
        auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> {
                    if (callback != null) callback.onSuccess(authResult.getUser());
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage());
                });
    }

    public GoogleSignInClient getGoogleSignInClient(Context context) {
        if (googleSignInClient == null && context != null) {
            String defaultWebClientId = context.getString(R.string.default_web_client_id);
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(defaultWebClientId)
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(context.getApplicationContext(), gso);
        }
        return googleSignInClient;
    }

    public void signInWithGoogleIdToken(String idToken, AuthCallback callback) {
        if (idToken == null || idToken.trim().isEmpty()) {
            if (callback != null) callback.onError("ID Token từ Google không hợp lệ!");
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken.trim(), null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    if (callback != null) callback.onSuccess(authResult.getUser());
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage());
                });
    }

    public void signOut() {
        auth.signOut();
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        }
    }

    public void signOut(Context context) {
        auth.signOut();
        if (googleSignInClient == null && context != null) {
            getGoogleSignInClient(context);
        }
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        }
    }

    /**
     * PUSH: Đẩy trạng thái học (isLearned, isFavorite, tiến độ SRS) của từ vựng lên Firestore.
     */
    public void pushVocabState(VocabEntity vocab, LearningProgressEntity progress) {
        String uid = getCurrentUid();
        if (uid == null || vocab == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("vocabId", vocab.getId());
        data.put("topicId", vocab.getTopicId());
        data.put("isLearned", vocab.isLearned());
        data.put("isFavorite", vocab.isFavorite());
        data.put("status", progress != null ? progress.getStatus() : "LEARNING");
        data.put("correctCount", progress != null ? progress.getCorrectCount() : 0);
        data.put("wrongCount", progress != null ? progress.getWrongCount() : 0);
        data.put("nextReview", progress != null ? progress.getNextReview() : 0L);
        data.put("updatedAt", System.currentTimeMillis());

        firestore.collection("users").document(uid)
                .collection("vocab_states").document(String.valueOf(vocab.getId()))
                .set(data, SetOptions.merge());
    }

    /**
     * PUSH: Đẩy từ vựng tự thêm thủ công (id > 120) lên Firestore.
     */
    public void pushCustomVocab(VocabEntity vocab) {
        String uid = getCurrentUid();
        if (uid == null || vocab == null || vocab.getId() <= 120) return;

        Map<String, Object> data = new HashMap<>();
        data.put("vocabId", vocab.getId());
        data.put("topicId", vocab.getTopicId());
        data.put("word", vocab.getWord());
        data.put("phonetic", vocab.getPhonetic());
        data.put("meaning", vocab.getMeaning());
        data.put("example", vocab.getExample());
        data.put("exampleMeaning", vocab.getExampleMeaning());
        data.put("isLearned", vocab.isLearned());
        data.put("isFavorite", vocab.isFavorite());
        data.put("updatedAt", System.currentTimeMillis());

        firestore.collection("users").document(uid)
                .collection("custom_vocabs").document(String.valueOf(vocab.getId()))
                .set(data, SetOptions.merge());
    }

    /**
     * DELETE: Xóa từ vựng tự thêm thủ công khỏi Firestore.
     */
    public void deleteCustomVocabOnCloud(int vocabId) {
        String uid = getCurrentUid();
        if (uid == null || vocabId <= 120) return;

        firestore.collection("users").document(uid)
                .collection("custom_vocabs").document(String.valueOf(vocabId))
                .delete();

        firestore.collection("users").document(uid)
                .collection("vocab_states").document(String.valueOf(vocabId))
                .delete();
    }

    /**
     * PUSH: Đẩy kết quả làm bài kiểm tra Quiz lên Firestore.
     */
    public void pushQuizResult(String quizName, Integer topicId, int totalQuestions, int score, int correctAnswers, long completedAt) {
        String uid = getCurrentUid();
        if (uid == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("quizName", quizName);
        data.put("topicId", topicId != null ? topicId : 0);
        data.put("totalQuestions", totalQuestions);
        data.put("score", score);
        data.put("correctAnswers", correctAnswers);
        data.put("completedAt", completedAt);

        firestore.collection("users").document(uid)
                .collection("quiz_results").document(String.valueOf(completedAt))
                .set(data, SetOptions.merge());
    }

    /**
     * PUSH: Đẩy cài đặt học tập hằng ngày lên Firestore.
     */
    public void pushDailySettings(DailyProgressManager manager) {
        String uid = getCurrentUid();
        if (uid == null || manager == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("dailyGoal", manager.getDailyGoal());
        data.put("todayWordsCount", manager.getTodayWordsCount());
        data.put("totalQuizCompleted", manager.getTotalQuizCompleted());
        data.put("ttsLocale", manager.getTtsLocale());
        data.put("ttsSpeed", manager.getTtsSpeed());
        data.put("reminderHour", manager.getReminderHour());
        data.put("reminderMinute", manager.getReminderMinute());
        data.put("reminderEnabled", manager.isReminderEnabled());
        data.put("updatedAt", System.currentTimeMillis());

        firestore.collection("users").document(uid)
                .collection("settings").document("daily_settings")
                .set(data, SetOptions.merge());
    }

    /**
     * PULL & MERGE: Tải toàn bộ dữ liệu từ Cloud Firestore về và gộp (Merge) an toàn vào Room Database cục bộ.
     */
    public void syncAllFromCloud(Context context, DailyProgressManager manager, SyncCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) {
            if (callback != null) callback.onError("Bạn chưa đăng nhập tài khoản!");
            return;
        }

        if (manager != null) {
            pushDailySettings(manager);
        }

        firestore.collection("users").document(uid).collection("custom_vocabs").get()
                .addOnSuccessListener(customSnapshots -> {
                    firestore.collection("users").document(uid).collection("vocab_states").get()
                            .addOnSuccessListener(stateSnapshots -> {
                                firestore.collection("users").document(uid).collection("quiz_results").get()
                                        .addOnSuccessListener(resultSnapshots -> {
                                            firestore.collection("users").document(uid).collection("settings").document("daily_settings").get()
                                                    .addOnSuccessListener(settingsDoc -> {
                                                        executor.execute(() -> {
                                                            try {
                                                                AppDatabase db = AppDatabase.getInstance(context);

                                                                // 1. Merge custom vocabs
                                                                for (DocumentSnapshot doc : customSnapshots.getDocuments()) {
                                                                    Long vIdL = doc.getLong("vocabId");
                                                                    if (vIdL == null) continue;
                                                                    int vId = vIdL.intValue();
                                                                    int tId = doc.getLong("topicId") != null ? doc.getLong("topicId").intValue() : 1;
                                                                    String word = doc.getString("word");
                                                                    String phonetic = doc.getString("phonetic");
                                                                    String meaning = doc.getString("meaning");
                                                                    String example = doc.getString("example");
                                                                    String exampleMeaning = doc.getString("exampleMeaning");
                                                                    boolean isLearned = Boolean.TRUE.equals(doc.getBoolean("isLearned"));
                                                                    boolean isFavorite = Boolean.TRUE.equals(doc.getBoolean("isFavorite"));

                                                                    VocabEntity customVocab = new VocabEntity(tId, word, phonetic, meaning, example, exampleMeaning, isLearned, isFavorite);
                                                                    customVocab.setId(vId);
                                                                    db.vocabDao().insertVocab(customVocab);
                                                                    db.topicDao().updateTopicTotalCount(tId);
                                                                }

                                                                // 2. Merge vocab states & SRS progress
                                                                for (DocumentSnapshot doc : stateSnapshots.getDocuments()) {
                                                                    Long vIdL = doc.getLong("vocabId");
                                                                    if (vIdL == null) continue;
                                                                    int vId = vIdL.intValue();
                                                                    int tId = doc.getLong("topicId") != null ? doc.getLong("topicId").intValue() : 1;
                                                                    boolean isLearned = Boolean.TRUE.equals(doc.getBoolean("isLearned"));
                                                                    boolean isFavorite = Boolean.TRUE.equals(doc.getBoolean("isFavorite"));
                                                                    String status = doc.getString("status");
                                                                    int correctCount = doc.getLong("correctCount") != null ? doc.getLong("correctCount").intValue() : 0;
                                                                    int wrongCount = doc.getLong("wrongCount") != null ? doc.getLong("wrongCount").intValue() : 0;
                                                                    long nextReview = doc.getLong("nextReview") != null ? doc.getLong("nextReview") : 0L;

                                                                    db.vocabDao().updateLearnedStatus(vId, isLearned);
                                                                    db.vocabDao().updateFavoriteStatus(vId, isFavorite);
                                                                    db.topicDao().updateTopicLearnedCount(tId);

                                                                    if (status != null) {
                                                                        LearningProgressEntity progress = new LearningProgressEntity(vId, status, correctCount, wrongCount, nextReview);
                                                                        db.learningProgressDao().insertProgress(progress);
                                                                    }
                                                                }

                                                                // 3. Merge quiz results
                                                                for (DocumentSnapshot doc : resultSnapshots.getDocuments()) {
                                                                    String qName = doc.getString("quizName");
                                                                    Long tIdL = doc.getLong("topicId");
                                                                    Integer tId = (tIdL != null && tIdL > 0) ? tIdL.intValue() : null;
                                                                    int totalQ = doc.getLong("totalQuestions") != null ? doc.getLong("totalQuestions").intValue() : 10;
                                                                    int score = doc.getLong("score") != null ? doc.getLong("score").intValue() : 0;
                                                                    int correctA = doc.getLong("correctAnswers") != null ? doc.getLong("correctAnswers").intValue() : 0;
                                                                    long completedAt = doc.getLong("completedAt") != null ? doc.getLong("completedAt") : System.currentTimeMillis();

                                                                    QuizEntity quiz = new QuizEntity(qName, tId, totalQ);
                                                                    long qId = db.quizDao().insertQuiz(quiz);
                                                                    QuizResultEntity qResult = new QuizResultEntity((int) qId, score, correctA, completedAt);
                                                                    db.quizResultDao().insertResult(qResult);
                                                                }

                                                                // 4. Merge settings
                                                                if (settingsDoc.exists() && manager != null) {
                                                                    Long goalL = settingsDoc.getLong("dailyGoal");
                                                                    if (goalL != null) manager.setDailyGoal(goalL.intValue());

                                                                    String locale = settingsDoc.getString("ttsLocale");
                                                                    if (locale != null) manager.setTtsLocale(locale);

                                                                    Double speedD = settingsDoc.getDouble("ttsSpeed");
                                                                    if (speedD != null) manager.setTtsSpeed(speedD.floatValue());
                                                                }

                                                                if (callback != null) {
                                                                    mainHandler.post(callback::onSuccess);
                                                                }
                                                            } catch (Exception e) {
                                                                if (callback != null) {
                                                                    mainHandler.post(() -> callback.onError(e.getLocalizedMessage()));
                                                                }
                                                            }
                                                        });
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        if (callback != null) callback.onError(e.getLocalizedMessage());
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            if (callback != null) callback.onError(e.getLocalizedMessage());
                                        });
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onError(e.getLocalizedMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getLocalizedMessage());
                });
    }
}
