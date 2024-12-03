package com.example.heartbeat.ui.WorkoutHistory;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.heartbeat.R;
import com.example.heartbeat.HeartBeatOpenHelper;
import com.example.heartbeat.SoundManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.SimpleDateFormat;

public class HistoryFragment extends  Fragment {
    private HeartBeatOpenHelper databaseHelper;
    private RecyclerView recyclerView;
    private WorkoutHistoryAdapter adapter;
    private TextView workoutOfDayTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        // Workout of current day
        workoutOfDayTextView = rootView.findViewById(R.id.textViewWorkoutOfDay);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = dateFormat.format(new Date());  // Gets current date in the format "2024-12-02"
        workoutOfDayTextView.setText("Workouts of " + dateStr);

        // Set up RecyclerView
        recyclerView = rootView.findViewById(R.id.recyclerViewWorkoutHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load workout history data
        loadWorkoutHistory();

        return rootView;
    }

    private void loadWorkoutHistory() {
        HeartBeatOpenHelper dbHelper = new HeartBeatOpenHelper(getContext());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = dateFormat.format(new Date());  // Gets current date in the format "2024-12-02"

        List<Map<String, String>> workoutHistory = dbHelper.getWorkoutSongsByDate(dateStr); // Example date
        if (workoutHistory.isEmpty()) {
            Log.e("HistoryFragment", "No workout history found for the given date.");
        } else {
            Log.d("HistoryFragment", "Workout history loaded: " + workoutHistory.toString());
        }

        // Group by workoutId
        Map<String, List<Map<String, String>>> groupedHistory = new HashMap<>();
        for (Map<String, String> songData : workoutHistory) {
            String workoutId = songData.get("id");
            if (workoutId == null) {
                Log.w("HistoryFragment", "Encountered entry with no workoutId: " + songData);
                continue;
            }
            groupedHistory.computeIfAbsent(workoutId, k -> new ArrayList<>()).add(songData);
        }

        // Log grouped history for debugging
        for (Map.Entry<String, List<Map<String, String>>> entry : groupedHistory.entrySet()) {
            Log.d("HistoryFragment", "Workout ID: " + entry.getKey() + ", Songs: " + entry.getValue());
        }


        adapter = new WorkoutHistoryAdapter(groupedHistory);
        recyclerView.setAdapter(adapter);
    }
}
