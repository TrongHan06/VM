package com.example.vocabmaster.view.dialog;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vocabmaster.R;
import com.example.vocabmaster.controller.PracticeController;
import com.example.vocabmaster.model.entity.VocabEntity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

/**
 * Lớp View quản lý giao diện BottomSheetDialog làm bài tập trắc nghiệm và Dialog tổng kết điểm số.
 */
public class PracticeDialogView {

    public interface OnSpeechListener {
        void onSpeak(String text);
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    public interface OnRetryListener {
        void onRetry();
    }

    /**
     * Hiển thị BottomSheetDialog phiên học trắc nghiệm / kiểm tra.
     */
    public static void show(Context context, PracticeController practiceController, String title, OnSpeechListener speechListener, OnDismissListener dismissListener, OnRetryListener retryListener) {
        if (context == null || practiceController == null) return;

        if (!practiceController.hasQuestions()) {
            Toast.makeText(context, "Không có câu hỏi nào phù hợp!", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.layout_practice, null);
        dialog.setContentView(sheetView);

        TextView tvPracticeTopicName = sheetView.findViewById(R.id.tvPracticeTopicName);
        if (tvPracticeTopicName != null) {
            tvPracticeTopicName.setText(title);
        }

        if (dialog.getWindow() != null) {
            View bottomSheetContainer = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetContainer != null) {
                bottomSheetContainer.setBackgroundResource(android.R.color.transparent);
            }
        }

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        boolean isLandscape = context.getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int halfScreenHeight = isLandscape ? (int)(displayMetrics.heightPixels * 0.80) : (dpWidth >= 600 ? (int)(displayMetrics.heightPixels * 0.55) : displayMetrics.heightPixels / 2);
        int twoThirdsScreenHeight = isLandscape ? (int)(displayMetrics.heightPixels * 0.90) : (dpWidth >= 600 ? (int)(displayMetrics.heightPixels * 0.70) : displayMetrics.heightPixels * 2 / 3);

        if (sheetView.getLayoutParams() != null) {
            sheetView.getLayoutParams().height = halfScreenHeight;
        }

        if (sheetView.getParent() != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) sheetView.getParent());
            behavior.setPeekHeight(halfScreenHeight);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        final Runnable[] loadNextQuestion = new Runnable[1];

        loadNextQuestion[0] = () -> {
            if (practiceController.isSessionCompleted()) {
                dialog.dismiss();
                showQuizSummaryDialog(context, practiceController, retryListener);
                if (dismissListener != null) dismissListener.onDismiss();
                return;
            }

            VocabEntity questionVocab = practiceController.getCurrentQuestion();
            final boolean[] isAnswered = {false};

            View cardExplanation = sheetView.findViewById(R.id.cardExplanation);
            View btnNextPractice = sheetView.findViewById(R.id.btnNextPractice);
            com.google.android.material.progressindicator.LinearProgressIndicator progressQuizSession = sheetView.findViewById(R.id.progressQuizSession);

            if (cardExplanation != null) cardExplanation.setVisibility(View.GONE);
            if (btnNextPractice != null) btnNextPractice.setVisibility(View.GONE);

            if (progressQuizSession != null && practiceController.getTotalQuestions() > 0) {
                progressQuizSession.setMax(practiceController.getTotalQuestions());
                progressQuizSession.setProgress(practiceController.getQuestionIndex() + 1);
            }

            if (sheetView.getLayoutParams() != null) {
                sheetView.getLayoutParams().height = halfScreenHeight;
                sheetView.requestLayout();
            }
            if (sheetView.getParent() != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) sheetView.getParent());
                behavior.setPeekHeight(halfScreenHeight);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            TextView tvPracticeWord = sheetView.findViewById(R.id.tvPracticeWord);
            TextView tvPracticePhonetic = sheetView.findViewById(R.id.tvPracticePhonetic);
            if (tvPracticeWord != null) tvPracticeWord.setText(questionVocab.getWord() + " (" + (practiceController.getQuestionIndex() + 1) + "/" + practiceController.getTotalQuestions() + ")");
            if (tvPracticePhonetic != null) tvPracticePhonetic.setText(questionVocab.getPhonetic());

            View btnPracticePronunciation = sheetView.findViewById(R.id.btnPracticePronunciation);
            if (btnPracticePronunciation != null) {
                btnPracticePronunciation.setOnClickListener(v -> {
                    if (speechListener != null) speechListener.onSpeak(questionVocab.getWord());
                });
            }

            List<String> options = practiceController.generateOptionsForCurrentQuestion();
            String correctAnswer = questionVocab.getMeaning();

            TextView tvOptionA = sheetView.findViewById(R.id.tvOptionA);
            TextView tvOptionB = sheetView.findViewById(R.id.tvOptionB);
            TextView tvOptionC = sheetView.findViewById(R.id.tvOptionC);
            TextView tvOptionD = sheetView.findViewById(R.id.tvOptionD);

            TextView tvBadgeOptionA = sheetView.findViewById(R.id.tvBadgeOptionA);
            TextView tvBadgeOptionB = sheetView.findViewById(R.id.tvBadgeOptionB);
            TextView tvBadgeOptionC = sheetView.findViewById(R.id.tvBadgeOptionC);
            TextView tvBadgeOptionD = sheetView.findViewById(R.id.tvBadgeOptionD);

            if (tvOptionA != null && !options.isEmpty()) tvOptionA.setText(options.get(0));
            if (tvOptionB != null && options.size() > 1) tvOptionB.setText(options.get(1));
            if (tvOptionC != null && options.size() > 2) tvOptionC.setText(options.get(2));
            if (tvOptionD != null && options.size() > 3) tvOptionD.setText(options.get(3));

            com.google.android.material.card.MaterialCardView[] optionCards = {
                    sheetView.findViewById(R.id.cardOptionA),
                    sheetView.findViewById(R.id.cardOptionB),
                    sheetView.findViewById(R.id.cardOptionC),
                    sheetView.findViewById(R.id.cardOptionD)
            };

            TextView[] optionBadges = { tvBadgeOptionA, tvBadgeOptionB, tvBadgeOptionC, tvBadgeOptionD };

            for (int c = 0; c < 4; c++) {
                if (optionCards[c] != null) {
                    optionCards[c].setCardBackgroundColor(android.graphics.Color.parseColor("#F8FAFC"));
                    optionCards[c].setStrokeColor(android.graphics.Color.parseColor("#CBD5E1"));
                }
                if (optionBadges[c] != null) {
                    optionBadges[c].setTextColor(android.graphics.Color.parseColor("#1E3A5F"));
                }
            }

            TextView[] optionTexts = { tvOptionA, tvOptionB, tvOptionC, tvOptionD };
            TextView tvExplanationExample = sheetView.findViewById(R.id.tvExplanationExample);
            TextView tvExplanationMeaning = sheetView.findViewById(R.id.tvExplanationMeaning);
            TextView tvExplanationResult = sheetView.findViewById(R.id.tvExplanationResult);

            for (int i = 0; i < 4; i++) {
                final int index = i;
                if (optionCards[i] != null && optionTexts[i] != null) {
                    optionCards[i].setOnClickListener(v -> {
                        if (isAnswered[0]) return;
                        isAnswered[0] = true;

                        String selectedAnswer = optionTexts[index].getText().toString();

                        if (practiceController.checkAnswer(selectedAnswer)) {
                            optionCards[index].setCardBackgroundColor(android.graphics.Color.parseColor("#DCFCE7"));
                            optionCards[index].setStrokeColor(android.graphics.Color.parseColor("#16A34A"));
                            if (optionBadges[index] != null) {
                                optionBadges[index].setTextColor(android.graphics.Color.parseColor("#16A34A"));
                            }
                            if (tvExplanationResult != null) {
                                tvExplanationResult.setText("Chính xác!");
                                tvExplanationResult.setTextColor(android.graphics.Color.parseColor("#16A34A"));
                            }
                        } else {
                            optionCards[index].setCardBackgroundColor(android.graphics.Color.parseColor("#FEE2E2"));
                            optionCards[index].setStrokeColor(android.graphics.Color.parseColor("#DC2626"));
                            if (optionBadges[index] != null) {
                                optionBadges[index].setTextColor(android.graphics.Color.parseColor("#DC2626"));
                            }
                            if (tvExplanationResult != null) {
                                tvExplanationResult.setText("Chưa chính xác!");
                                tvExplanationResult.setTextColor(android.graphics.Color.parseColor("#DC2626"));
                            }
                            for (int j = 0; j < 4; j++) {
                                if (optionTexts[j].getText().toString().equals(correctAnswer)) {
                                    optionCards[j].setCardBackgroundColor(android.graphics.Color.parseColor("#DCFCE7"));
                                    optionCards[j].setStrokeColor(android.graphics.Color.parseColor("#16A34A"));
                                    if (optionBadges[j] != null) {
                                        optionBadges[j].setTextColor(android.graphics.Color.parseColor("#16A34A"));
                                    }
                                    break;
                                }
                            }
                        }

                        if (sheetView.getLayoutParams() != null) {
                            sheetView.getLayoutParams().height = twoThirdsScreenHeight;
                            sheetView.requestLayout();
                        }
                        if (sheetView.getParent() != null) {
                            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) sheetView.getParent());
                            behavior.setPeekHeight(twoThirdsScreenHeight);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }

                        if (tvExplanationExample != null) tvExplanationExample.setText("• Ví dụ: " + questionVocab.getExample());
                        if (tvExplanationMeaning != null) tvExplanationMeaning.setText("• Dịch nghĩa: " + questionVocab.getExampleMeaning());

                        if (cardExplanation != null) cardExplanation.setVisibility(View.VISIBLE);
                        if (btnNextPractice != null) {
                            btnNextPractice.setVisibility(View.VISIBLE);
                            if (btnNextPractice instanceof com.google.android.material.button.MaterialButton) {
                                com.google.android.material.button.MaterialButton btnNext = (com.google.android.material.button.MaterialButton) btnNextPractice;
                                if (practiceController.isLastQuestion()) {
                                    btnNext.setText("Hoàn thành");
                                } else {
                                    btnNext.setText("Câu tiếp theo →");
                                }
                            }
                            btnNextPractice.setOnClickListener(nextV -> {
                                practiceController.advanceToNextQuestion();
                                loadNextQuestion[0].run();
                            });
                        }
                    });
                }
            }
        };

        loadNextQuestion[0].run();

        dialog.setOnDismissListener(dialogInterface -> {
            if (dismissListener != null) dismissListener.onDismiss();
        });

        dialog.show();
    }

    /**
     * Hiển thị BottomSheetDialog tổng kết điểm số sau khi hoàn thành bài Quiz.
     */
    public static void showQuizSummaryDialog(Context context, PracticeController practiceController, OnRetryListener retryListener) {
        if (context == null || practiceController == null || practiceController.getTotalQuestions() == 0) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_quiz_summary, null);
        dialog.setContentView(sheetView);

        if (dialog.getWindow() != null) {
            View bottomSheetContainer = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetContainer != null) {
                bottomSheetContainer.setBackgroundResource(android.R.color.transparent);
            }
        }

        int total = practiceController.getTotalQuestions();
        int correct = practiceController.getCorrectAnswersCount();
        int scorePercent = (correct * 100) / total;

        String feedback;
        if (scorePercent >= 90) {
            feedback = "Xuất sắc! Bạn đã thành thạo bộ từ này!";
        } else if (scorePercent >= 70) {
            feedback = "Rất tốt! Hãy tiếp tục phát huy nhé!";
        } else if (scorePercent >= 50) {
            feedback = "Khá lắm! Cố gắng luyện tập thêm!";
        } else {
            feedback = "Đừng nản lòng! Hãy ôn lại từ sai nhé!";
        }

        com.google.android.material.progressindicator.CircularProgressIndicator progressScore = sheetView.findViewById(R.id.progressQuizSummaryScore);
        TextView tvSummaryScorePercent = sheetView.findViewById(R.id.tvSummaryScorePercent);
        TextView tvSummaryResultBadge = sheetView.findViewById(R.id.tvSummaryResultBadge);
        TextView tvSummaryCorrectCount = sheetView.findViewById(R.id.tvSummaryCorrectCount);
        TextView tvSummaryFeedbackNote = sheetView.findViewById(R.id.tvSummaryFeedbackNote);
        View btnSummaryRetry = sheetView.findViewById(R.id.btnSummaryRetry);
        View btnSummaryClose = sheetView.findViewById(R.id.btnSummaryClose);

        if (progressScore != null) {
            progressScore.setMax(100);
            progressScore.setProgress(scorePercent);
        }
        if (tvSummaryScorePercent != null) tvSummaryScorePercent.setText(scorePercent + "%");
        if (tvSummaryResultBadge != null) tvSummaryResultBadge.setText(feedback);
        if (tvSummaryCorrectCount != null) tvSummaryCorrectCount.setText(correct + " / " + total + " câu");
        if (tvSummaryFeedbackNote != null) tvSummaryFeedbackNote.setText("Đã lưu kết quả");

        if (btnSummaryRetry != null) {
            btnSummaryRetry.setOnClickListener(v -> {
                dialog.dismiss();
                if (retryListener != null) retryListener.onRetry();
            });
        }

        if (btnSummaryClose != null) {
            btnSummaryClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }
}
