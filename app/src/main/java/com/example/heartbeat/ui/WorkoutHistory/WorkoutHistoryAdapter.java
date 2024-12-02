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
        holder.titleTextView.setText(workout.get("title"));
        holder.artistTextView.setText(workout.get("artist"));
        holder.bpmTextView.setText(workout.get("bpm"));
    }

    @Override
    public int getItemCount() {
        return workoutHistoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView titleTextView;
        TextView artistTextView;
        TextView bpmTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            titleTextView = itemView.findViewById(R.id.textTitle);
            artistTextView = itemView.findViewById(R.id.textArtist);
            bpmTextView = itemView.findViewById(R.id.textBpm);
        }
    }
}
