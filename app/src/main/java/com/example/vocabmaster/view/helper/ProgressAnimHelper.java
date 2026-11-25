package com.example.vocabmaster.view.helper;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.vocabmaster.R;
import com.example.vocabmaster.model.entity.TopicEntity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

/**
 * Lớp Helper xử lý các hiệu ứng chuyển động (Animation) và hỗ trợ giao diện người dùng (UI Helper).
 */
public class ProgressAnimHelper {

    private ObjectAnimator animator;
    private int currentProgress = 0;

    /**
     * Chạy hiệu ứng animation tăng thanh tiến trình tròn/ngang ProgressBar trong 500ms và cập nhật TextView %.
     */
    public void animateTo(ProgressBar progressBar, TextView tvPercent, int targetPercent) {
        if (progressBar == null) return;

        int targetValue = targetPercent * 100;
        int startValue = currentProgress;

        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        animator = ObjectAnimator.ofInt(progressBar, "progress", startValue, targetValue);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            currentProgress = animatedValue;
            if (tvPercent != null) {
                int displayPercent = animatedValue / 100;
                tvPercent.setText(displayPercent + "%");
            }
        });

        animator.start();
    }

    /**
     * Chạy hiệu ứng animation tăng LinearProgressIndicator Material 3 trong 500ms và cập nhật TextView %.
     */
    public void animateTo(LinearProgressIndicator progressIndicator, TextView tvPercent, int targetPercent) {
        if (progressIndicator == null) return;

        int targetValue = targetPercent * 100;
        int startValue = currentProgress;

        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        animator = ObjectAnimator.ofInt(progressIndicator, "progress", startValue, targetValue);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            currentProgress = animatedValue;
            if (tvPercent != null) {
                int displayPercent = animatedValue / 100;
                tvPercent.setText(displayPercent + "%");
            }
        });

        animator.start();
    }

    /**
     * Chuyển đổi tab giao diện mượt mà với hiệu ứng Fade-in + Slide-up (200ms).
     */
    public static void switchTabWithAnimation(View targetView, View... otherViews) {
        for (View v : otherViews) {
            if (v != null) v.setVisibility(View.GONE);
        }
        if (targetView != null) {
            targetView.setVisibility(View.VISIBLE);
            targetView.setAlpha(0f);
            targetView.setTranslationY(24f);
            targetView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    /**
     * Hiệu ứng trượt thẻ Flashcard ngang (Slide Left/Right) khi bấm từ tiếp theo hoặc lùi lại.
     */
    public static void slideCard(View flashcard, Runnable updateDataRunnable, boolean isNext) {
        if (flashcard == null) {
            updateDataRunnable.run();
            return;
        }
        float screenWidth = flashcard.getWidth() > 0 ? flashcard.getWidth() : 600f;
        float outX = isNext ? -screenWidth : screenWidth;
        float inX = isNext ? screenWidth : -screenWidth;

        flashcard.animate()
                .translationX(outX)
                .alpha(0f)
                .setDuration(160)
                .withEndAction(() -> {
                    updateDataRunnable.run();
                    flashcard.setTranslationX(inX);
                    flashcard.animate()
                            .translationX(0f)
                            .alpha(1f)
                            .setDuration(160)
                            .start();
                })
                .start();
    }

    /**
     * Hiệu ứng lật thẻ 3D xoay trục Y (RotationY) 180 độ giữa mặt trước (cardFront) và mặt sau (cardBack).
     */
    public static void flipCard3D(View cardFront, View cardBack, boolean[] isFlipped) {
        if (cardFront == null || cardBack == null || isFlipped == null) return;
        DecelerateInterpolator interpolator = new DecelerateInterpolator();
        if (!isFlipped[0]) {
            cardFront.animate()
                    .rotationY(90)
                    .setDuration(280)
                    .setInterpolator(interpolator)
                    .withEndAction(() -> {
                        cardFront.setVisibility(View.GONE);
                        cardBack.setVisibility(View.VISIBLE);
                        cardBack.setRotationY(-90);
                        cardBack.animate()
                                .rotationY(0)
                                .setDuration(280)
                                .setInterpolator(interpolator)
                                .start();
                    })
                    .start();
            isFlipped[0] = true;
        } else {
            cardBack.animate()
                    .rotationY(-90)
                    .setDuration(280)
                    .setInterpolator(interpolator)
                    .withEndAction(() -> {
                        cardBack.setVisibility(View.GONE);
                        cardFront.setVisibility(View.VISIBLE);
                        cardFront.setRotationY(90);
                        cardFront.animate()
                                .rotationY(0)
                                .setDuration(280)
                                .setInterpolator(interpolator)
                                .start();
                    })
                    .start();
            isFlipped[0] = false;
        }
    }

    /**
     * Khởi tạo và cập nhật động các Chip lọc chủ đề con trong danh sách từ vựng.
     * Tự động bổ sung Chip "Tất cả", danh sách chủ đề, Chip "Khác" (nếu chọn filter Đã thêm) và khôi phục trạng thái Chip đang chọn.
     */
    public static void populateSubTopicChips(Context context, ChipGroup chipGroupSubTopics, List<TopicEntity> allTopics, int checkedMainChipId) {
        if (context == null || chipGroupSubTopics == null) return;

        int previousSubTag = -1;
        int checkedSubId = chipGroupSubTopics.getCheckedChipId();
        View checkedSubView = chipGroupSubTopics.findViewById(checkedSubId);
        if (checkedSubView != null && checkedSubView.getTag() instanceof Integer) {
            previousSubTag = (Integer) checkedSubView.getTag();
        }

        chipGroupSubTopics.removeAllViews();

        Chip allChip = new Chip(context);
        allChip.setText("Tất cả");
        allChip.setCheckable(true);
        allChip.setTag(-1);
        allChip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
        allChip.setTextColor(android.graphics.Color.parseColor("#1E3A5F"));
        chipGroupSubTopics.addView(allChip);

        if (allTopics != null) {
            for (TopicEntity t : allTopics) {
                Chip topicChip = new Chip(context);
                topicChip.setText(t.getTitle());
                topicChip.setCheckable(true);
                topicChip.setTag(t.getId());
                topicChip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
                topicChip.setTextColor(android.graphics.Color.parseColor("#1E3A5F"));
                chipGroupSubTopics.addView(topicChip);
            }
        }

        if (checkedMainChipId == R.id.chipAdded) {
            Chip otherChip = new Chip(context);
            otherChip.setText("Khác");
            otherChip.setCheckable(true);
            otherChip.setTag(-2);
            otherChip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
            otherChip.setTextColor(android.graphics.Color.parseColor("#1E3A5F"));
            chipGroupSubTopics.addView(otherChip);
        }

        boolean restored = false;
        for (int i = 0; i < chipGroupSubTopics.getChildCount(); i++) {
            View child = chipGroupSubTopics.getChildAt(i);
            if (child instanceof Chip) {
                Chip c = (Chip) child;
                if (c.getTag() instanceof Integer && (Integer) c.getTag() == previousSubTag) {
                    c.setChecked(true);
                    restored = true;
                    break;
                }
            }
        }
        if (!restored && chipGroupSubTopics.getChildCount() > 0) {
            ((Chip) chipGroupSubTopics.getChildAt(0)).setChecked(true);
        }
    }

    /**
     * Cài đặt listener lắng nghe sự kiện cuộn ngang để ẩn/hiện mũi tên gợi ý cuộn trên danh sách Chip lọc.
     */
    public static void setupHorizontalScrollHints(HorizontalScrollView scrollMainFilter, TextView tvScrollHintMain,
                                                   HorizontalScrollView scrollSubTopics, TextView tvScrollHintSub) {
        if (scrollMainFilter != null && tvScrollHintMain != null) {
            scrollMainFilter.post(() -> tvScrollHintMain.setVisibility(scrollMainFilter.canScrollHorizontally(1) ? View.VISIBLE : View.GONE));
            scrollMainFilter.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                tvScrollHintMain.setVisibility(scrollMainFilter.canScrollHorizontally(1) ? View.VISIBLE : View.GONE);
            });
        }
        if (scrollSubTopics != null && tvScrollHintSub != null) {
            scrollSubTopics.post(() -> tvScrollHintSub.setVisibility(scrollSubTopics.canScrollHorizontally(1) ? View.VISIBLE : View.GONE));
            scrollSubTopics.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                tvScrollHintSub.setVisibility(scrollSubTopics.canScrollHorizontally(1) ? View.VISIBLE : View.GONE);
            });
        }
    }

    /**
     * Hủy bỏ hiệu ứng animation đang chạy.
     */
    public void cancel() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
}
