package com.example.vocabmaster.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabmaster.R;
import com.example.vocabmaster.model.entity.TopicEntity;
import com.example.vocabmaster.view.dialog.AppInfoDialogView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Adapter hiển thị danh sách phần trăm tiến độ học của từng chủ đề trong Dialog Tiến độ tổng quan.
 */
public class OverallProgressAdapter extends ListAdapter<TopicEntity, OverallProgressAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<TopicEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<TopicEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull TopicEntity oldItem, @NonNull TopicEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TopicEntity oldItem, @NonNull TopicEntity newItem) {
            return oldItem.getId() == newItem.getId()
                    && Objects.equals(oldItem.getTitle(), newItem.getTitle())
                    && Objects.equals(oldItem.getSubTitle(), newItem.getSubTitle())
                    && oldItem.getLearnedCount() == newItem.getLearnedCount()
                    && oldItem.getTotalCount() == newItem.getTotalCount();
        }
    };

    private final AppInfoDialogView.TopicProgressCalculator calculator;

    public OverallProgressAdapter(List<TopicEntity> topics, AppInfoDialogView.TopicProgressCalculator calculator) {
        super(DIFF_CALLBACK);
        this.calculator = calculator;
        submitList(topics != null ? new ArrayList<>(topics) : null);
    }

    public void updateTopics(List<TopicEntity> newList) {
        submitList(newList != null ? new ArrayList<>(newList) : null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_overall_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TopicEntity topic = getItem(position);
        holder.tvTitle.setText(topic.getTitle());
        holder.tvSub.setText(topic.getSubTitle());
        holder.imgIcon.setImageResource(topic.getIconResId());

        int percent = calculator != null ? calculator.calculatePercent(topic.getId()) : 0;
        holder.tvPercent.setText(percent + "%");
        holder.progressBar.setProgress(percent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvTitle, tvSub, tvPercent;
        LinearProgressIndicator progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgItemTopicIcon);
            tvTitle = itemView.findViewById(R.id.tvItemTopicTitle);
            tvSub = itemView.findViewById(R.id.tvItemTopicSub);
            tvPercent = itemView.findViewById(R.id.tvItemTopicPercent);
            progressBar = itemView.findViewById(R.id.progressItemTopic);
        }
    }
}
