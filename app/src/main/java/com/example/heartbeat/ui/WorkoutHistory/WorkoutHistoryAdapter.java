package com.example.heartbeat.ui.WorkoutHistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.heartbeat.R;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder> {

    private List<Map<String, String>> workoutHistoryList;

    public WorkoutHistoryAdapter(List<Map<String, String>> workoutHistoryList) {
        this.workoutHistoryList = workoutHistoryList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map<String, String> workout = workoutHistoryList.get(position);

        holder.dateTextView.setText(workout.get("date"));
        holder.songsTextView.setText(workout.get("songs"));
        holder.durationTextView.setText(workout.get("duration"));
    }

    @Override
    public int getItemCount() {
        return workoutHistoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView songsTextView;
        TextView durationTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            songsTextView = itemView.findViewById(R.id.textViewSongs);
            durationTextView = itemView.findViewById(R.id.textViewDuration);
        }
    }
}
