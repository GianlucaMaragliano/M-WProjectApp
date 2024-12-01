package com.example.heartbeat.ui.WorkoutHistory;

import android.os.Bundle;
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
import java.util.List;
import java.util.Map;

public class HistoryFragment extends  Fragment {
    private HeartBeatOpenHelper databaseHelper;
    private SoundManager soundManager;
    private RecyclerView recyclerView;
    private WorkoutHistoryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        // Set up RecyclerView
        recyclerView = rootView.findViewById(R.id.recyclerViewWorkoutHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load workout history data
        loadWorkoutHistory();

        return rootView;
    }

    private void loadWorkoutHistory() {
        HeartBeatOpenHelper dbHelper = new HeartBeatOpenHelper(getContext());
        List<Map<String, String>> workoutHistory = dbHelper.getWorkoutSongsByDate("2024-12-01"); // Example date
        adapter = new WorkoutHistoryAdapter(workoutHistory);
        recyclerView.setAdapter(adapter);
    }
}
