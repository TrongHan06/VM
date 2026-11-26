package com.example.vocabmaster.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabmaster.R;
import com.example.vocabmaster.model.entity.PronunciationResult;
import com.example.vocabmaster.model.entity.TopicEntity;
import com.example.vocabmaster.model.entity.VocabEntity;
import com.example.vocabmaster.view.adapter.VocabAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp View quản lý các BottomSheetDialog liên quan đến Từ vựng.
 * Bao gồm: Tìm kiếm từ vựng, Xem chi tiết từ, Thêm từ mới thủ công, Luyện phát âm (STT).
 */
public class VocabDialogView {

    public interface OnSpeechListener {
        void onSpeak(String text);
    }

    public interface OnFavoriteToggleListener {
        void onFavoriteToggle(VocabEntity vocab, boolean isFavorite);
    }

    public interface OnResetProgressListener {
        void onResetProgress(VocabEntity vocab);
    }

    public interface OnDeleteVocabListener {
        void onDeleteVocab(VocabEntity vocab);
    }

    public interface OnSaveAddVocabListener {
        void onSaveVocab(VocabEntity newVocab);
    }

    public interface OnStartRecordingListener {
        void onStartRecording(String targetWord);
    }

    public interface OnSearchQueryProvider {
        List<VocabEntity> search(String query);
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    private static void setTransparentBackground(BottomSheetDialog dialog) {
        if (dialog != null && dialog.getWindow() != null) {
            View bottomSheetContainer = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetContainer != null) {
                bottomSheetContainer.setBackgroundResource(android.R.color.transparent);
            }
        }
    }

    /**
     * Hiển thị BottomSheetDialog tìm kiếm từ vựng ngay trên màn hình hiện tại.
     * Tự động lọc real-time khi người dùng gõ từ khóa và không chuyển tab ngoài ý muốn.
     */
    public static BottomSheetDialog showSearchVocabDialog(Context context, String initialQuery,
                                                        OnSearchQueryProvider queryProvider,
                                                        OnSpeechListener speechListener,
                                                        OnFavoriteToggleListener favoriteListener,
                                                        VocabAdapter.OnVocabClickListener vocabClickListener,
                                                        OnDismissListener dismissListener) {
        if (context == null || queryProvider == null) return null;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_search_vocab, null);
        dialog.setContentView(sheetView);
        setTransparentBackground(dialog);

        TextInputEditText etSearchQuery = sheetView.findViewById(R.id.etSearchQuery);
        TextView tvSearchCount = sheetView.findViewById(R.id.tvSearchCount);
        TextView tvEmptySearch = sheetView.findViewById(R.id.tvEmptySearch);
        RecyclerView rvSearchResults = sheetView.findViewById(R.id.rvSearchResults);

        if (rvSearchResults != null) {
            rvSearchResults.setLayoutManager(new LinearLayoutManager(context));
        }

        final List<VocabEntity> currentResults = new ArrayList<>();
        final VocabAdapter[] adapter = new VocabAdapter[1];

        Runnable performSearch = () -> {
            String query = etSearchQuery != null && etSearchQuery.getText() != null ? etSearchQuery.getText().toString().trim() : "";
            List<VocabEntity> results = queryProvider.search(query);
            currentResults.clear();
            if (results != null) currentResults.addAll(results);

            if (tvEmptySearch != null) {
                tvEmptySearch.setVisibility(currentResults.isEmpty() ? View.VISIBLE : View.GONE);
            }
            if (tvSearchCount != null) {
                if (query.isEmpty()) {
                    tvSearchCount.setText("Tìm thấy " + currentResults.size() + " từ vựng");
                } else {
                    tvSearchCount.setText("Kết quả tìm kiếm cho \"" + query + "\": " + currentResults.size() + " từ");
                }
            }

            if (adapter[0] == null) {
                adapter[0] = new VocabAdapter(currentResults,
                        vocab -> {
                            if (speechListener != null) speechListener.onSpeak(vocab.getWord());
                        },
                        (vocab, isFav) -> {
                            if (favoriteListener != null) favoriteListener.onFavoriteToggle(vocab, isFav);
                        }
                );
                adapter[0].setOnVocabClickListener(vocab -> {
                    if (vocabClickListener != null) {
                        vocabClickListener.onVocabClick(vocab);
                    } else {
                        showWordDetailDialog(context, vocab, speechListener, favoriteListener, null, null);
                    }
                });
                if (rvSearchResults != null) rvSearchResults.setAdapter(adapter[0]);
            } else {
                adapter[0].updateData(currentResults);
            }
        };

        if (etSearchQuery != null) {
            if (initialQuery != null && !initialQuery.isEmpty()) {
                etSearchQuery.setText(initialQuery);
                etSearchQuery.setSelection(initialQuery.length());
            }

            etSearchQuery.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    performSearch.run();
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        performSearch.run();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int targetHeight = (int) (displayMetrics.heightPixels * 0.85);
        if (sheetView.getLayoutParams() != null) {
            sheetView.getLayoutParams().height = targetHeight;
        }
        if (sheetView.getParent() != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) sheetView.getParent());
            behavior.setPeekHeight(targetHeight);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        dialog.setOnDismissListener(d -> {
            if (dismissListener != null) dismissListener.onDismiss();
        });

        dialog.show();
        return dialog;
    }

    /**
     * Hiển thị BottomSheetDialog xem thông tin chi tiết một từ vựng.
     * Cho phép phát âm, đánh dấu yêu thích, đặt lại tiến độ học hoặc xóa từ khỏi DB.
     */
    public static void showWordDetailDialog(Context context, VocabEntity vocab,
                                            OnSpeechListener speechListener,
                                            OnFavoriteToggleListener favoriteListener,
                                            OnResetProgressListener resetListener,
                                            OnDeleteVocabListener deleteListener) {
        if (context == null || vocab == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_word_detail, null);
        dialog.setContentView(sheetView);
        setTransparentBackground(dialog);

        TextView tvDetailWord = sheetView.findViewById(R.id.tvDetailWord);
        TextView tvDetailPhoneticAndMeta = sheetView.findViewById(R.id.tvDetailPhoneticAndMeta);
        TextView tvDetailMeaning = sheetView.findViewById(R.id.tvDetailMeaning);
        TextView tvDetailExampleEn = sheetView.findViewById(R.id.tvDetailExampleEn);
        TextView tvDetailExampleVi = sheetView.findViewById(R.id.tvDetailExampleVi);
        ImageButton btnDetailSpeaker = sheetView.findViewById(R.id.btnDetailSpeaker);
        ImageButton btnDetailFavorite = sheetView.findViewById(R.id.btnDetailFavorite);
        View btnResetWordProgress = sheetView.findViewById(R.id.btnResetWordProgress);
        View btnDeleteWord = sheetView.findViewById(R.id.btnDeleteWord);

        if (tvDetailWord != null) tvDetailWord.setText(vocab.getWord() != null ? vocab.getWord() : "");
        if (tvDetailPhoneticAndMeta != null) {
            String phonetic = vocab.getPhonetic() != null ? vocab.getPhonetic().trim() : "";
            String wordType = (vocab.getWordType() != null && !vocab.getWordType().trim().isEmpty()) ? vocab.getWordType().trim() : "từ";
            String level = (vocab.getLevel() != null && !vocab.getLevel().trim().isEmpty()) ? vocab.getLevel().trim() : "A1";
            String meta = (phonetic.isEmpty() ? "" : phonetic + " • ") + wordType + " • " + level;
            tvDetailPhoneticAndMeta.setText(meta);
        }
        if (tvDetailMeaning != null) tvDetailMeaning.setText(vocab.getMeaning() != null ? vocab.getMeaning() : "");
        if (tvDetailExampleEn != null) {
            String exEn = vocab.getExample();
            if (exEn != null && !exEn.trim().isEmpty()) {
                tvDetailExampleEn.setVisibility(View.VISIBLE);
                tvDetailExampleEn.setText(exEn.trim());
            } else {
                tvDetailExampleEn.setText("");
                tvDetailExampleEn.setVisibility(View.GONE);
            }
        }
        if (tvDetailExampleVi != null) {
            String exVi = vocab.getExampleMeaning();
            if (exVi != null && !exVi.trim().isEmpty()) {
                tvDetailExampleVi.setVisibility(View.VISIBLE);
                tvDetailExampleVi.setText("(" + exVi.trim() + ")");
            } else {
                tvDetailExampleVi.setText("");
                tvDetailExampleVi.setVisibility(View.GONE);
            }
        }

        if (btnDetailFavorite != null) {
            btnDetailFavorite.setImageResource(vocab.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            btnDetailFavorite.setOnClickListener(v -> {
                boolean newFav = !vocab.isFavorite();
                vocab.setFavorite(newFav);
                btnDetailFavorite.setImageResource(newFav ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
                if (favoriteListener != null) favoriteListener.onFavoriteToggle(vocab, newFav);
            });
        }

        if (btnDetailSpeaker != null) {
            btnDetailSpeaker.setOnClickListener(v -> {
                if (speechListener != null) speechListener.onSpeak(vocab.getWord());
            });
        }

        if (btnResetWordProgress != null) {
            btnResetWordProgress.setOnClickListener(v -> {
                if (resetListener != null) resetListener.onResetProgress(vocab);
                dialog.dismiss();
            });
        }

        if (btnDeleteWord != null) {
            boolean isCustomWord = vocab.isCustom() || vocab.getId() > 120;
            btnDeleteWord.setVisibility(isCustomWord ? View.VISIBLE : View.GONE);
            if (isCustomWord) {
                btnDeleteWord.setOnClickListener(v -> {
                    new AlertDialog.Builder(context)
                            .setTitle("Xóa từ vựng")
                            .setMessage("Bạn có chắc chắn muốn xóa từ \"" + vocab.getWord() + "\" khỏi cơ sở dữ liệu?")
                            .setPositiveButton("Xóa", (d, w) -> {
                                if (deleteListener != null) deleteListener.onDeleteVocab(vocab);
                                dialog.dismiss();
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                });
            }
        }

        dialog.show();
    }

    /**
     * Hiển thị BottomSheetDialog thêm từ vựng mới thủ công vào Database.
     */
    public static void showAddVocabDialog(Context context, List<TopicEntity> topics, OnSaveAddVocabListener saveListener) {
        if (context == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_add_vocab, null);
        dialog.setContentView(sheetView);
        setTransparentBackground(dialog);

        com.google.android.material.textfield.TextInputEditText etAddWord = sheetView.findViewById(R.id.etAddWord);
        com.google.android.material.textfield.TextInputEditText etAddPhonetic = sheetView.findViewById(R.id.etAddPhonetic);
        com.google.android.material.textfield.TextInputEditText etAddMeaning = sheetView.findViewById(R.id.etAddMeaning);
        com.google.android.material.textfield.TextInputEditText etAddExampleEn = sheetView.findViewById(R.id.etAddExampleEn);
        com.google.android.material.textfield.TextInputEditText etAddExampleVi = sheetView.findViewById(R.id.etAddExampleVi);
        Spinner spinnerTopic = sheetView.findViewById(R.id.spinnerTopic);
        View btnSaveVocab = sheetView.findViewById(R.id.btnSaveVocab);

        List<String> topicTitles = new ArrayList<>();
        if (topics != null) {
            for (TopicEntity t : topics) {
                topicTitles.add(t.getTitle());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, topicTitles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerTopic != null) {
            spinnerTopic.setAdapter(adapter);
        }

        if (btnSaveVocab != null) {
            btnSaveVocab.setOnClickListener(v -> {
                String word = etAddWord != null && etAddWord.getText() != null ? etAddWord.getText().toString().trim() : "";
                String phonetic = etAddPhonetic != null && etAddPhonetic.getText() != null ? etAddPhonetic.getText().toString().trim() : "";
                String meaning = etAddMeaning != null && etAddMeaning.getText() != null ? etAddMeaning.getText().toString().trim() : "";
                String exampleEn = etAddExampleEn != null && etAddExampleEn.getText() != null ? etAddExampleEn.getText().toString().trim() : "";
                String exampleVi = etAddExampleVi != null && etAddExampleVi.getText() != null ? etAddExampleVi.getText().toString().trim() : "";

                if (word.isEmpty() || meaning.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập Từ tiếng Anh và Nghĩa tiếng Việt!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int topicId = 1;
                if (topics != null && spinnerTopic != null && spinnerTopic.getSelectedItemPosition() >= 0 && spinnerTopic.getSelectedItemPosition() < topics.size()) {
                    topicId = topics.get(spinnerTopic.getSelectedItemPosition()).getId();
                }

                if (phonetic.isEmpty()) phonetic = "/" + word.toLowerCase() + "/";
                if (exampleEn.isEmpty()) exampleEn = "Example sentence with " + word + ".";
                if (exampleVi.isEmpty()) exampleVi = "Câu ví dụ với từ " + word + ".";

                VocabEntity newVocab = new VocabEntity(topicId, word, phonetic, meaning, exampleEn, exampleVi, false, false);
                newVocab.setCustom(true);
                if (saveListener != null) saveListener.onSaveVocab(newVocab);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    /**
     * Hiển thị BottomSheetDialog luyện phát âm bằng microphone (Google STT).
     * Trả về ViewHolder để cập nhật kết quả nhận dạng giọng nói real-time.
     */
    public static SpeechPracticeDialogViewHolder showSpeechPracticeDialog(Context context, List<VocabEntity> vocabs, OnSpeechListener speechListener, OnStartRecordingListener recordingListener) {
        if (context == null || vocabs == null || vocabs.isEmpty()) return null;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_speech_practice, null);
        dialog.setContentView(sheetView);
        setTransparentBackground(dialog);

        TextView tvSpeechTargetWord = sheetView.findViewById(R.id.tvSpeechTargetWord);
        TextView tvSpeechPhonetic = sheetView.findViewById(R.id.tvSpeechPhonetic);
        View btnSpeechListen = sheetView.findViewById(R.id.btnSpeechListen);
        View btnStartRecording = sheetView.findViewById(R.id.btnStartRecording);
        View btnNextSpeechWord = sheetView.findViewById(R.id.btnNextSpeechWord);
        TextView tvSpeechResultStatus = sheetView.findViewById(R.id.tvSpeechResultStatus);
        TextView tvRecognizedText = sheetView.findViewById(R.id.tvRecognizedText);

        SpeechPracticeDialogViewHolder viewHolder = new SpeechPracticeDialogViewHolder(tvSpeechResultStatus, tvRecognizedText);

        final int[] speechIndex = {0};

        Runnable loadSpeechWord = () -> {
            if (speechIndex[0] >= vocabs.size()) speechIndex[0] = 0;
            VocabEntity current = vocabs.get(speechIndex[0]);
            viewHolder.setCurrentTargetWord(current.getWord());

            if (tvSpeechTargetWord != null) tvSpeechTargetWord.setText(current.getWord());
            if (tvSpeechPhonetic != null) tvSpeechPhonetic.setText(current.getPhonetic());
            if (tvSpeechResultStatus != null) {
                tvSpeechResultStatus.setText("Nhấn nút Micro để bắt đầu nói...");
                tvSpeechResultStatus.setTextColor(android.graphics.Color.parseColor("#475569"));
            }
            if (tvRecognizedText != null) tvRecognizedText.setText("");
        };

        loadSpeechWord.run();

        if (btnSpeechListen != null) {
            btnSpeechListen.setOnClickListener(v -> {
                if (speechListener != null) speechListener.onSpeak(viewHolder.getCurrentTargetWord());
            });
        }

        if (btnStartRecording != null) {
            btnStartRecording.setOnClickListener(v -> {
                if (recordingListener != null) recordingListener.onStartRecording(viewHolder.getCurrentTargetWord());
            });
        }

        if (btnNextSpeechWord != null) {
            btnNextSpeechWord.setOnClickListener(v -> {
                speechIndex[0]++;
                loadSpeechWord.run();
            });
        }

        dialog.show();
        return viewHolder;
    }

    /**
     * ViewHolder chứa các TextView kết quả phát âm và từ vựng mục tiêu đang được luyện.
     */
    public static class SpeechPracticeDialogViewHolder {
        private final TextView tvSpeechResultStatus;
        private final TextView tvRecognizedText;
        private String currentTargetWord = "";

        public SpeechPracticeDialogViewHolder(TextView tvSpeechResultStatus, TextView tvRecognizedText) {
            this.tvSpeechResultStatus = tvSpeechResultStatus;
            this.tvRecognizedText = tvRecognizedText;
        }

        /**
         * Lấy từ vựng mục tiêu hiện tại đang luyện phát âm.
         */
        public String getCurrentTargetWord() {
            return currentTargetWord;
        }

        /**
         * Cập nhật từ vựng mục tiêu hiện tại.
         */
        public void setCurrentTargetWord(String currentTargetWord) {
            this.currentTargetWord = currentTargetWord != null ? currentTargetWord : "";
        }

        /**
         * Cập nhật UI hiển thị kết quả đánh giá giọng nói (Đúng / Gần đúng / Sai).
         */
        public void updatePronunciationResult(PronunciationResult result) {
            if (result == null) return;
            if (tvRecognizedText != null) {
                tvRecognizedText.setText("Giọng nói ghi nhận: \"" + result.getRecognizedText() + "\"");
            }
            if (tvSpeechResultStatus != null) {
                tvSpeechResultStatus.setText(result.getStatusMessage());
                tvSpeechResultStatus.setTextColor(android.graphics.Color.parseColor(result.getColorHex()));
            }
        }
    }
}
