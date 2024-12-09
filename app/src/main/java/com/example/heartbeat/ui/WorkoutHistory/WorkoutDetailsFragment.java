package com.example.heartbeat.ui.WorkoutHistory;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;


import com.example.heartbeat.HeartBeatOpenHelper;
import com.example.heartbeat.R;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkoutDetailsFragment extends Fragment {
    private String workoutId;
    private TextView workoutDetailsTextView;
    private RecyclerView recyclerView;
    private SongsAdapter adapter;
    private LineChart lineChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_workout_details, container, false);

        // Initialize LineChart
        lineChart = rootView.findViewById(R.id.lineChart);

        // Get the workoutId passed from HistoryFragment
        if (getArguments() != null) {
            workoutId = getArguments().getString("workoutId");
        }

        if (workoutId != null) {
            loadSongsForWorkout(workoutId);
        } else {
            Log.e("WorkoutDetailsFragment", "No workoutId found in arguments");
        }

        // Initialize the button for creating workout exercises
        Button createWorkoutExercisesButton = rootView.findViewById(R.id.createWorkoutExercisesButton);
        createWorkoutExercisesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateWorkoutExercisesClicked();
            }
        });

        return rootView;
    }

    // This method will be triggered when the button is clicked
    public void onCreateWorkoutExercisesClicked() {
        // Navigate to the exercise creation screen
        // You can either use a new Activity or a Fragment for creating exercises
        Intent intent = new Intent(getContext(), CreateExerciseActivity.class);
        intent.putExtra("workoutId", workoutId);  // Pass workoutId to the new activity/fragment
        startActivity(intent);
    }

    private void loadSongsForWorkout(String workoutId) {
        HeartBeatOpenHelper dbHelper = new HeartBeatOpenHelper(getContext());

        // Query the database for songs associated with this workoutId
        List<Map<String, String>> songsList = dbHelper.getWorkoutSongsByWorkoutId(workoutId);

        if (songsList.isEmpty()) {
            Log.w("WorkoutDetailsFragment", "No songs found for workoutId: " + workoutId);
        } else {
            Log.d("WorkoutDetailsFragment", "Songs loaded for workoutId " + workoutId + ": " + songsList);
        }

        Log.d("SongsAdapter", "Songs list size: " + songsList.size());
        for (Map<String, String> song : songsList) {
            Log.d("SongsAdapter", "Song: " + song.toString());
        }

        // Prepare data for LineChart
        setupLineChart(lineChart, songsList);
    }

    private void setupLineChart(LineChart lineChart, List<Map<String, String>> songsList) {
        List<Entry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();
        float minBpm = Float.MAX_VALUE;
        float maxBpm = Float.MIN_VALUE;

        // Populate entries and find min/max BPM values
        for (int i = 0; i < songsList.size(); i++) {
            Map<String, String> song = songsList.get(i);
            String timestamp = song.get("timestamp");
            String bpmString = song.get("bpm");

            try {
                float bpm = Float.parseFloat(bpmString);
                entries.add(new Entry(i, bpm));
                xLabels.add(timestamp);

                // Track min and max BPM
                if (bpm < minBpm) minBpm = bpm;
                if (bpm > maxBpm) maxBpm = bpm;

            } catch (NumberFormatException e) {
                Log.e("LineChart", "Invalid BPM value: " + bpmString);
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "BPM over Time");
        dataSet.setColor(getResources().getColor(R.color.dark_md_theme_inversePrimary_highContrast)); // Set line color
        dataSet.setValueTextColor(getResources().getColor(R.color.dark_md_theme_background_highContrast)); // Set value text color

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Customize the X-Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        // Customize the Left Y-Axis (BPM values)
        YAxis leftYAxis = lineChart.getAxisLeft();
        leftYAxis.setAxisMinimum(minBpm - 5); // Set the minimum value slightly below the minBpm
        leftYAxis.setAxisMaximum(maxBpm + 5); // Set the maximum value slightly above the maxBpm
        leftYAxis.setDrawLabels(true); // Show labels on the left axis

        // Disable the Right Y-Axis
        YAxis rightYAxis = lineChart.getAxisRight();
        rightYAxis.setEnabled(false); // Hide the right Y-axis

        // Set the OnChartValueSelectedListener
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) e.getX();  // Get the index of the selected point
                if (index >= 0 && index < songsList.size()) {
                    Map<String, String> song = songsList.get(index);
                    showSongDetails(song);  // Show details of the selected song
                }
            }

            @Override
            public void onNothingSelected() {
                // Handle the case when no point is selected (optional)
            }
        });

        // Refresh the chart
        lineChart.invalidate();
    }

    private void showSongDetails(Map<String, String> song) {
//        String title = song.get("title");
//        String artist = song.get("artist");
//        String timestamp = song.get("timestamp");
//        String bpm = song.get("bpm");
//
//        // Display the song details in a TextView or a Dialog
//        // Example: Display in a TextView
//        String songDetails = "Title: " + title + "\nArtist: " + artist + "\nTimestamp: " + timestamp + "\nBPM: " + bpm;
//        TextView songDetailsTextView = getView().findViewById(R.id.songDetailsTextView);  // Make sure this TextView exists in your layout
//        songDetailsTextView.setText(songDetails);
        String title = song.get("title");
        String artist = song.get("artist");
        String timestamp = song.get("timestamp");
        String bpm = song.get("bpm");

        // Create an alert dialog to show song details
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Song Details")
                .setMessage("Title: " + title + "\nArtist: " + artist + "\nTimestamp: " + timestamp + "\nBPM: " + bpm)
                .setPositiveButton("OK", null)
                .show();
    }

    public static WorkoutDetailsFragment newInstance(String workoutId) {
        WorkoutDetailsFragment fragment = new WorkoutDetailsFragment();
        Bundle args = new Bundle();
        args.putString("workoutId", workoutId);
        fragment.setArguments(args);
        return fragment;
    }
}
