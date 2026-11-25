package com.example.vocabmaster.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.vocabmaster.R;
import com.example.vocabmaster.model.database.AppDatabase;
import com.example.vocabmaster.model.entity.LearningProgressEntity;
import com.example.vocabmaster.model.entity.QuizEntity;
import com.example.vocabmaster.model.entity.QuizResultEntity;
import com.example.vocabmaster.model.entity.VocabEntity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller quản lý đăng nhập Firebase Authentication và đồng bộ dữ liệu 2 chiều (Push & Pull/Merge)
 * giữa Room Database cục bộ và Cloud Firestore theo mô hình Offline-First.
 */
public class FirebaseSyncController {

    private static final int BUILTIN_VOCAB_MAX_ID = 120;

    private static FirebaseSyncController instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private GoogleSignInClient googleSignInClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);

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

    public boolean isSyncing() {
        return isSyncing.get();
    }

    /**
     * Sinh khóa tài liệu ổn định theo nội dung (topicId + word chuẩn hóa) bằng SHA-256
     * để tránh xung đột khóa chính tự tăng cục bộ giữa nhiều thiết bị trên Firestore.
     */
    public static String buildCustomVocabDocKey(int topicId, String word) {
        if (word == null) return null;
        String normalizedWord = word.trim().toLowerCase(Locale.ROOT);
        if (normalizedWord.isEmpty()) return null;

        String rawKey = topicId + "_" + normalizedWord;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) hex.append('0');
                hex.append(part);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(rawKey.hashCode()) + "_" + rawKey.length();
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

        String stateDocId = String.valueOf(vocab.getId());
        if (vocab.getId() > BUILTIN_VOCAB_MAX_ID) {
            String docKey = buildCustomVocabDocKey(vocab.getTopicId(), vocab.getWord());
            if (docKey != null) {
                stateDocId = docKey;
                data.put("docKey", docKey);
                data.put("word", vocab.getWord().trim());
            }
        }

        firestore.collection("users").document(uid)
                .collection("vocab_states").document(stateDocId)
                .set(data, SetOptions.merge());
    }

    /**
     * PUSH: Đẩy từ vựng tự thêm thủ công (id > 120) lên Firestore với docKey ổn định theo nội dung.
     */
    public void pushCustomVocab(VocabEntity vocab) {
        String uid = getCurrentUid();
        if (uid == null || vocab == null || (!vocab.isCustom() && vocab.getId() <= BUILTIN_VOCAB_MAX_ID)) return;

        String docKey = buildCustomVocabDocKey(vocab.getTopicId(), vocab.getWord());
        if (docKey == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("docKey", docKey);
        data.put("vocabId", vocab.getId());
        data.put("topicId", vocab.getTopicId());
        data.put("word", vocab.getWord().trim());
        data.put("phonetic", vocab.getPhonetic());
        data.put("meaning", vocab.getMeaning());
        data.put("example", vocab.getExample());
        data.put("exampleMeaning", vocab.getExampleMeaning());
        data.put("isLearned", vocab.isLearned());
        data.put("isFavorite", vocab.isFavorite());
        data.put("updatedAt", System.currentTimeMillis());

        firestore.collection("users").document(uid)
                .collection("custom_vocabs").document(docKey)
                .set(data, SetOptions.merge());
    }

    /**
     * DELETE: Xóa từ vựng tự thêm thủ công khỏi Firestore theo docKey ổn định.
     */
    public void deleteCustomVocabOnCloud(VocabEntity vocab) {
        String uid = getCurrentUid();
        if (uid == null || vocab == null || (!vocab.isCustom() && vocab.getId() <= BUILTIN_VOCAB_MAX_ID)) return;

        String docKey = buildCustomVocabDocKey(vocab.getTopicId(), vocab.getWord());
        if (docKey == null) return;

        DocumentReference userDoc = firestore.collection("users").document(uid);
        userDoc.collection("custom_vocabs").document(docKey).delete();
        userDoc.collection("vocab_states").document(docKey).delete();
    }

    /**
     * DELETE (tương thích ngược): Xóa theo vocabId cũ khi không có đối tượng {@link VocabEntity}.
     *
     * @deprecated Dùng {@link #deleteCustomVocabOnCloud(VocabEntity)} để xóa theo docKey ổn định.
     */
    @Deprecated
    public void deleteCustomVocabOnCloud(int vocabId) {
        String uid = getCurrentUid();
        if (uid == null || vocabId <= BUILTIN_VOCAB_MAX_ID) return;

        DocumentReference userDoc = firestore.collection("users").document(uid);
        userDoc.collection("custom_vocabs").document(String.valueOf(vocabId)).delete();
        userDoc.collection("vocab_states").document(String.valueOf(vocabId)).delete();
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
     * PUSH: Đẩy cài đặt học tập hằng ngày lên Firestore kèm {@code todayDate} và {@code updatedAt}.
     */
    public void pushDailySettings(DailyProgressManager manager) {
        String uid = getCurrentUid();
        if (uid == null || manager == null) return;

        long updatedAt = manager.getSettingsUpdatedAt();
        if (updatedAt <= 0L) {
            updatedAt = System.currentTimeMillis();
            manager.setSettingsUpdatedAt(updatedAt);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("dailyGoal", manager.getDailyGoal());
        data.put("todayDate", manager.getTodayDate());
        data.put("todayWordsCount", manager.getTodayWordsCount());
        data.put("totalQuizCompleted", manager.getTotalQuizCompleted());
        data.put("ttsLocale", manager.getTtsLocale());
        data.put("ttsSpeed", manager.getTtsSpeed());
        data.put("reminderHour", manager.getReminderHour());
        data.put("reminderMinute", manager.getReminderMinute());
        data.put("reminderEnabled", manager.isReminderEnabled());
        data.put("updatedAt", updatedAt);

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
            notifySyncError(callback, "Bạn chưa đăng nhập tài khoản!");
            return;
        }
        if (context == null) {
            notifySyncError(callback, "Thiếu context để đồng bộ dữ liệu!");
            return;
        }
        if (!isSyncing.compareAndSet(false, true)) {
            return;
        }

        Context appContext = context.getApplicationContext() != null ? context.getApplicationContext() : context;
        DocumentReference userDoc = firestore.collection("users").document(uid);

        Task<QuerySnapshot> customVocabsTask = userDoc.collection("custom_vocabs").get();
        Task<QuerySnapshot> vocabStatesTask = userDoc.collection("vocab_states").get();
        Task<QuerySnapshot> quizResultsTask = userDoc.collection("quiz_results").get();
        Task<DocumentSnapshot> settingsTask = userDoc.collection("settings").document("daily_settings").get();

        Tasks.whenAllComplete(customVocabsTask, vocabStatesTask, quizResultsTask, settingsTask)
                .addOnCompleteListener(allTask -> {
                    Exception firstFailure = findFirstTaskException(
                            customVocabsTask, vocabStatesTask, quizResultsTask, settingsTask);
                    if (firstFailure != null) {
                        isSyncing.set(false);
                        notifySyncError(callback, firstFailure.getLocalizedMessage());
                        return;
                    }

                    QuerySnapshot customSnapshots = customVocabsTask.getResult();
                    QuerySnapshot stateSnapshots = vocabStatesTask.getResult();
                    QuerySnapshot resultSnapshots = quizResultsTask.getResult();
                    DocumentSnapshot settingsDoc = settingsTask.getResult();

                    try {
                        executor.execute(() -> {
                            try {
                                // Pull & merge settings trước để tránh ghi đè Cloud bằng giá trị mặc định cục bộ
                                mergeDailySettings(appContext, manager, settingsDoc);

                                AppDatabase db = AppDatabase.getInstance(appContext);
                                db.runInTransaction(() -> {
                                    mergeCustomVocabs(db, customSnapshots);
                                    mergeVocabStates(db, stateSnapshots);
                                    mergeQuizResults(db, resultSnapshots);
                                });

                                isSyncing.set(false);
                                notifySyncSuccess(callback);
                            } catch (Exception e) {
                                isSyncing.set(false);
                                notifySyncError(callback, e.getLocalizedMessage());
                            }
                        });
                    } catch (RejectedExecutionException e) {
                        isSyncing.set(false);
                        notifySyncError(callback, e.getLocalizedMessage());
                    }
                });
    }

    private void mergeCustomVocabs(AppDatabase db, QuerySnapshot customSnapshots) {
        if (customSnapshots == null) return;
        for (DocumentSnapshot doc : customSnapshots.getDocuments()) {
            String rawWord = doc.getString("word");
            if (rawWord == null || rawWord.trim().isEmpty()) continue;

            String word = rawWord.trim();
            Long tIdL = getLongCompat(doc, "topicId");
            int tId = (tIdL != null && tIdL > 0) ? tIdL.intValue() : 1;
            String phonetic = doc.getString("phonetic");
            String meaning = doc.getString("meaning");
            if (meaning == null) meaning = "";
            String example = doc.getString("example");
            String exampleMeaning = doc.getString("exampleMeaning");
            boolean isLearned = Boolean.TRUE.equals(doc.getBoolean("isLearned"));
            boolean isFavorite = Boolean.TRUE.equals(doc.getBoolean("isFavorite"));

            VocabEntity existing = db.vocabDao().findByTopicAndWord(tId, word);
            if (existing != null) {
                existing.setPhonetic(phonetic);
                existing.setMeaning(meaning);
                existing.setExample(example);
                existing.setExampleMeaning(exampleMeaning);
                existing.setLearned(isLearned);
                existing.setFavorite(isFavorite);
                db.vocabDao().updateVocab(existing);
            } else {
                VocabEntity customVocab = new VocabEntity(
                        tId, word, phonetic, meaning, example, exampleMeaning, isLearned, isFavorite);
                customVocab.setId(0);
                db.vocabDao().insertVocab(customVocab);
            }
            db.topicDao().updateTopicTotalCount(tId);
            db.topicDao().updateTopicLearnedCount(tId);
        }
    }

    private void mergeVocabStates(AppDatabase db, QuerySnapshot stateSnapshots) {
        if (stateSnapshots == null) return;
        for (DocumentSnapshot doc : stateSnapshots.getDocuments()) {
            Long vIdL = getLongCompat(doc, "vocabId");
            Long tIdL = getLongCompat(doc, "topicId");
            int tId = (tIdL != null && tIdL > 0) ? tIdL.intValue() : 1;
            String word = doc.getString("word");

            int targetVocabId = -1;
            if (word != null && !word.trim().isEmpty()) {
                VocabEntity matched = db.vocabDao().findByTopicAndWord(tId, word.trim());
                if (matched != null) {
                    targetVocabId = matched.getId();
                }
            } else if (vIdL != null && vIdL.intValue() > 0 && vIdL.intValue() <= BUILTIN_VOCAB_MAX_ID) {
                targetVocabId = vIdL.intValue();
            }
            if (targetVocabId <= 0) continue;

            boolean isLearned = Boolean.TRUE.equals(doc.getBoolean("isLearned"));
            boolean isFavorite = Boolean.TRUE.equals(doc.getBoolean("isFavorite"));
            String status = doc.getString("status");
            Long correctL = getLongCompat(doc, "correctCount");
            Long wrongL = getLongCompat(doc, "wrongCount");
            Long nextReviewL = getLongCompat(doc, "nextReview");
            int correctCount = correctL != null ? correctL.intValue() : 0;
            int wrongCount = wrongL != null ? wrongL.intValue() : 0;
            long nextReview = nextReviewL != null ? nextReviewL : 0L;

            db.vocabDao().updateLearnedStatus(targetVocabId, isLearned);
            db.vocabDao().updateFavoriteStatus(targetVocabId, isFavorite);
            db.topicDao().updateTopicLearnedCount(tId);

            if (status != null) {
                LearningProgressEntity progress = new LearningProgressEntity(
                        targetVocabId, status, correctCount, wrongCount, nextReview);
                db.learningProgressDao().insertProgress(progress);
            }
        }
    }

    private void mergeQuizResults(AppDatabase db, QuerySnapshot resultSnapshots) {
        if (resultSnapshots == null) return;
        for (DocumentSnapshot doc : resultSnapshots.getDocuments()) {
            Long completedAtL = getLongCompat(doc, "completedAt");
            if (completedAtL == null) {
                try {
                    completedAtL = Long.parseLong(doc.getId());
                } catch (NumberFormatException ignored) {
                    continue;
                }
            }
            long completedAt = completedAtL;
            if (db.quizResultDao().countByCompletedAt(completedAt) > 0) {
                continue;
            }

            String qName = doc.getString("quizName");
            if (qName == null || qName.trim().isEmpty()) {
                qName = "Bài kiểm tra";
            }
            Long tIdL = getLongCompat(doc, "topicId");
            Integer tId = (tIdL != null && tIdL > 0) ? tIdL.intValue() : null;
            Long totalQL = getLongCompat(doc, "totalQuestions");
            Long scoreL = getLongCompat(doc, "score");
            Long correctAL = getLongCompat(doc, "correctAnswers");
            int totalQ = totalQL != null ? totalQL.intValue() : 10;
            int score = scoreL != null ? scoreL.intValue() : 0;
            int correctA = correctAL != null ? correctAL.intValue() : 0;

            QuizEntity quiz = new QuizEntity(qName, tId, totalQ);
            long qId = db.quizDao().insertQuiz(quiz);
            QuizResultEntity qResult = new QuizResultEntity((int) qId, score, correctA, completedAt);
            db.quizResultDao().insertResult(qResult);
        }
    }

    private void mergeDailySettings(Context context, DailyProgressManager manager, DocumentSnapshot settingsDoc) {
        if (manager == null) return;

        long localUpdatedAt = manager.getSettingsUpdatedAt();
        boolean exists = settingsDoc != null && settingsDoc.exists();
        Long updatedAtL = exists ? getLongCompat(settingsDoc, "updatedAt") : null;
        long cloudUpdatedAt = updatedAtL != null ? updatedAtL : 0L;

        if (shouldPullSettings(exists, cloudUpdatedAt, localUpdatedAt)) {
            Long goalL = getLongCompat(settingsDoc, "dailyGoal");
            String locale = settingsDoc.getString("ttsLocale");
            Double speedD = getDoubleCompat(settingsDoc, "ttsSpeed");
            String cloudTodayDate = settingsDoc.getString("todayDate");
            Long todayWordsL = getLongCompat(settingsDoc, "todayWordsCount");
            Long totalQuizL = getLongCompat(settingsDoc, "totalQuizCompleted");
            Long reminderHourL = getLongCompat(settingsDoc, "reminderHour");
            Long reminderMinuteL = getLongCompat(settingsDoc, "reminderMinute");
            Boolean reminderEnabledB = settingsDoc.getBoolean("reminderEnabled");

            applyCloudSettings(
                    manager,
                    goalL != null ? goalL.intValue() : null,
                    locale,
                    speedD != null ? speedD.floatValue() : null,
                    cloudTodayDate,
                    todayWordsL != null ? todayWordsL.intValue() : null,
                    totalQuizL != null ? totalQuizL.intValue() : null,
                    reminderHourL != null ? reminderHourL.intValue() : null,
                    reminderMinuteL != null ? reminderMinuteL.intValue() : null,
                    reminderEnabledB,
                    cloudUpdatedAt,
                    () -> ReminderScheduler.scheduleDailyReminder(
                            context, manager.getReminderHour(), manager.getReminderMinute()),
                    () -> ReminderScheduler.cancelReminder(context)
            );
        } else if (shouldPushSettings(exists, cloudUpdatedAt, localUpdatedAt)) {
            pushDailySettings(manager);
        }
    }

    static boolean shouldPullSettings(boolean cloudExists, long cloudUpdatedAt, long localUpdatedAt) {
        return cloudExists && cloudUpdatedAt > localUpdatedAt;
    }

    static boolean shouldPushSettings(boolean cloudExists, long cloudUpdatedAt, long localUpdatedAt) {
        return !cloudExists || localUpdatedAt > cloudUpdatedAt;
    }

    static void applyCloudSettings(
            DailyProgressManager manager,
            Integer dailyGoal,
            String ttsLocale,
            Float ttsSpeed,
            String cloudTodayDate,
            Integer todayWordsCount,
            Integer totalQuizCompleted,
            Integer reminderHour,
            Integer reminderMinute,
            Boolean reminderEnabled,
            long cloudUpdatedAt,
            Runnable onReminderEnabled,
            Runnable onReminderDisabled
    ) {
        if (manager == null) return;

        if (dailyGoal != null && dailyGoal > 0) {
            manager.setDailyGoal(dailyGoal);
        }
        if (ttsLocale != null && !ttsLocale.trim().isEmpty()) {
            manager.setTtsLocale(ttsLocale);
        }
        if (ttsSpeed != null && ttsSpeed > 0f) {
            manager.setTtsSpeed(ttsSpeed);
        }
        if (todayWordsCount != null && todayWordsCount >= 0
                && manager.getTodayDate().equals(cloudTodayDate)) {
            int mergedTodayWords = Math.max(manager.getTodayWordsCount(), todayWordsCount);
            manager.setTodayWordsCount(mergedTodayWords);
        }
        if (totalQuizCompleted != null && totalQuizCompleted >= 0) {
            int mergedTotalQuiz = Math.max(manager.getTotalQuizCompleted(), totalQuizCompleted);
            manager.setTotalQuizCompleted(mergedTotalQuiz);
        }
        if (reminderHour != null || reminderMinute != null || reminderEnabled != null) {
            int hour = reminderHour != null ? reminderHour : manager.getReminderHour();
            int minute = reminderMinute != null ? reminderMinute : manager.getReminderMinute();
            boolean enabled = reminderEnabled != null ? reminderEnabled : manager.isReminderEnabled();
            manager.setReminderTime(hour, minute, enabled);
            if (enabled) {
                if (onReminderEnabled != null) onReminderEnabled.run();
            } else {
                if (onReminderDisabled != null) onReminderDisabled.run();
            }
        }
        manager.setSettingsUpdatedAt(cloudUpdatedAt);
    }

    private static Long getLongCompat(DocumentSnapshot doc, String field) {
        Object val = doc.get(field);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return null;
    }

    private static Double getDoubleCompat(DocumentSnapshot doc, String field) {
        Object val = doc.get(field);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return null;
    }

    private static Exception findFirstTaskException(Task<?>... tasks) {
        for (Task<?> task : tasks) {
            if (task == null) {
                return new IllegalStateException("Task đồng bộ không hợp lệ.");
            }
            if (!task.isSuccessful()) {
                Exception ex = task.getException();
                return ex != null ? ex : new IllegalStateException("Tác vụ đồng bộ bị hủy hoặc thất bại.");
            }
        }
        return null;
    }

    private void notifySyncSuccess(SyncCallback callback) {
        if (callback == null) return;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback.onSuccess();
        } else {
            mainHandler.post(callback::onSuccess);
        }
    }

    private void notifySyncError(SyncCallback callback, String message) {
        if (callback == null) return;
        String safeMessage = (message != null && !message.trim().isEmpty())
                ? message
                : "Đã xảy ra lỗi khi đồng bộ dữ liệu.";
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback.onError(safeMessage);
        } else {
            mainHandler.post(() -> callback.onError(safeMessage));
        }
    }
}
