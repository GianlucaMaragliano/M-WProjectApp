package com.example.heartbeat.ui.Statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.heartbeat.R;

public class StatisticsFragment extends Fragment {

    private TextView totalStepsTextView;
    private TextView averageHeartRateTextView;
    private TextView averageWorkoutTimeTextView;
    private TextView mostStepsDayTextView;
    private TextView workoutTimeWeekTextView;
    private TextView mostPlayedSongTextView;
    private TextView mostSkippedSongTextView;
    private TextView overallMusicPlayedTextView;
    private TextView overallWorkoutTimeTextView;
    private TextView weeklyStepsTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Initialize TextViews
        totalStepsTextView = rootView.findViewById(R.id.totalSteps);
        averageHeartRateTextView = rootView.findViewById(R.id.averageHeartRate);
        averageWorkoutTimeTextView = rootView.findViewById(R.id.averageWorkoutTime);
        mostStepsDayTextView = rootView.findViewById(R.id.mostStepsDay);
        workoutTimeWeekTextView = rootView.findViewById(R.id.workoutTimeWeek);
        mostPlayedSongTextView = rootView.findViewById(R.id.mostPlayedSong);
        mostSkippedSongTextView = rootView.findViewById(R.id.mostSkippedSong);
        overallMusicPlayedTextView = rootView.findViewById(R.id.overallMusicPlayed);
        overallWorkoutTimeTextView = rootView.findViewById(R.id.overallWorkoutTime);
        weeklyStepsTextView = rootView.findViewById(R.id.weeklySteps);

        // Load the statistics
        loadStatistics();

        return rootView;
    }

    private void loadStatistics() {
        // Replace these with actual queries or calculations from your database
        int totalSteps = getTotalSteps();
        int averageHeartRate = getAverageHeartRate();
        int averageWorkoutTime = getAverageWorkoutTime();
        int mostStepsDay = getMostStepsDay();
        int workoutTimeWeek = getWorkoutTimeWeek();
        String mostPlayedSong = getMostPlayedSong();
        String mostSkippedSong = getMostSkippedSong();
        int overallMusicPlayed = getOverallMusicPlayed();
        int overallWorkoutTime = getOverallWorkoutTime();
        int weeklySteps = getWeeklySteps();

        // Set the data in TextViews
        totalStepsTextView.setText("Total Steps: " + totalSteps);
        averageHeartRateTextView.setText("Average Heart Rate: " + averageHeartRate + " bpm");
        averageWorkoutTimeTextView.setText("Average Workout Time: " + averageWorkoutTime + " min");
        mostStepsDayTextView.setText("Most Steps in a Day: " + mostStepsDay);
        workoutTimeWeekTextView.setText("Workout Time This Week: " + workoutTimeWeek + " min");
        mostPlayedSongTextView.setText("Most Played Song: " + mostPlayedSong);
        mostSkippedSongTextView.setText("Most Skipped Song: " + mostSkippedSong);
        overallMusicPlayedTextView.setText("Overall Music Played: " + overallMusicPlayed + " songs");
        overallWorkoutTimeTextView.setText("Overall Workout Time: " + overallWorkoutTime + " min");
        weeklyStepsTextView.setText("Weekly Steps: " + weeklySteps);
    }

    // Add database or in-memory logic for each of these methods

    private int getTotalSteps() {
        // Example: Replace with database query
        return 50000;
    }

    private int getAverageHeartRate() {
        // Example: Replace with database query
        return 75;
    }

    private int getAverageWorkoutTime() {
        // Example: Replace with database query
        return 45;
    }

    private int getMostStepsDay() {
        // Example: Replace with database query
        return 8000;
    }

    private int getWorkoutTimeWeek() {
        // Example: Replace with database query
        return 200;
    }

    private String getMostPlayedSong() {
        // Example: Replace with database query
        return "Birds of a Feather";
    }

    private String getMostSkippedSong() {
        // Hardcoded as per requirement
        return "Not like us";
    }

    private int getOverallMusicPlayed() {
        // Example: Replace with database query
        return 50;
    }

    private int getOverallWorkoutTime() {
        // Example: Replace with database query
        return 600;
    }

    private int getWeeklySteps() {
        // Example: Replace with database query
        return 35000;
    }
}