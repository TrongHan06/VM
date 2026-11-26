package com.example.vocabmaster.view.dialog;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.vocabmaster.R;
import com.example.vocabmaster.controller.FlashcardController;

import com.example.vocabmaster.model.entity.TopicEntity;
import com.example.vocabmaster.model.entity.VocabEntity;
import com.example.vocabmaster.view.helper.ProgressAnimHelper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class FlashcardDialogView {

    public interface OnSpeechListener {
        void onSpeak(String text);
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    public static void show(Context context, FlashcardController flashcardController, OnSpeechListener speechListener, OnDismissListener dismissListener) {
        if (context == null || flashcardController == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.layout_flashcard, null);
        dialog.setContentView(sheetView);

        TopicEntity topic = flashcardController.getTopic();
        TextView tvFlashcardTopicName = sheetView.findViewById(R.id.tvFlashcardTopicName);
        if (tvFlashcardTopicName != null && topic != null) {
            tvFlashcardTopicName.setText("Chủ đề: " + topic.getTitle());
        }

        TextView tvWord = sheetView.findViewById(R.id.tvWord);
        TextView tvPhonetic = sheetView.findViewById(R.id.tvPhonetic);
        TextView tvMeaning = sheetView.findViewById(R.id.tvMeaning);
        TextView tvExample = sheetView.findViewById(R.id.tvExample);
        TextView tvExampleMeaning = sheetView.findViewById(R.id.tvExampleMeaning);
        TextView tvFlashcardCounter = sheetView.findViewById(R.id.tvFlashcardCounter);
        TextView tvFlashcardMetaBadge = sheetView.findViewById(R.id.tvFlashcardMetaBadge);
        com.google.android.material.progressindicator.LinearProgressIndicator progressFlashcard = sheetView.findViewById(R.id.progressFlashcard);

        View flashcard = sheetView.findViewById(R.id.flashcard);
        View cardFront = sheetView.findViewById(R.id.cardFront);
        View cardBack = sheetView.findViewById(R.id.cardBack);
        View btnPronunciation = sheetView.findViewById(R.id.btnPronunciation);
        View btnSpeakExample = sheetView.findViewById(R.id.btnSpeakExample);
        View btnPreviousWord = sheetView.findViewById(R.id.btnPreviousWord);
        View btnNextWord = sheetView.findViewById(R.id.btnNextWord);

        final boolean[] isFlipped = {false};

        Runnable updateCardData = () -> {
            VocabEntity vocab = flashcardController.getCurrentVocab();
            if (vocab == null) return;
            if (tvWord != null) tvWord.setText(vocab.getWord() != null ? vocab.getWord() : "");
            if (tvPhonetic != null) tvPhonetic.setText(vocab.getPhonetic() != null ? vocab.getPhonetic() : "");
            if (tvMeaning != null) tvMeaning.setText(vocab.getMeaning() != null ? vocab.getMeaning() : "");
            if (tvExample != null) {
                String exEn = vocab.getExample();
                if (exEn != null && !exEn.trim().isEmpty()) {
                    tvExample.setVisibility(View.VISIBLE);
                    tvExample.setText(exEn.trim());
                } else {
                    tvExample.setText("");
                    tvExample.setVisibility(View.GONE);
                }
            }
            if (tvExampleMeaning != null) {
                String exMeaning = vocab.getExampleMeaning();
                if (exMeaning != null && !exMeaning.trim().isEmpty()) {
                    tvExampleMeaning.setVisibility(View.VISIBLE);
                    tvExampleMeaning.setText("(" + exMeaning.trim() + ")");
                } else {
                    tvExampleMeaning.setText("");
                    tvExampleMeaning.setVisibility(View.GONE);
                }
            }

            int total = flashcardController.getTotalCount();
            int currentPos = flashcardController.getCurrentIndex() + 1;
            if (tvFlashcardCounter != null) {
                tvFlashcardCounter.setText("Thẻ " + currentPos + " / " + total);
            }
            if (progressFlashcard != null && total > 0) {
                progressFlashcard.setMax(total);
                progressFlashcard.setProgress(currentPos);
            }
            if (tvFlashcardMetaBadge != null) {
                String meta = (vocab.getWordType() != null ? vocab.getWordType() : "từ") + " • " + (vocab.getLevel() != null ? vocab.getLevel() : "A1");
                tvFlashcardMetaBadge.setText(meta);
            }

            if (btnNextWord instanceof com.google.android.material.button.MaterialButton) {
                if (flashcardController.isLastVocab()) {
                    ((com.google.android.material.button.MaterialButton) btnNextWord).setText("Hoàn thành");
                } else {
                    ((com.google.android.material.button.MaterialButton) btnNextWord).setText("Từ tiếp theo →");
                }
            }

            if (btnPreviousWord != null) {
                boolean hasPrev = !flashcardController.isFirstVocab();
                btnPreviousWord.setEnabled(hasPrev);
                btnPreviousWord.setAlpha(hasPrev ? 1f : 0.5f);
            }

            isFlipped[0] = false;
            if (cardFront != null && cardBack != null) {
                cardBack.setVisibility(View.GONE);
                cardFront.setVisibility(View.VISIBLE);
                cardFront.setRotationY(0);
                cardBack.setRotationY(180);
            }
        };

        final boolean[] isAnimating = {false};
        updateCardData.run();

        if (flashcard != null && cardFront != null && cardBack != null) {
            float distance = 8000 * context.getResources().getDisplayMetrics().density;
            cardFront.setCameraDistance(distance);
            cardBack.setCameraDistance(distance);

            flashcard.setOnClickListener(v -> {
                if (isAnimating[0]) return;
                isAnimating[0] = true;
                flashcardController.markCurrentAsLearned();
                ProgressAnimHelper.flipCard3D(cardFront, cardBack, isFlipped, () -> isAnimating[0] = false);
            });
        }

        if (btnNextWord != null) {
            btnNextWord.setOnClickListener(v -> {
                if (isAnimating[0]) return;
                isAnimating[0] = true;
                if (flashcardController.nextVocab()) {
                    ProgressAnimHelper.slideCard(flashcard, updateCardData, true, () -> isAnimating[0] = false);
                } else {
                    isAnimating[0] = false;
                    dialog.dismiss();
                }
            });
        }

        if (btnPreviousWord != null) {
            btnPreviousWord.setOnClickListener(v -> {
                if (isAnimating[0]) return;
                isAnimating[0] = true;
                if (flashcardController.previousVocab()) {
                    ProgressAnimHelper.slideCard(flashcard, updateCardData, false, () -> isAnimating[0] = false);
                } else {
                    isAnimating[0] = false;
                }
            });
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
        int targetHeight = isLandscape ? (int)(displayMetrics.heightPixels * 0.85) : (dpWidth >= 600 ? (int)(displayMetrics.heightPixels * 0.55) : displayMetrics.heightPixels / 2);

        if (sheetView.getLayoutParams() != null) {
            sheetView.getLayoutParams().height = targetHeight;
        }

        if (sheetView.getParent() != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) sheetView.getParent());
            behavior.setPeekHeight(targetHeight);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        if (btnPronunciation != null) {
            btnPronunciation.setOnClickListener(v -> {
                VocabEntity vocab = flashcardController.getCurrentVocab();
                if (vocab != null && speechListener != null) {
                    speechListener.onSpeak(vocab.getWord());
                }
            });
        }

        if (btnSpeakExample != null) {
            btnSpeakExample.setOnClickListener(v -> {
                VocabEntity vocab = flashcardController.getCurrentVocab();
                if (vocab != null && vocab.getExample() != null && !vocab.getExample().trim().isEmpty() && speechListener != null) {
                    speechListener.onSpeak(vocab.getExample());
                }
            });
        }

        dialog.setOnDismissListener(dialogInterface -> {
            if (dismissListener != null) dismissListener.onDismiss();
        });

        dialog.show();
    }
}
