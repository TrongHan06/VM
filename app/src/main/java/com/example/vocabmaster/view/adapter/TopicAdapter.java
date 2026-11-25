package com.example.vocabmaster.view.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabmaster.R;
import com.example.vocabmaster.model.entity.TopicEntity;
import com.example.vocabmaster.model.entity.TopicProgressItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.Map;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {

    public interface OnTopicClickListener {
        void onTopicClick(TopicEntity topic);
    }

    private List<TopicEntity> topicList;
    private Map<Integer, TopicProgressItem> progressMap;
    private OnTopicClickListener listener;

    public TopicAdapter(List<TopicEntity> topicList) {
        this.topicList = topicList;
    }

    public TopicAdapter(List<TopicEntity> topicList, OnTopicClickListener listener) {
        this.topicList = topicList;
        this.listener = listener;
    }

    public void setOnTopicClickListener(OnTopicClickListener listener) {
        this.listener = listener;
    }

    public void updateProgressMap(Map<Integer, TopicProgressItem> progressMap) {
        this.progressMap = progressMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        TopicEntity topic = topicList.get(position);

        holder.tvTitle.setText(topic.getTitle());
        holder.tvSubTitle.setText(topic.getSubTitle());
        holder.imgIcon.setImageResource(topic.getIconResId());

        int total = topic.getTotalCount() > 0 ? topic.getTotalCount() : 1;
        int learned = topic.getLearnedCount();
        int percent = Math.min(100, (learned * 100) / total);

        if (progressMap != null && progressMap.containsKey(topic.getId())) {
            TopicProgressItem item = progressMap.get(topic.getId());
            if (item != null) {
                percent = item.getProgressPercent();
                if (item.getTotalWords() > 0) total = item.getTotalWords();
            }
        }

        holder.progressBar.setMax(total);
        holder.progressBar.setProgress(learned);
        if (holder.tvProgressText != null) {
            holder.tvProgressText.setText(learned + "/" + total + " từ");
        }
        if (holder.tvTopicPercentBadge != null) {
            holder.tvTopicPercentBadge.setText(percent + "%");
        }

        applyDynamicTopicPalette(holder, topic.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTopicClick(topic);
            }
        });
    }

    private void applyDynamicTopicPalette(TopicViewHolder holder, String title) {
        String bgHex = "#EFF6FF";
        String accentHex = "#2563EB";
        String trackHex = "#DBEAFE";

        if (title != null) {
            switch (title.trim()) {
                case "Gia đình":
                    bgHex = "#EFF6FF"; accentHex = "#2563EB"; trackHex = "#DBEAFE";
                    break;
                case "Trường học":
                    bgHex = "#F3E8FF"; accentHex = "#7C3AED"; trackHex = "#E9D5FF";
                    break;
                case "Công việc":
                    bgHex = "#FEF3C7"; accentHex = "#D97706"; trackHex = "#FDE68A";
                    break;
                case "Đồ ăn":
                    bgHex = "#FFE4E6"; accentHex = "#E11D48"; trackHex = "#FECDD3";
                    break;
                case "Du lịch":
                    bgHex = "#D1FAE5"; accentHex = "#059669"; trackHex = "#A7F3D0";
                    break;
                case "Công nghệ":
                    bgHex = "#CFFAFE"; accentHex = "#0891B2"; trackHex = "#A5F3FC";
                    break;
                default:
                    bgHex = "#EFF6FF"; accentHex = "#2563EB"; trackHex = "#DBEAFE";
                    break;
            }
        }

        if (holder.cardIconContainer != null) {
            holder.cardIconContainer.setCardBackgroundColor(Color.parseColor(bgHex));
        }
        if (holder.progressBar != null) {
            holder.progressBar.setIndicatorColor(Color.parseColor(accentHex));
            holder.progressBar.setTrackColor(Color.parseColor(trackHex));
        }
        if (holder.tvTopicPercentBadge != null) {
            holder.tvTopicPercentBadge.setTextColor(Color.parseColor(accentHex));
        }
    }

    @Override
    public int getItemCount() {
        return topicList != null ? topicList.size() : 0;
    }

    public static class TopicViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvSubTitle, tvProgressText, tvTopicPercentBadge;
        ImageView imgIcon;
        MaterialCardView cardIconContainer;
        LinearProgressIndicator progressBar;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTopicNameVi);
            tvSubTitle = itemView.findViewById(R.id.tvTopicNameEn);
            tvProgressText = itemView.findViewById(R.id.tvTopicProgressText);
            tvTopicPercentBadge = itemView.findViewById(R.id.tvTopicPercentBadge);
            imgIcon = itemView.findViewById(R.id.imgTopicIcon);
            cardIconContainer = itemView.findViewById(R.id.cardIconContainer);
            progressBar = itemView.findViewById(R.id.progressTopic);
        }
    }
}
