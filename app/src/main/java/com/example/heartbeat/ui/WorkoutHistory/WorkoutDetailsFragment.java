package com.example.heartbeat.ui.WorkoutHistory;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
    private LineChart lineChart;
    private WorkoutHistoryAdapter adapter;
    private Spinner songSpinner;
    private EditText exerciseNameInput, setCountInput, repCountInput;
    private Button saveExerciseButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_workout_details, container, false);

        // Initialize LineChart
        lineChart = rootView.findViewById(R.id.lineChart);

        // Initialize inputs for exercise creation
        exerciseNameInput = rootView.findViewById(R.id.exerciseNameInput);
        setCountInput = rootView.findViewById(R.id.setCountInput);
        repCountInput = rootView.findViewById(R.id.repCountInput);
        songSpinner = rootView.findViewById(R.id.songSpinner);
        saveExerciseButton = rootView.findViewById(R.id.saveExerciseButton);

        // Get the workoutId passed from HistoryFragment
        if (getArguments() != null) {
            workoutId = getArguments().getString("workoutId");
        }

        if (workoutId != null) {
            loadSongsForWorkout(workoutId);
        } else {
            Log.e("WorkoutDetailsFragment", "No workoutId found in arguments");
        }

        saveExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveExerciseClicked();
            }
        });

        return rootView;
    }

    // This method will be triggered when the save button is clicked
    public void onSaveExerciseClicked() {
        String exerciseName = exerciseNameInput.getText().toString();
        String setCountString = setCountInput.getText().toString();
        String repCountString = repCountInput.getText().toString();
        Map<String, String> selectedSong = (Map<String, String>) songSpinner.getSelectedItem();

        if (exerciseName.isEmpty() || setCountString.isEmpty() || repCountString.isEmpty() || selectedSong == null) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int setCount = Integer.parseInt(setCountString);
        int repCount = Integer.parseInt(repCountString);
        String songId = selectedSong.get("id");

        // Insert exercise into database
        HeartBeatOpenHelper dbHelper = new HeartBeatOpenHelper(getContext());
        dbHelper.insertWorkoutExercise(workoutId, songId, exerciseName, setCount, repCount);

        // Optionally, show a confirmation message
        Toast.makeText(getContext(), "Exercise saved", Toast.LENGTH_SHORT).show();
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

        // Populate Spinner with song titles
        ArrayAdapter<Map<String, String>> adapter = new ArrayAdapter<Map<String, String>>(getContext(), android.R.layout.simple_spinner_item, songsList) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                Map<String, String> song = getItem(position);
                String songDisplay = song.get("title") + " - " + song.get("artist");  // Customize the text display format
                text.setText(songDisplay);
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        songSpinner.setAdapter(adapter);

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
