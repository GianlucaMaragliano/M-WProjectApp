package com.example.heartbeat.ui.WorkoutHistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.heartbeat.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder> {
    private final Map<String, List<Map<String, String>>> groupedHistory;
    private final OnWorkoutClickListener listener;

    public WorkoutHistoryAdapter(Map<String, List<Map<String, String>>> groupedHistory, OnWorkoutClickListener listener) {
        this.groupedHistory = groupedHistory;
        this.listener = listener;
    }

    public interface OnWorkoutClickListener {
        void onWorkoutClick(String workoutId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        List<String> workoutIds = new ArrayList<>(groupedHistory.keySet());
//        String workoutId = workoutIds.get(position);
//        List<Map<String, String>> songs = groupedHistory.get(workoutId);
//
//        holder.workoutIdView.setText("Workout ID: " + workoutId);
//        holder.songsView.setText(formatSongs(songs));
//        holder.itemView.setOnClickListener(v -> listener.onWorkoutClick(workoutId));
        String workoutId = new ArrayList<>(groupedHistory.keySet()).get(position);
        List<Map<String, String>> songs = groupedHistory.get(workoutId);
        holder.workoutIdView.setText("Workout ID: " + workoutId);
        holder.songsView.setText(formatSongs(songs));
        holder.itemView.setOnClickListener(v -> {
           if (listener != null) {
               listener.onWorkoutClick(workoutId);
           }
        });
    }

    @Override
    public int getItemCount() {
        return groupedHistory.size();
    }

    private String formatSongs(List<Map<String, String>> songs) {
        StringBuilder formatted = new StringBuilder();
        for (Map<String, String> song : songs) {
            formatted.append(
                    song.get("timestamp"))
                    .append(" - ")
                    .append(song.get("title"))
                    .append(" by ")
                    .append(song.get("artist"))
                    .append("\n");
        }
        return formatted.toString();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView workoutIdView;
        TextView songsView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutIdView = itemView.findViewById(R.id.workout_id_view);
            songsView = itemView.findViewById(R.id.songs_view);
        }
    }
}


