package com.example.vocabmaster.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabmaster.R;
import com.example.vocabmaster.model.entity.QuizResultWithQuiz;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class QuizHistoryAdapter extends ListAdapter<QuizResultWithQuiz, QuizHistoryAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<QuizResultWithQuiz> DIFF_CALLBACK = new DiffUtil.ItemCallback<QuizResultWithQuiz>() {
        @Override
        public boolean areItemsTheSame(@NonNull QuizResultWithQuiz oldItem, @NonNull QuizResultWithQuiz newItem) {
            return oldItem.getResultId() == newItem.getResultId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull QuizResultWithQuiz oldItem, @NonNull QuizResultWithQuiz newItem) {
            return oldItem.getResultId() == newItem.getResultId()
                    && oldItem.getQuizId() == newItem.getQuizId()
                    && Objects.equals(oldItem.getQuizName(), newItem.getQuizName())
                    && oldItem.getTotalQuestions() == newItem.getTotalQuestions()
                    && oldItem.getScore() == newItem.getScore()
                    && oldItem.getCorrectAnswers() == newItem.getCorrectAnswers()
                    && oldItem.getCompletedAt() == newItem.getCompletedAt();
        }
    };

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public QuizHistoryAdapter(List<QuizResultWithQuiz> results) {
        super(DIFF_CALLBACK);
        submitList(results != null ? new ArrayList<>(results) : null);
    }

    public void updateData(List<QuizResultWithQuiz> newList) {
        submitList(newList != null ? new ArrayList<>(newList) : null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizResultWithQuiz result = getItem(position);
        String title = (result.getQuizName() != null && !result.getQuizName().isEmpty()) ? result.getQuizName() : ("Bài kiểm tra #" + result.getResultId());
        holder.tvQuizName.setText(title);
        holder.tvDate.setText(dateFormat.format(new Date(result.getCompletedAt())));
        holder.tvScore.setText(result.getScore() + "%");
        holder.tvDetails.setText(result.getCorrectAnswers() + "/" + result.getTotalQuestions() + " câu đúng");

        if (result.getScore() >= 80) {
            holder.tvScore.setTextColor(android.graphics.Color.parseColor("#16A34A"));
        } else if (result.getScore() >= 50) {
            holder.tvScore.setTextColor(android.graphics.Color.parseColor("#D97706"));
        } else {
            holder.tvScore.setTextColor(android.graphics.Color.parseColor("#DC2626"));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuizName, tvDate, tvScore, tvDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuizName = itemView.findViewById(R.id.tvHistoryQuizName);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvScore = itemView.findViewById(R.id.tvHistoryScore);
            tvDetails = itemView.findViewById(R.id.tvHistoryDetails);
        }
    }
}
