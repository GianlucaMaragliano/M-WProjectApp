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

    public WorkoutHistoryAdapter(Map<String, List<Map<String, String>>> groupedHistory) {
        this.groupedHistory = groupedHistory;
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
        // Convert groupedHistory keys to a list to access by position
        List<String> workoutIds = new ArrayList<>(groupedHistory.keySet());
        String workoutId = workoutIds.get(position);
        List<Map<String, String>> songs = groupedHistory.get(workoutId);

        // Bind workout ID and songs to the ViewHolder
        holder.workoutIdView.setText("Workout #" + (position + 1));
        holder.songsView.setText(formatSongs(songs));
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


