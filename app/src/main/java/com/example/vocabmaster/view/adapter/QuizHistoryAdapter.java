package com.example.vocabmaster.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vocabmaster.R;
import com.example.vocabmaster.model.entity.QuizResultWithQuiz;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuizHistoryAdapter extends RecyclerView.Adapter<QuizHistoryAdapter.ViewHolder> {

    private final List<QuizResultWithQuiz> results;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public QuizHistoryAdapter(List<QuizResultWithQuiz> results) {
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizResultWithQuiz result = results.get(position);
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

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
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
