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
import java.util.Objects;

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

        View cardExplanation = sheetView.findViewById(R.id.cardExplanation);
        View btnNextPractice = sheetView.findViewById(R.id.btnNextPractice);
        com.google.android.material.progressindicator.LinearProgressIndicator progressQuizSession = sheetView.findViewById(R.id.progressQuizSession);
        TextView tvPracticeWord = sheetView.findViewById(R.id.tvPracticeWord);
        TextView tvPracticePhonetic = sheetView.findViewById(R.id.tvPracticePhonetic);
        View btnPracticePronunciation = sheetView.findViewById(R.id.btnPracticePronunciation);

        com.google.android.material.card.MaterialCardView[] optionCards = {
                sheetView.findViewById(R.id.cardOptionA),
                sheetView.findViewById(R.id.cardOptionB),
                sheetView.findViewById(R.id.cardOptionC),
                sheetView.findViewById(R.id.cardOptionD)
        };
        TextView[] optionTexts = {
                sheetView.findViewById(R.id.tvOptionA),
                sheetView.findViewById(R.id.tvOptionB),
                sheetView.findViewById(R.id.tvOptionC),
                sheetView.findViewById(R.id.tvOptionD)
        };
        TextView[] optionBadges = {
                sheetView.findViewById(R.id.tvBadgeOptionA),
                sheetView.findViewById(R.id.tvBadgeOptionB),
                sheetView.findViewById(R.id.tvBadgeOptionC),
                sheetView.findViewById(R.id.tvBadgeOptionD)
        };

        TextView tvExplanationExample = sheetView.findViewById(R.id.tvExplanationExample);
        TextView tvExplanationMeaning = sheetView.findViewById(R.id.tvExplanationMeaning);
        TextView tvExplanationResult = sheetView.findViewById(R.id.tvExplanationResult);

        if (btnNextPractice != null) {
            btnNextPractice.setOnClickListener(nextV -> {
                if (practiceController.isSessionCompleted()) return;
                practiceController.advanceToNextQuestion();
                loadNextQuestion[0].run();
            });
        }

        loadNextQuestion[0] = () -> {
            if (practiceController.isSessionCompleted()) {
                dialog.dismiss();
                showQuizSummaryDialog(context, practiceController, retryListener);
                return;
            }

            VocabEntity questionVocab = practiceController.getCurrentQuestion();
            if (questionVocab == null) {
                dialog.dismiss();
                return;
            }
            final boolean[] isAnswered = {false};

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

            String wordStr = questionVocab.getWord() != null ? questionVocab.getWord() : "";
            String phoneticStr = questionVocab.getPhonetic() != null ? questionVocab.getPhonetic() : "";
            if (tvPracticeWord != null) {
                tvPracticeWord.setText(wordStr + " (" + (practiceController.getQuestionIndex() + 1) + "/" + practiceController.getTotalQuestions() + ")");
            }
            if (tvPracticePhonetic != null) tvPracticePhonetic.setText(phoneticStr);

            if (btnPracticePronunciation != null) {
                btnPracticePronunciation.setOnClickListener(v -> {
                    if (speechListener != null && !wordStr.isEmpty()) speechListener.onSpeak(wordStr);
                });
            }

            List<String> options = practiceController.generateOptionsForCurrentQuestion();
            String correctAnswer = questionVocab.getMeaning();

            for (int i = 0; i < 4; i++) {
                if (optionCards[i] != null) {
                    optionCards[i].setCardBackgroundColor(android.graphics.Color.parseColor("#F8FAFC"));
                    optionCards[i].setStrokeColor(android.graphics.Color.parseColor("#CBD5E1"));
                }
                if (optionBadges[i] != null) {
                    optionBadges[i].setTextColor(android.graphics.Color.parseColor("#1E3A5F"));
                }

                if (i < options.size()) {
                    if (optionCards[i] != null) {
                        optionCards[i].setVisibility(View.VISIBLE);
                    }
                    if (optionTexts[i] != null) {
                        optionTexts[i].setText(options.get(i));
                    }
                    if (optionCards[i] != null && optionTexts[i] != null) {
                        final int index = i;
                        optionCards[i].setOnClickListener(v -> {
                            if (isAnswered[0]) return;
                            isAnswered[0] = true;

                            CharSequence textSeq = optionTexts[index].getText();
                            String selectedAnswer = textSeq != null ? textSeq.toString() : "";

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
                                    if (optionCards[j] != null
                                            && optionCards[j].getVisibility() == View.VISIBLE
                                            && optionTexts[j] != null
                                            && optionTexts[j].getText() != null
                                            && Objects.equals(optionTexts[j].getText().toString(), correctAnswer)) {
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

                            if (tvExplanationExample != null) {
                                String exEn = questionVocab.getExample();
                                if (exEn != null && !exEn.trim().isEmpty()) {
                                    tvExplanationExample.setVisibility(View.VISIBLE);
                                    tvExplanationExample.setText("• Ví dụ: " + exEn.trim());
                                } else {
                                    tvExplanationExample.setText("");
                                    tvExplanationExample.setVisibility(View.GONE);
                                }
                            }
                            if (tvExplanationMeaning != null) {
                                String exVi = questionVocab.getExampleMeaning();
                                if (exVi != null && !exVi.trim().isEmpty()) {
                                    tvExplanationMeaning.setVisibility(View.VISIBLE);
                                    tvExplanationMeaning.setText("• Dịch nghĩa: " + exVi.trim());
                                } else {
                                    tvExplanationMeaning.setText("");
                                    tvExplanationMeaning.setVisibility(View.GONE);
                                }
                            }

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
                            }
                        });
                    } else if (optionCards[i] != null) {
                        optionCards[i].setOnClickListener(null);
                    }
                } else {
                    if (optionTexts[i] != null) {
                        optionTexts[i].setText("");
                    }
                    if (optionCards[i] != null) {
                        optionCards[i].setVisibility(View.GONE);
                        optionCards[i].setOnClickListener(null);
                    }
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
