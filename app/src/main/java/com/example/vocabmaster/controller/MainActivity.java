package com.example.vocabmaster.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabmaster.R;
import com.example.vocabmaster.model.entity.DashboardStats;
import com.example.vocabmaster.model.entity.PronunciationResult;
import com.example.vocabmaster.model.entity.TopicEntity;
import com.example.vocabmaster.model.entity.TopicProgress;
import com.example.vocabmaster.model.entity.VocabEntity;
import com.example.vocabmaster.view.adapter.TopicAdapter;
import com.example.vocabmaster.view.adapter.VocabAdapter;
import com.example.vocabmaster.view.dialog.AppInfoDialogView;
import com.example.vocabmaster.view.dialog.FlashcardDialogView;
import com.example.vocabmaster.view.dialog.PracticeDialogView;
import com.example.vocabmaster.view.dialog.VocabDialogView;
import com.example.vocabmaster.view.helper.ProgressAnimHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller điều phối trung tâm của ứng dụng VocabMaster.
 * Đóng vai trò làm cầu nối giữa tầng View (UI) và các Controller nghiệp vụ.
 */
public class MainActivity extends AppCompatActivity {

    private TopicController topicController;
    private LearningProgressController learningProgressController;
    private DailyProgressManager dailyProgressManager;
    private SpeechController speechController;

    private final ProgressAnimHelper dailyGoalAnimHelper = new ProgressAnimHelper();
    private final ProgressAnimHelper detailTopicAnimHelper = new ProgressAnimHelper();
    private LiveData<TopicProgress> currentTopicProgressLiveData;

    private VocabAdapter wordListAdapter;
    private Runnable updateWordListRunnable;

    private BottomSheetDialog activeSearchDialog;
    private VocabDialogView.SpeechPracticeDialogViewHolder speechDialogViewHolder;

    /**
     * ActivityResultLauncher nhận diện giọng nói từ Google Speech Recognition (STT).
     */
    private final ActivityResultLauncher<Intent> speechLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty() && speechDialogViewHolder != null) {
                        String targetWord = speechDialogViewHolder.getCurrentTargetWord();
                        PronunciationResult eval = learningProgressController.evaluatePronunciation(targetWord, matches.get(0));
                        speechDialogViewHolder.updatePronunciationResult(eval);
                    }
                }
            }
    );

    /**
     * ActivityResultLauncher Đăng nhập Google Sign-In.
     */
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null && account.getIdToken() != null) {
                            String idToken = account.getIdToken();
                            Toast.makeText(this, "Đang xác thực tài khoản Google...", Toast.LENGTH_SHORT).show();
                            FirebaseSyncController syncController = FirebaseSyncController.getInstance();
                            syncController.signInWithGoogleIdToken(idToken, new FirebaseSyncController.AuthCallback() {
                                @Override
                                public void onSuccess(FirebaseUser user) {
                                    Toast.makeText(MainActivity.this, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show();
                                    syncController.syncAllFromCloud(MainActivity.this, dailyProgressManager, new FirebaseSyncController.SyncCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(MainActivity.this, "Đã đồng bộ dữ liệu từ Cloud!", Toast.LENGTH_SHORT).show();
                                            updateDashboardStats();
                                            if (updateWordListRunnable != null) updateWordListRunnable.run();
                                        }

                                        @Override
                                        public void onError(String errorMessage) {
                                            Toast.makeText(MainActivity.this, "Đồng bộ thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    Toast.makeText(MainActivity.this, "Đăng nhập Google thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } catch (ApiException e) {
                        Toast.makeText(this, "Lỗi đăng nhập Google: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

    /**
     * ActivityResultLauncher xin quyền POST_NOTIFICATIONS trên Android 13+ (TIRAMISU).
     */
    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Đã cấp quyền thông báo thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Bạn đã từ chối quyền thông báo. Không thể nhận nhắc nhở học tập.", Toast.LENGTH_LONG).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        topicController = new TopicController(this);
        learningProgressController = new LearningProgressController(this);
        dailyProgressManager = new DailyProgressManager(this);
        speechController = new SpeechController(this, dailyProgressManager);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                View detailView = findViewById(R.id.nestedScrollViewTopicDetail);
                if (detailView != null && detailView.getVisibility() == View.VISIBLE) {
                    hideTopicDetailScreen();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });

        setupBottomNavigation();
        setupWindowInsets();
        setupTopicsGrid();
        setupHeaderAndHomeCards();
        setupTestTabCards();
        setupInfoTabCards();
        setupWordListFiltersAndSearch();
    }

    /**
     * Thiết lập BottomNavigationView để chuyển đổi giữa 4 tab chính với hiệu ứng animation.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setItemIconTintList(null);
        bottomNav.setOnItemSelectedListener(item -> {
            View homeView = findViewById(R.id.nestedScrollViewHome);
            View testView = findViewById(R.id.nestedScrollViewTest);
            View wordListView = findViewById(R.id.nestedScrollViewWordList);
            View infoView = findViewById(R.id.nestedScrollViewInfo);
            View detailView = findViewById(R.id.nestedScrollViewTopicDetail);

            if (detailView != null) detailView.setVisibility(View.GONE);

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                ProgressAnimHelper.switchTabWithAnimation(homeView, testView, wordListView, infoView);
                return true;
            } else if (id == R.id.nav_quiz) {
                ProgressAnimHelper.switchTabWithAnimation(testView, homeView, wordListView, infoView);
                return true;
            } else if (id == R.id.nav_word_list) {
                ProgressAnimHelper.switchTabWithAnimation(wordListView, homeView, testView, infoView);
                return true;
            } else if (id == R.id.nav_info) {
                ProgressAnimHelper.switchTabWithAnimation(infoView, homeView, testView, wordListView);
                return true;
            }
            return false;
        });
    }

    /**
     * Thiết lập WindowInsets để xử lý tràn lề thanh trạng thái (Status Bar) và Navigation Bar.
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            View headerView = findViewById(R.id.tvAppTitle);
            if (headerView != null && headerView.getParent() != null && headerView.getParent().getParent() instanceof View) {
                View topHeaderLayout = (View) headerView.getParent().getParent();
                topHeaderLayout.setPadding(topHeaderLayout.getPaddingLeft(), systemBars.top + 16, topHeaderLayout.getPaddingRight(), topHeaderLayout.getPaddingBottom());
            }
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigation), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Thiết lập RecyclerView hiển thị danh sách dạng lưới (Grid) các chủ đề từ vựng.
     */
    private void setupTopicsGrid() {
        RecyclerView rvTopics = findViewById(R.id.rvTopics);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int spanCount = dpWidth >= 900 ? 4 : (dpWidth >= 600 ? 3 : 2);
        rvTopics.setLayoutManager(new GridLayoutManager(this, spanCount));

        topicController.getAllTopicsLiveData().observe(this, topicEntities -> {
            if (topicEntities != null && !topicEntities.isEmpty()) {
                TopicAdapter adapter = new TopicAdapter(topicEntities, this::showTopicDetailScreen);
                rvTopics.setAdapter(adapter);

                topicController.getAllTopicProgressLiveData().observe(this, progressList -> {
                    if (progressList != null) {
                        java.util.Map<Integer, com.example.vocabmaster.model.entity.TopicProgressItem> progressMap = new java.util.HashMap<>();
                        for (com.example.vocabmaster.model.entity.TopicProgressItem item : progressList) {
                            progressMap.put(item.getTopicId(), item);
                        }
                        adapter.updateProgressMap(progressMap);
                    }
                });
                updateDashboardStats();
            }
        });
    }

    /**
     * Gán sự kiện click cho các nút cài đặt trên Header và các thẻ lối tắt trên Trang chủ.
     */
    private void setupHeaderAndHomeCards() {
        ImageButton btnReminderSetting = findViewById(R.id.btnReminderSetting);
        ImageButton btnSearchAction = findViewById(R.id.btnSearchAction);
        View btnEditDailyGoal = findViewById(R.id.btnEditDailyGoal);
        View btnReviewWrongWords = findViewById(R.id.btnReviewWrongWords);
        View btnAddCustomWord = findViewById(R.id.btnAddCustomWord);
        View btnSpeechCheck = findViewById(R.id.btnSpeechCheck);

        if (btnSearchAction != null) {
            btnSearchAction.setOnClickListener(v -> openSearchDialog());
        }

        if (btnReminderSetting != null) {
            btnReminderSetting.setOnClickListener(v -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                    }
                }
                AppInfoDialogView.showReminderDialog(
                        this, dailyProgressManager.getReminderHour(), dailyProgressManager.getReminderMinute(),
                        dailyProgressManager.isReminderEnabled(),
                        (hour, minute, enabled) -> {
                            dailyProgressManager.setReminderTime(hour, minute, enabled);
                            if (enabled) {
                                ReminderScheduler.scheduleDailyReminder(this, hour, minute);
                            } else {
                                ReminderScheduler.cancelReminder(this);
                            }
                        },
                        () -> {
                            Intent testIntent = new Intent(this, ReminderReceiver.class);
                            sendBroadcast(testIntent);
                        }
                );
            });
        }

        if (btnEditDailyGoal != null) {
            btnEditDailyGoal.setOnClickListener(v -> AppInfoDialogView.showEditDailyGoalDialog(
                    this, dailyProgressManager.getDailyGoal(), newGoal -> {
                        dailyProgressManager.setDailyGoal(newGoal);
                        updateDashboardStats();
                    }
            ));
        }

        if (btnReviewWrongWords != null) btnReviewWrongWords.setOnClickListener(v -> startReviewWrongWordsSession());
        if (btnAddCustomWord != null) btnAddCustomWord.setOnClickListener(v -> showAddVocabDialog());
        if (btnSpeechCheck != null) btnSpeechCheck.setOnClickListener(v -> showSpeechPracticeDialog());
    }

    /**
     * Gán sự kiện click cho các Thẻ chức năng ở Tab 2 Kiểm tra.
     */
    private void setupTestTabCards() {
        View cardTakeQuiz = findViewById(R.id.cardTakeQuiz);
        View cardReviewWrongQuiz = findViewById(R.id.cardReviewWrongQuiz);
        View cardSrsReview = findViewById(R.id.cardSrsReview);
        View cardQuizHistory = findViewById(R.id.cardQuizHistory);
        View cardProficiency = findViewById(R.id.cardProficiency);

        if (cardTakeQuiz != null) cardTakeQuiz.setOnClickListener(v -> showPracticeDialog(null, true));
        if (cardReviewWrongQuiz != null) cardReviewWrongQuiz.setOnClickListener(v -> startReviewWrongWordsSession());
        if (cardSrsReview != null) cardSrsReview.setOnClickListener(v -> startSrsReviewSession());
        if (cardQuizHistory != null) {
            cardQuizHistory.setOnClickListener(v -> AppInfoDialogView.showQuizHistoryDialog(
                    this, topicController.getAllQuizResultsWithDetails(),
                    () -> {
                        topicController.clearQuizHistory();
                        updateDashboardStats();
                    }
            ));
        }
        if (cardProficiency != null) {
            cardProficiency.setOnClickListener(v -> AppInfoDialogView.showProficiencyDialog(
                    this, learningProgressController.getProficiencyReport(topicController.getAllVocabs().size())
            ));
        }
    }

    /**
     * Gán sự kiện click cho các Thẻ chức năng ở Tab 4 Thông tin & Cài đặt.
     */
    private void setupInfoTabCards() {
        View cardProfile = findViewById(R.id.cardProfile);
        View cardOverallProgress = findViewById(R.id.cardOverallProgress);
        View cardSettings = findViewById(R.id.cardSettings);

        if (cardProfile != null) {
            cardProfile.setOnClickListener(v -> AppInfoDialogView.showAuthSyncDialog(
                    this, dailyProgressManager,
                    () -> {
                        GoogleSignInClient gsc = FirebaseSyncController.getInstance().getGoogleSignInClient(this);
                        if (gsc != null) {
                            googleSignInLauncher.launch(gsc.getSignInIntent());
                        }
                    },
                    () -> {
                        updateDashboardStats();
                        if (updateWordListRunnable != null) updateWordListRunnable.run();
                    }
            ));
        }
        if (cardOverallProgress != null) {
            cardOverallProgress.setOnClickListener(v -> AppInfoDialogView.showOverallProgressDialog(
                    this, topicController.getAllTopics(), topicId -> topicController.calculateTopicProgressPercent(topicId)
            ));
        }
        if (cardSettings != null) {
            cardSettings.setOnClickListener(v -> AppInfoDialogView.showSettingsDialog(
                    this, dailyProgressManager.getTtsLocale(), dailyProgressManager.getTtsSpeed(),
                    localeStr -> {
                        dailyProgressManager.setTtsLocale(localeStr);
                        speechController.applyTtsSettings();
                        FirebaseSyncController.getInstance().pushDailySettings(dailyProgressManager);
                    },
                    speed -> {
                        dailyProgressManager.setTtsSpeed(speed);
                        speechController.applyTtsSettings();
                        FirebaseSyncController.getInstance().pushDailySettings(dailyProgressManager);
                    },
                    () -> topicController.clearAllData(dailyProgressManager, () -> {
                        updateDashboardStats();
                        if (updateWordListRunnable != null) updateWordListRunnable.run();
                    })
            ));
        }
    }

    /**
     * Thiết lập danh sách lọc từ vựng bằng ChipGroup và tìm kiếm SearchView.
     */
    private void setupWordListFiltersAndSearch() {
        RecyclerView rvWordList = findViewById(R.id.rvWordList);
        TextView tvEmptyWordList = findViewById(R.id.tvEmptyWordList);
        ChipGroup chipGroupWordList = findViewById(R.id.chipGroupWordList);
        ChipGroup chipGroupSubTopics = findViewById(R.id.chipGroupSubTopics);
        HorizontalScrollView scrollMainFilter = findViewById(R.id.scrollMainFilter);
        HorizontalScrollView scrollSubTopics = findViewById(R.id.scrollSubTopics);
        TextView tvScrollHintMain = findViewById(R.id.tvScrollHintMain);
        TextView tvScrollHintSub = findViewById(R.id.tvScrollHintSub);

        if (rvWordList != null) rvWordList.setLayoutManager(new LinearLayoutManager(this));

        ProgressAnimHelper.setupHorizontalScrollHints(scrollMainFilter, tvScrollHintMain, scrollSubTopics, tvScrollHintSub);

        List<TopicEntity> allTopics = topicController.getAllTopics();

        Runnable populateSubTopics = () -> {
            int mainId = chipGroupWordList != null ? chipGroupWordList.getCheckedChipId() : R.id.chipTopic;
            ProgressAnimHelper.populateSubTopicChips(this, chipGroupSubTopics, allTopics, mainId);
        };

        updateWordListRunnable = () -> {
            int mainId = chipGroupWordList != null ? chipGroupWordList.getCheckedChipId() : R.id.chipTopic;
            TopicController.VocabFilterType filterType = TopicController.VocabFilterType.ALL;
            if (mainId == R.id.chipLearned) filterType = TopicController.VocabFilterType.LEARNED;
            else if (mainId == R.id.chipFavorite) filterType = TopicController.VocabFilterType.FAVORITE;
            else if (mainId == R.id.chipWrong) filterType = TopicController.VocabFilterType.WRONG;
            else if (mainId == R.id.chipAdded) filterType = TopicController.VocabFilterType.CUSTOM;

            int selectedTopicId = -1;
            if (chipGroupSubTopics != null) {
                View checkedSub = chipGroupSubTopics.findViewById(chipGroupSubTopics.getCheckedChipId());
                if (checkedSub != null && checkedSub.getTag() instanceof Integer) {
                    selectedTopicId = (Integer) checkedSub.getTag();
                }
            }

            List<VocabEntity> filtered = topicController.filterVocabs(filterType, selectedTopicId);
            if (tvEmptyWordList != null) {
                tvEmptyWordList.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
            }

            wordListAdapter = new VocabAdapter(filtered,
                    vocab -> speechController.speak(vocab.getWord()),
                    (vocab, isFav) -> topicController.toggleFavorite(vocab.getId(), isFav, () -> {
                        updateDashboardStats();
                        if (updateWordListRunnable != null) updateWordListRunnable.run();
                    })
            );
            wordListAdapter.setOnVocabClickListener(this::showWordDetailDialog);
            if (rvWordList != null) rvWordList.setAdapter(wordListAdapter);
        };

        if (chipGroupWordList != null) {
            chipGroupWordList.setOnCheckedStateChangeListener((group, checkedIds) -> {
                populateSubTopics.run();
                updateWordListRunnable.run();
            });
        }
        if (chipGroupSubTopics != null) {
            chipGroupSubTopics.setOnCheckedStateChangeListener((group, checkedIds) -> updateWordListRunnable.run());
        }
        populateSubTopics.run();
        updateWordListRunnable.run();
    }

    /**
     * Mở BottomSheetDialog tìm kiếm từ vựng.
     */
    private void openSearchDialog() {
        if (activeSearchDialog != null && activeSearchDialog.isShowing()) {
            activeSearchDialog.dismiss();
        }
        activeSearchDialog = VocabDialogView.showSearchVocabDialog(this, "",
                q -> topicController.searchVocabs(q),
                text -> speechController.speak(text),
                (vocab, isFav) -> {
                    topicController.toggleFavorite(vocab.getId(), isFav);
                    updateDashboardStats();
                    if (updateWordListRunnable != null) updateWordListRunnable.run();
                },
                vocab -> showWordDetailDialog(vocab),
                () -> activeSearchDialog = null
        );
    }

    /**
     * Khởi chạy phiên ôn tập ngắt quãng (Spaced Repetition System - SRS).
     */
    private void startSrsReviewSession() {
        List<VocabEntity> srsVocabs = learningProgressController.getDueReviewVocabs();
        if (srsVocabs == null || srsVocabs.isEmpty()) {
            Toast.makeText(this, "Hiện chưa có từ vựng nào đến hạn ôn tập ngắt quãng (SRS)!", Toast.LENGTH_SHORT).show();
            return;
        }
        showPracticeDialogWithController(new PracticeController(null, false, topicController, learningProgressController, dailyProgressManager, srsVocabs), "Ôn tập ngắt quãng (SRS)");
    }

    /**
     * Khởi chạy phiên ôn tập dành riêng cho các từ vựng từng bị làm sai.
     */
    private void startReviewWrongWordsSession() {
        List<VocabEntity> wrongVocabs = topicController.getWrongVocabs();
        if (wrongVocabs == null || wrongVocabs.isEmpty()) {
            Toast.makeText(this, "Bạn chưa có từ vựng nào bị sai! Tuyệt vời!", Toast.LENGTH_SHORT).show();
            return;
        }
        showPracticeDialogWithController(new PracticeController(null, false, topicController, learningProgressController, dailyProgressManager, wrongVocabs), "Ôn từ vựng hay sai");
    }

    /**
     * Hiển thị Dialog xem thông tin chi tiết một từ vựng.
     */
    private void showWordDetailDialog(VocabEntity vocab) {
        VocabDialogView.showWordDetailDialog(this, vocab,
                text -> speechController.speak(text),
                (v, isFav) -> {
                    topicController.toggleFavorite(v.getId(), isFav);
                    updateDashboardStats();
                    if (updateWordListRunnable != null) updateWordListRunnable.run();
                },
                v -> {
                    learningProgressController.resetProgress(v.getId());
                    updateDashboardStats();
                    if (updateWordListRunnable != null) updateWordListRunnable.run();
                },
                v -> topicController.deleteVocab(v, () -> {
                    updateDashboardStats();
                    if (updateWordListRunnable != null) updateWordListRunnable.run();
                })
        );
    }

    /**
     * Hiển thị Dialog thêm từ vựng mới thủ công vào Database.
     */
    private void showAddVocabDialog() {
        VocabDialogView.showAddVocabDialog(this, topicController.getAllTopics(), newVocab -> topicController.addCustomVocab(newVocab, () -> {
            Toast.makeText(this, "Đã thêm từ vựng mới thành công!", Toast.LENGTH_SHORT).show();
            updateDashboardStats();
            if (updateWordListRunnable != null) updateWordListRunnable.run();
        }));
    }

    /**
     * Hiển thị Dialog luyện phát âm bằng microphone STT.
     */
    private void showSpeechPracticeDialog() {
        speechDialogViewHolder = VocabDialogView.showSpeechPracticeDialog(this, topicController.getAllVocabs(),
                text -> speechController.speak(text),
                targetWord -> {
                    Intent intent = speechController.createSpeechRecognitionIntent(targetWord);
                    try {
                        speechLauncher.launch(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Thiết bị chưa hỗ trợ nhận dạng giọng nói Google STT", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Chuyển sang màn hình xem chi tiết chủ đề từ vựng.
     */
    private void showTopicDetailScreen(TopicEntity topic) {
        View detailView = findViewById(R.id.nestedScrollViewTopicDetail);
        ProgressAnimHelper.switchTabWithAnimation(detailView, findViewById(R.id.nestedScrollViewHome), findViewById(R.id.nestedScrollViewTest), findViewById(R.id.nestedScrollViewWordList), findViewById(R.id.nestedScrollViewInfo));

        ImageView imgIcon = findViewById(R.id.imgDetailTopicIcon);
        TextView tvTitle = findViewById(R.id.tvDetailTopicTitle);
        TextView tvSub = findViewById(R.id.tvDetailTopicSub);
        RecyclerView rvDetailVocabs = findViewById(R.id.rvDetailVocabs);
        View btnLearn = findViewById(R.id.btnDetailLearn);
        View btnReview = findViewById(R.id.btnDetailReview);
        View btnBack = findViewById(R.id.btnBackFromDetail);

        if (topic != null) {
            if (imgIcon != null) imgIcon.setImageResource(topic.getIconResId());
            if (tvTitle != null) tvTitle.setText(topic.getTitle());
            List<VocabEntity> vocabs = topicController.getVocabsForTopic(topic.getTitle());
            if (tvSub != null) tvSub.setText(topic.getSubTitle() + " • " + vocabs.size() + " từ vựng");

            TextView tvProgressText = findViewById(R.id.tvDetailProgressText);
            com.google.android.material.progressindicator.LinearProgressIndicator progressIndicator = findViewById(R.id.progressDetailTopic);
            if (progressIndicator != null) progressIndicator.setMax(10000);

            if (currentTopicProgressLiveData != null) currentTopicProgressLiveData.removeObservers(this);
            currentTopicProgressLiveData = topicController.getTopicProgressLiveData(topic.getId());
            currentTopicProgressLiveData.observe(this, tp -> {
                if (tp != null) detailTopicAnimHelper.animateTo(progressIndicator, tvProgressText, tp.getProgressPercent());
            });

            if (rvDetailVocabs != null) {
                rvDetailVocabs.setLayoutManager(new LinearLayoutManager(this));
                VocabAdapter detailAdapter = new VocabAdapter(vocabs, v -> speechController.speak(v.getWord()), (v, isFav) -> {
                    topicController.toggleFavorite(v.getId(), isFav);
                    updateDashboardStats();
                });
                detailAdapter.setOnVocabClickListener(this::showWordDetailDialog);
                rvDetailVocabs.setAdapter(detailAdapter);
            }

            if (btnLearn != null) btnLearn.setOnClickListener(v -> showFlashcardDialog(topic));
            if (btnReview != null) btnReview.setOnClickListener(v -> showPracticeDialog(topic, false));
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> hideTopicDetailScreen());
    }

    /**
     * Quay trở lại màn hình Trang chủ từ màn hình Chi tiết chủ đề.
     */
    private void hideTopicDetailScreen() {
        ProgressAnimHelper.switchTabWithAnimation(findViewById(R.id.nestedScrollViewHome), findViewById(R.id.nestedScrollViewTopicDetail));
    }

    /**
     * Hiển thị BottomSheetDialog học thẻ Flashcard.
     */
    private void showFlashcardDialog(TopicEntity topic) {
        FlashcardController controller = new FlashcardController(topic, topicController, dailyProgressManager);
        FlashcardDialogView.show(this, controller, text -> speechController.speak(text), this::updateDashboardStats);
    }

    /**
     * Hiển thị phiên học trắc nghiệm Quiz theo chủ đề hoặc thi tổng hợp.
     */
    private void showPracticeDialog(TopicEntity topic, boolean isExam) {
        PracticeController controller = new PracticeController(topic, isExam, topicController, learningProgressController, dailyProgressManager);
        showPracticeDialogWithController(controller, isExam ? "Bài kiểm tra" : (topic != null ? "Ôn tập: " + topic.getTitle() : "Ôn tập tổng hợp"));
    }

    /**
     * Hiển thị giao diện bài tập trắc nghiệm bằng PracticeDialogView.
     */
    private void showPracticeDialogWithController(PracticeController controller, String title) {
        PracticeDialogView.show(this, controller, title, text -> speechController.speak(text), this::updateDashboardStats, () -> showPracticeDialogWithController(controller.createRetryController(), title));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboardStats();
    }

    /**
     * Cập nhật toàn bộ các số liệu thống kê Dashboard trên trang chủ.
     */
    private void updateDashboardStats() {
        DashboardStats stats = learningProgressController.getDashboardStats(topicController, dailyProgressManager);

        TextView tvLearned = findViewById(R.id.tvStatLearned);
        TextView tvQuizzes = findViewById(R.id.tvStatQuizzes);
        TextView tvCorrect = findViewById(R.id.tvStatCorrect);
        TextView tvAccuracy = findViewById(R.id.tvStatAccuracy);
        com.google.android.material.progressindicator.CircularProgressIndicator progressDailyGoal = findViewById(R.id.progressDailyGoal);
        TextView tvDailyGoalFraction = findViewById(R.id.tvDailyGoalFraction);
        TextView tvDailyGoalStatus = findViewById(R.id.tvDailyGoalStatus);
        TextView tvWrongWordsCount = findViewById(R.id.tvWrongWordsCount);
        TextView tvCustomWordsCount = findViewById(R.id.tvCustomWordsCount);

        if (tvLearned != null) tvLearned.setText(stats.getLearnedCount() + "\nTừ đã học");
        if (tvQuizzes != null) tvQuizzes.setText(stats.getQuizCompleted() + "\nBài đã làm");
        if (tvCorrect != null) tvCorrect.setText(stats.getTotalCorrectQuiz() + "\nCâu đúng");
        if (tvAccuracy != null) tvAccuracy.setText(stats.getAccuracy() + "%\nChính xác");

        if (tvWrongWordsCount != null) tvWrongWordsCount.setText(String.valueOf(stats.getWrongCount()));
        if (tvCustomWordsCount != null) tvCustomWordsCount.setText(String.valueOf(stats.getCustomCount()));

        if (progressDailyGoal != null) {
            progressDailyGoal.setMax(10000);
            dailyGoalAnimHelper.animateTo(progressDailyGoal, null, stats.getDailyPercent());
        }

        if (tvDailyGoalFraction != null) tvDailyGoalFraction.setText(stats.getTodayCount() + "/" + stats.getDailyGoal());
        if (tvDailyGoalStatus != null) tvDailyGoalStatus.setText(stats.getDailyGoalStatus());
    }

    @Override
    protected void onDestroy() {
        if (activeSearchDialog != null) {
            if (activeSearchDialog.isShowing()) {
                activeSearchDialog.dismiss();
            }
            activeSearchDialog = null;
        }
        if (dailyGoalAnimHelper != null) dailyGoalAnimHelper.cancel();
        if (detailTopicAnimHelper != null) detailTopicAnimHelper.cancel();
        if (speechController != null) speechController.shutdown();
        super.onDestroy();
    }
}
