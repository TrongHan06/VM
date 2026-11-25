package com.example.vocabmaster.view.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabmaster.R;
import com.example.vocabmaster.model.entity.VocabEntity;

import java.util.List;

public class VocabAdapter extends RecyclerView.Adapter<VocabAdapter.VocabViewHolder> {

    public interface OnVocabClickListener {
        void onVocabClick(VocabEntity vocab);
    }

    public interface OnPronounceClickListener {
        void onPronounceClick(VocabEntity vocab);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(VocabEntity vocab, boolean isFavorite);
    }

    private List<VocabEntity> vocabList;
    private OnVocabClickListener clickListener;
    private OnPronounceClickListener pronounceListener;
    private OnFavoriteClickListener favoriteListener;

    public VocabAdapter(List<VocabEntity> vocabList) {
        this.vocabList = vocabList;
    }

    public VocabAdapter(List<VocabEntity> vocabList, OnPronounceClickListener pronounceListener, OnFavoriteClickListener favoriteListener) {
        this.vocabList = vocabList;
        this.pronounceListener = pronounceListener;
        this.favoriteListener = favoriteListener;
    }

    public void setOnVocabClickListener(OnVocabClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setOnPronounceClickListener(OnPronounceClickListener pronounceListener) {
        this.pronounceListener = pronounceListener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener favoriteListener) {
        this.favoriteListener = favoriteListener;
    }

    public void updateData(List<VocabEntity> newList) {
        this.vocabList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VocabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vocab_row, parent, false);
        return new VocabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabViewHolder holder, int position) {
        VocabEntity vocab = vocabList.get(position);
        holder.tvRowWord.setText(vocab.getWord());
        holder.tvRowPhonetic.setText(vocab.getPhonetic());
        holder.tvRowMeaning.setText(vocab.getMeaning());

        String level = vocab.getLevel() != null ? vocab.getLevel() : "A1";
        holder.tvRowLevel.setText(level);
        if ("A1".equalsIgnoreCase(level) || "A2".equalsIgnoreCase(level)) {
            holder.tvRowLevel.setTextColor(Color.parseColor("#16A34A"));
        } else if ("B1".equalsIgnoreCase(level) || "B2".equalsIgnoreCase(level)) {
            holder.tvRowLevel.setTextColor(Color.parseColor("#2563EB"));
        } else {
            holder.tvRowLevel.setTextColor(Color.parseColor("#7C3AED"));
        }

        String wordType = vocab.getWordType() != null ? vocab.getWordType() : "n";
        holder.tvRowWordType.setText(wordType);

        if (holder.vLearnedIndicator != null) {
            holder.vLearnedIndicator.setVisibility(vocab.isLearned() ? View.VISIBLE : View.GONE);
        }

        if (vocab.isFavorite()) {
            holder.btnRowFavorite.setImageResource(R.drawable.ic_star_filled);
        } else {
            holder.btnRowFavorite.setImageResource(R.drawable.ic_star_outline);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onVocabClick(vocab);
            }
        });

        holder.btnRowSpeaker.setOnClickListener(v -> {
            if (pronounceListener != null) {
                pronounceListener.onPronounceClick(vocab);
            }
        });

        holder.btnRowFavorite.setOnClickListener(v -> {
            boolean newFavState = !vocab.isFavorite();
            vocab.setFavorite(newFavState);
            if (newFavState) {
                holder.btnRowFavorite.setImageResource(R.drawable.ic_star_filled);
            } else {
                holder.btnRowFavorite.setImageResource(R.drawable.ic_star_outline);
            }

            holder.btnRowFavorite.setScaleX(1.3f);
            holder.btnRowFavorite.setScaleY(1.3f);
            holder.btnRowFavorite.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start();

            if (favoriteListener != null) {
                favoriteListener.onFavoriteClick(vocab, newFavState);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vocabList != null ? vocabList.size() : 0;
    }

    public static class VocabViewHolder extends RecyclerView.ViewHolder {
        TextView tvRowWord, tvRowPhonetic, tvRowMeaning, tvRowLevel, tvRowWordType;
        View vLearnedIndicator;
        ImageButton btnRowSpeaker, btnRowFavorite;

        public VocabViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRowWord = itemView.findViewById(R.id.tvRowWord);
            tvRowPhonetic = itemView.findViewById(R.id.tvRowPhonetic);
            tvRowMeaning = itemView.findViewById(R.id.tvRowMeaning);
            tvRowLevel = itemView.findViewById(R.id.tvRowLevel);
            tvRowWordType = itemView.findViewById(R.id.tvRowWordType);
            vLearnedIndicator = itemView.findViewById(R.id.vLearnedIndicator);
            btnRowSpeaker = itemView.findViewById(R.id.btnRowSpeaker);
            btnRowFavorite = itemView.findViewById(R.id.btnRowFavorite);
        }
    }
}
