package com.example.vocabmaster.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabmaster.R;
import com.example.vocabmaster.controller.DailyProgressManager;
import com.example.vocabmaster.controller.FirebaseSyncController;
import com.example.vocabmaster.model.entity.ProficiencyReport;
import com.example.vocabmaster.model.entity.QuizResultWithQuiz;
import com.example.vocabmaster.model.entity.TopicEntity;
import com.example.vocabmaster.model.entity.VocabEntity;
import com.example.vocabmaster.view.adapter.OverallProgressAdapter;
import com.example.vocabmaster.view.adapter.QuizHistoryAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Locale;

/**
 * Lớp View quản lý các BottomSheetDialog thông tin và cài đặt hệ thống.
 * Bao gồm: Lịch sử làm bài, Đánh giá năng lực, Hồ sơ học viên, Tiến độ tổng quan, Cài đặt & Khác, Cài đặt nhắc nhở, Đồng bộ Cloud.
 */
public class AppInfoDialogView {

    public interface OnClearHistoryListener {
        void onClearHistory();
    }

    public interface OnSaveDailyGoalListener {
        void onSaveGoal(int newGoal);
    }

    public interface OnSaveReminderListener {
        void onSaveReminder(int hour, int minute, boolean enabled);
    }

    public interface OnTestReminderListener {
        void onTestReminder();
    }

    public interface OnSelectTtsVoiceListener {
        void onSelectVoice(String localeStr);
    }

    public interface OnResetDataListener {
        void onResetData();
    }

    public interface OnSyncCompletedListener {
        void onSyncCompleted();
    }

    public interface OnGoogleSignInListener {
        void onGoogleSignIn();
    }

    public interface TopicProgressCalculator {
        int calculatePercent(int topicId);
    }

    private static void setTransparentBackground(BottomSheetDialog dialog) {
        if (dialog != null && dialog.getWindow() != null) {
            View bottomSheetContainer = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetContainer != null) {
                bottomSheetContainer.setBackgroundResource(android.R.color.transparent);
            }
        }
    }

    public static void showAuthSyncDialog(Context context, DailyProgressManager manager, OnSyncCompletedListener syncListener) {
        showAuthSyncDialog(context, manager, null, syncListener);
    }

    /**
     * Hiển thị BottomSheetDialog Đăng nhập / Đăng ký và Đồng bộ Cloud Firestore.
     */
    public static void showAuthSyncDialog(Context context, DailyProgressManager manager,
                                           OnGoogleSignInListener googleSignInListener,
                                           OnSyncCompletedListener syncListener) {
        if (context == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_auth_sync, null);
        dialog.setContentView(sheetView);
        setTransparentBackground(dialog);

        FirebaseSyncController syncController = FirebaseSyncController.getInstance();

        View layoutLoggedOut = sheetView.findViewById(R.id.layoutAuthLoggedOut);
        View layoutLoggedIn = sheetView.findViewById(R.id.layoutAuthLoggedIn);
        ChipGroup chipGroupMode = sheetView.findViewById(R.id.chipGroupAuthMode);
        com.google.android.material.textfield.TextInputEditText etEmail = sheetView.findViewById(R.id.etAuthEmail);
        com.google.android.material.textfield.TextInputEditText etPassword = sheetView.findViewById(R.id.etAuthPassword);
        com.google.android.material.button.MaterialButton btnSubmit = sheetView.findViewById(R.id.btnAuthSubmit);
        View btnAuthGoogle = sheetView.findViewById(R.id.btnAuthGoogle);

        TextView tvUserEmail = sheetView.findViewById(R.id.tvAuthUserEmail);
        View btnSyncNow = sheetView.findViewById(R.id.btnSyncNow);
        View btnSignOut = sheetView.findViewById(R.id.btnAuthSignOut);

        Runnable updateState = () -> {
            if (syncController.isLoggedIn()) {
                if (layoutLoggedOut != null) layoutLoggedOut.setVisibility(View.GONE);
                if (layoutLoggedIn != null) layoutLoggedIn.setVisibility(View.VISIBLE);
                if (tvUserEmail != null && syncController.getCurrentUser() != null) {
                    tvUserEmail.setText(syncController.getCurrentUser().getEmail());
                }
            } else {
                if (layoutLoggedOut != null) layoutLoggedOut.setVisibility(View.VISIBLE);
                if (layoutLoggedIn != null) layoutLoggedIn.setVisibility(View.GONE);
            }
        };

        updateState.run();

        if (chipGroupMode != null && btnSubmit != null) {
            chipGroupMode.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty() && checkedIds.get(0) == R.id.chipModeSignUp) {
                    btnSubmit.setText("Đăng ký tài khoản");
                } else {
                    btnSubmit.setText("Đăng nhập");
                }
            });
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String email = etEmail != null && etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                String password = etPassword != null && etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

                boolean isSignUp = chipGroupMode != null && chipGroupMode.getCheckedChipId() == R.id.chipModeSignUp;

                FirebaseSyncController.AuthCallback authCallback = new FirebaseSyncController.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        Toast.makeText(context, (isSignUp ? "Đăng ký thành công!" : "Đăng nhập thành công!"), Toast.LENGTH_SHORT).show();
                        updateState.run();
                        syncController.syncAllFromCloud(context, manager, new FirebaseSyncController.SyncCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(context, "Đã đồng bộ dữ liệu từ Cloud!", Toast.LENGTH_SHORT).show();
                                if (syncListener != null) syncListener.onSyncCompleted();
                            }
                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(context, "Đồng bộ thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(context, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                };

                if (isSignUp) {
                    syncController.signUp(email, password, authCallback);
                } else {
                    syncController.signIn(email, password, authCallback);
                }
            });
        }

        if (btnAuthGoogle != null) {
            btnAuthGoogle.setOnClickListener(v -> {
                if (googleSignInListener != null) {
                    googleSignInListener.onGoogleSignIn();
                }
                dialog.dismiss();
            });
        }

        if (btnSyncNow != null) {
            btnSyncNow.setOnClickListener(v -> {
                Toast.makeText(context, "Đang đồng bộ dữ liệu...", Toast.LENGTH_SHORT).show();
                syncController.syncAllFromCloud(context, manager, new FirebaseSyncController.SyncCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, "Đã đồng bộ dữ liệu Cloud & Room thành công!", Toast.LENGTH_SHORT).show();
                        if (syncListener != null) syncListener.onSyncCompleted();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(context, "Đồng bộ thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        if (btnSignOut != null) {
            btnSignOut.setOnClickListener(v -> {
                syncController.signOut(context);
                Toast.makeText(context, "Đã đăng xuất tài khoản!", Toast.LENGTH_SHORT).show();
                updateState.run();
            });
        }

        dialog.show();
    }

    /**
     * Hiển thị BottomSheetDialog danh sách Lịch sử làm bài kiểm tra.
     */
    public static void showQuizHistoryDialog(Context context, List<QuizResultWithQuiz> results, OnClearHistoryListener clearListener) {
        if (context == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_quiz_history, null);
        dialog.setContentView(sheetView);
        setTransparentBackground(dialog);

        RecyclerView rvQuizHistory = sheetView.findViewById(R.id.rvQuizHistory);
        TextView tvEmptyHistory = sheetView.findViewById(R.id.tvEmptyHistory);
        TextView tvClearHistory = sheetView.findViewById(R.id.tvClearHistory);

        if (results == null || results.isEmpty()) {
            if (tvEmptyHistory != null) tvEmptyHistory.setVisibility(View.VISIBLE);
            if (rvQuizHistory != null) rvQuizHistory.setVisibility(View.GONE);
        } else {
            if (tvEmptyHistory != null) tvEmptyHistory.setVisibility(View.GONE);
            if (rvQuizHistory != null) {
                rvQuizHistory.setVisibility(View.VISIBLE);
                rvQuizHistory.setLayoutManager(new LinearLayoutManager(context));
                rvQuizHistory.setAdapter(new QuizHistoryAdapter(results));
            }
        }

        if (tvClearHistory != null) {
            tvClearHistory.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Xóa lịch sử làm bài")
                        .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử bài kiểm tra?")
                        .setPositiveButton("Xóa", (dialogInterface, i) -> {
                            if (clearListener != null) clearListener.onClearHistory();
                            dialog.dismiss();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        dialog.show();
    }

    /**
     * Hiển thị BottomSheetDialog Đánh giá năng lực học viên với thẻ huy hiệu trình độ và KPI nhỏ.
     */
    public static void showProficiencyDialog(Context context, ProficiencyReport report) {
        if (context == null || report == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_proficiency, null);
        dialog.setContentView(sheetView);
        setTransparentBackground(dialog);

        TextView tvProficiencyLevelTitle = sheetView.findViewById(R.id.tvProficiencyLevelTitle);
        TextView tvProficiencyAccuracyPercent = sheetView.findViewById(R.id.tvProficiencyAccuracyPercent);
        TextView tvProficiencyMasteredCount = sheetView.findViewById(R.id.tvProficiencyMasteredCount);
        TextView tvProficiencyAdviceText = sheetView.findViewById(R.id.tvProficiencyAdviceText);
        View btnProficiencyClose = sheetView.findViewById(R.id.btnProficiencyClose);

        if (tvProficiencyLevelTitle != null) tvProficiencyLevelTitle.setText(report.getLevel());
        if (tvProficiencyAccuracyPercent != null) tvProficiencyAccuracyPercent.setText(report.getAccuracy() + "%");
        if (tvProficiencyMasteredCount != null) tvProficiencyMasteredCount.setText(report.getMasteredCount() + " / " + report.getTotalVocabs());
        if (tvProficiencyAdviceText != null) tvProficiencyAdviceText.setText(report.getFeedbackMessage());

        if (btnProficiencyClose != null) {
            btnProficiencyClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    /**
     * Hiển thị BottomSheetDialog Chỉnh sửa mục tiêu số từ vựng cần học hằng ngày.
     */
    public static void showEditDailyGoalDialog(Context context, int currentGoal, OnSaveDailyGoalListener saveListener) {
        if (context == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_daily_goal, null);
        dialog.setContentView(sheetView);
        setTransparentBackground(dialog);

        ChipGroup chipGroupGoal = sheetView.findViewById(R.id.chipGroupGoal);
        View btnSaveDailyGoal = sheetView.findViewById(R.id.btnSaveDailyGoal);

        if (chipGroupGoal != null) {
            if (currentGoal <= 5) chipGroupGoal.check(R.id.chipGoal5);
            else if (currentGoal <= 10) chipGroupGoal.check(R.id.chipGoal10);
            else if (currentGoal <= 15) chipGroupGoal.check(R.id.chipGoal15);
            else if (currentGoal <= 20) chipGroupGoal.check(R.id.chipGoal20);
            else chipGroupGoal.check(R.id.chipGoal30);
        }

        if (btnSaveDailyGoal != null) {
            btnSaveDailyGoal.setOnClickListener(v -> {
                int newGoal = 10;
                if (chipGroupGoal != null) {
                    int checkedId = chipGroupGoal.getCheckedChipId();
                    if (checkedId == R.id.chipGoal5) newGoal = 5;
                    else if (checkedId == R.id.chipGoal10) newGoal = 10;
                    else if (checkedId == R.id.chipGoal15) newGoal = 15;
                    else if (checkedId == R.id.chipGoal20) newGoal = 20;
                    else if (checkedId == R.id.chipGoal30) newGoal = 30;
                }
                if (saveListener != null) saveListener.onSaveGoal(newGoal);
                Toast.makeText(context, "Đã cập nhật mục tiêu mới: " + newGoal + " từ / ngày", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    /**
     * Hiển thị BottomSheetDialog Cài đặt thời gian nhắc nhở hằng ngày (bằng TimePicker và MaterialSwitch).
     */
    public static void showReminderDialog(Context context, int currentHour, int currentMinute, boolean currentEnabled,
                                           OnSaveReminderListener saveListener, OnTestReminderListener testListener) {
        if (context == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_reminder, null);
        dialog.setContentView(sheetView);
        setTransparentBackground(dialog);

        TimePicker timePicker = sheetView.findViewById(R.id.timePicker);
        MaterialSwitch switchReminder = sheetView.findViewById(R.id.switchReminder);
        View btnSaveReminder = sheetView.findViewById(R.id.btnSaveReminder);
        View btnTestReminder = sheetView.findViewById(R.id.btnTestReminder);

        if (timePicker != null) {
            timePicker.setIs24HourView(true);
            timePicker.setHour(currentHour);
            timePicker.setMinute(currentMinute);
        }
        if (switchReminder != null) {
            switchReminder.setChecked(currentEnabled);
        }

        if (btnSaveReminder != null) {
            btnSaveReminder.setOnClickListener(v -> {
                int hour = timePicker != null ? timePicker.getHour() : 20;
                int minute = timePicker != null ? timePicker.getMinute() : 0;
                boolean enabled = switchReminder != null && switchReminder.isChecked();
                if (saveListener != null) {
                    saveListener.onSaveReminder(hour, minute, enabled);
                }

                String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                if (enabled) {
                    Toast.makeText(context, "Đã cài đặt nhắc nhở học từ vựng lúc " + timeStr + " mỗi ngày!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Đã tắt nhắc nhở hằng ngày!", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        }

        if (btnTestReminder != null) {
            btnTestReminder.setOnClickListener(v -> {
                if (testListener != null) testListener.onTestReminder();
                Toast.makeText(context, "Đã gửi thông báo thử nghiệm!", Toast.LENGTH_SHORT).show();
            });
        }

        dialog.show();
    }

    /**
     * Hiển thị BottomSheetDialog Hồ sơ cá nhân dạng Card Profile với lưới 2x2 các thẻ chỉ số tích lũy.
     */
    public static void showProfileDialog(Context context, List<VocabEntity> allVocabs, int accuracy, int dailyGoal, int quizCompleted) {
        if (context == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_profile, null);
        dialog.setContentView(sheetView);
        setTransparentBackground(dialog);

        int learned = 0;
        int total = allVocabs != null ? allVocabs.size() : 0;
        if (allVocabs != null) {
            for (VocabEntity v : allVocabs) {
                if (v.isLearned()) learned++;
            }
        }

        TextView tvProfileLearnedCount = sheetView.findViewById(R.id.tvProfileLearnedCount);
        TextView tvProfileAccuracyPercent = sheetView.findViewById(R.id.tvProfileAccuracyPercent);
        TextView tvProfileDailyGoal = sheetView.findViewById(R.id.tvProfileDailyGoal);
        TextView tvProfileQuizzesCount = sheetView.findViewById(R.id.tvProfileQuizzesCount);
        View btnProfileClose = sheetView.findViewById(R.id.btnProfileClose);

        if (tvProfileLearnedCount != null) tvProfileLearnedCount.setText(learned + " / " + total + " từ");
        if (tvProfileAccuracyPercent != null) tvProfileAccuracyPercent.setText(accuracy + "%");
        if (tvProfileDailyGoal != null) tvProfileDailyGoal.setText(dailyGoal + " từ / ngày");
        if (tvProfileQuizzesCount != null) tvProfileQuizzesCount.setText(quizCompleted + " bài");

        if (btnProfileClose != null) {
            btnProfileClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    /**
     * Hiển thị BottomSheetDialog Tiến độ tổng quan theo từng chủ đề trực quan bằng RecyclerView.
     */
    public static void showOverallProgressDialog(Context context, List<TopicEntity> topics, TopicProgressCalculator calculator) {
        if (context == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_overall_progress, null);
        dialog.setContentView(sheetView);
        setTransparentBackground(dialog);

        RecyclerView rvOverallProgressList = sheetView.findViewById(R.id.rvOverallProgressList);
        View btnOverallProgressClose = sheetView.findViewById(R.id.btnOverallProgressClose);

        if (rvOverallProgressList != null && topics != null) {
            rvOverallProgressList.setLayoutManager(new LinearLayoutManager(context));
            rvOverallProgressList.setAdapter(new OverallProgressAdapter(topics, calculator));
        }

        if (btnOverallProgressClose != null) {
            btnOverallProgressClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    /**
     * Hiển thị BottomSheetDialog Cài đặt & Khác với lựa chọn ChipGroup cho giọng đọc TTS và tốc độ phát âm.
     */
    public static void showSettingsDialog(Context context, String currentLocale, float currentSpeed,
                                           OnSelectTtsVoiceListener ttsListener, OnResetDataListener resetListener) {
        if (context == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_settings, null);
        dialog.setContentView(sheetView);
        setTransparentBackground(dialog);

        ChipGroup chipGroupTtsVoice = sheetView.findViewById(R.id.chipGroupTtsVoice);
        ChipGroup chipGroupTtsSpeed = sheetView.findViewById(R.id.chipGroupTtsSpeed);
        Chip chipVoiceUs = sheetView.findViewById(R.id.chipVoiceUs);
        Chip chipVoiceUk = sheetView.findViewById(R.id.chipVoiceUk);
        View btnResetData = sheetView.findViewById(R.id.btnResetData);
        View btnAppAbout = sheetView.findViewById(R.id.btnAppAbout);
        View btnSettingsClose = sheetView.findViewById(R.id.btnSettingsClose);

        if (chipGroupTtsVoice != null) {
            if ("UK".equalsIgnoreCase(currentLocale) && chipVoiceUk != null) {
                chipVoiceUk.setChecked(true);
            } else if (chipVoiceUs != null) {
                chipVoiceUs.setChecked(true);
            }
            chipGroupTtsVoice.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    int id = checkedIds.get(0);
                    String selectedLocale = (id == R.id.chipVoiceUk) ? "UK" : "US";
                    if (ttsListener != null) ttsListener.onSelectVoice(selectedLocale);
                }
            });
        }

        if (chipGroupTtsSpeed != null) {
            if (currentSpeed <= 0.8f) chipGroupTtsSpeed.check(R.id.chipSpeed075);
            else if (currentSpeed >= 1.2f) chipGroupTtsSpeed.check(R.id.chipSpeed125);
            else chipGroupTtsSpeed.check(R.id.chipSpeed100);
        }

        if (btnResetData != null) {
            btnResetData.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Đặt lại toàn bộ tiến độ")
                        .setMessage("Bạn có chắc chắn muốn xóa toàn bộ tiến độ học và kết quả kiểm tra?")
                        .setPositiveButton("Xóa toàn bộ", (d, w) -> {
                            if (resetListener != null) resetListener.onResetData();
                            dialog.dismiss();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        if (btnAppAbout != null) {
            btnAppAbout.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("VocabMaster v1.0")
                        .setMessage("Ứng dụng Học Từ Vựng Tiếng Anh theo thuật toán Lặp lại ngắt quãng (Spaced Repetition System - SRS) & Mô hình MVC chuẩn.")
                        .setPositiveButton("Đóng", null)
                        .show();
            });
        }

        if (btnSettingsClose != null) {
            btnSettingsClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }
}
