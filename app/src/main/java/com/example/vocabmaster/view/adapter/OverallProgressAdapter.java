package com.example.vocabmaster.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabmaster.R;
import com.example.vocabmaster.model.entity.TopicEntity;
import com.example.vocabmaster.view.dialog.AppInfoDialogView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

/**
 * Adapter hiển thị danh sách phần trăm tiến độ học của từng chủ đề trong Dialog Tiến độ tổng quan.
 */
public class OverallProgressAdapter extends RecyclerView.Adapter<OverallProgressAdapter.ViewHolder> {

    private final List<TopicEntity> topics;
    private final AppInfoDialogView.TopicProgressCalculator calculator;

    public OverallProgressAdapter(List<TopicEntity> topics, AppInfoDialogView.TopicProgressCalculator calculator) {
        this.topics = topics;
        this.calculator = calculator;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_overall_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TopicEntity topic = topics.get(position);
        holder.tvTitle.setText(topic.getTitle());
        holder.tvSub.setText(topic.getSubTitle());
        holder.imgIcon.setImageResource(topic.getIconResId());

        int percent = calculator != null ? calculator.calculatePercent(topic.getId()) : 0;
        holder.tvPercent.setText(percent + "%");
        holder.progressBar.setProgress(percent);
    }

    @Override
    public int getItemCount() {
        return topics != null ? topics.size() : 0;
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
