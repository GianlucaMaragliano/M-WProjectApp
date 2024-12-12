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
import com.github.mikephil.charting.components.Legend;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutDetailsFragment extends Fragment {
    private String workoutId;
    private LineChart lineChart;
    private WorkoutHistoryAdapter adapter;
    private TextView detailsTextView;
    private TextView insightsTextView;
    HeartBeatOpenHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_workout_details, container, false);

        // Initialize LineChart
        lineChart = rootView.findViewById(R.id.lineChart);

        detailsTextView = rootView.findViewById(R.id.detailsTextView);
        insightsTextView = rootView.findViewById(R.id.insightsTextView);

        // Get the workoutId passed from HistoryFragment
        if (getArguments() != null) {
            workoutId = getArguments().getString("workoutId");
        }

        if (workoutId != null) {
            loadSongsForWorkout(workoutId);
        } else {
            Log.e("WorkoutDetailsFragment", "No workoutId found in arguments");
        }

        displayInsights(workoutId);

        return rootView;
    }

    private void displayInsights(String workoutId) {
        dbHelper = new HeartBeatOpenHelper(getContext());

        // Query the database for songs associated with this workoutId
        List<Map<String, String>> songDataList = dbHelper.getWorkoutSongsByWorkoutId(workoutId);
        if (songDataList.isEmpty()) {
            insightsTextView.setText("No workout data available.");
            return;
        }

        int totalBPM = 0;
        double peakBPM = Double.MIN_VALUE;
        double lowestBPM = Double.MAX_VALUE;
        int highIntensityThreshold = 140;
        int highIntensityDuration = 0;
        String peakSong = "";
        String lowestSong = "";
        Map<String, Integer> songPlayCount = new HashMap<>(); // Track song frequencies

        for (int i = 0; i < songDataList.size(); i++) {
            Map<String, String> song = songDataList.get(i);
            double bpm = Double.parseDouble(song.get("bpm"));
            totalBPM += bpm;

            if (bpm > peakBPM) {
                peakBPM = bpm;
                peakSong = song.get("title");
            }

            if (bpm < lowestBPM) {
                lowestBPM = bpm;
                lowestSong = song.get("title");
            }

            if (bpm > highIntensityThreshold) {
                highIntensityDuration++;
            }

            // Track play count for each song
            String title = song.get("title");
            songPlayCount.put(title, songPlayCount.getOrDefault(title, 0) + 1);
        }

        float averageBPM = (float) totalBPM / songDataList.size();

        // Find the most played song
        String mostPlayedSong = "";
        int maxPlays = 0;
        for (Map.Entry<String, Integer> entry : songPlayCount.entrySet()) {
            if (entry.getValue() > maxPlays) {
                maxPlays = entry.getValue();
                mostPlayedSong = entry.getKey();
            }
        }

        // Build insights text
        StringBuilder insights = new StringBuilder();
        insights.append("Average BPM: ").append(String.format("%.2f", averageBPM)).append("\n");
        insights.append("Peak BPM: ").append(peakBPM).append(" (").append(peakSong).append(")\n");
        insights.append("Lowest BPM: ").append(lowestBPM).append(" (").append(lowestSong).append(")\n");
        insights.append("Most Played Song: ").append(mostPlayedSong).append(" (").append(maxPlays).append(" times)\n");
        insights.append("Duration of High Intensity (> ").append(highIntensityThreshold).append(" BPM): ").append(highIntensityDuration).append(" songs\n");
        insightsTextView.setText(insights.toString());
    }


    private void loadSongsForWorkout(String workoutId) {
        dbHelper = new HeartBeatOpenHelper(getContext());

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
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f); // Adjust curve intensity
        dataSet.setDrawHighlightIndicators(true);
        dataSet.setHighLightColor(getResources().getColor(R.color.dark_md_theme_primary));


        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Customize the X-Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);

        // Customize the Left Y-Axis (BPM values)
        YAxis leftYAxis = lineChart.getAxisLeft();
        leftYAxis.setAxisMinimum(minBpm - 5); // Set the minimum value slightly below the minBpm
        leftYAxis.setAxisMaximum(maxBpm + 5); // Set the maximum value slightly above the maxBpm
        leftYAxis.setDrawLabels(true); // Show labels on the left axis
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setGridColor(getResources().getColor(R.color.dark_md_theme_onSecondaryFixedVariant));

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

        lineChart.getDescription().setText("Heart Rate Analysis");
        lineChart.getDescription().setTextSize(16f);
        lineChart.getDescription().setTextColor(getResources().getColor(R.color.dark_md_theme_background_highContrast));
        lineChart.setDrawBorders(true);
        lineChart.setBorderWidth(1f);
        lineChart.setBorderColor(getResources().getColor(R.color.dark_md_theme_inverseOnSurface_mediumContrast));
        lineChart.setScaleEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setPinchZoom(true);

        Legend legend = lineChart.getLegend();
        legend.setTextSize(14f);
        legend.setTextColor(getResources().getColor(R.color.dark_md_theme_background_highContrast));
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);


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
