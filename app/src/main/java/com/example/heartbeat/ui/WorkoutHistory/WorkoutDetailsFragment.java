package com.example.heartbeat.ui.WorkoutHistory;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.heartbeat.R;

public class WorkoutDetailsFragment extends Fragment {
    private String workoutId;
    private TextView workoutDetailsTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_workout_details, container, false);

        // Initialize TextView to show workout details
        workoutDetailsTextView = rootView.findViewById(R.id.textViewWorkoutDetails);

        // Retrieve the workoutId passed from HistoryFragment
        if (getArguments() != null) {
            workoutId = getArguments().getString("workoutId");
            Log.d("WorkoutDetailsFragment", "Received workoutId: " + workoutId);
        }

        // Display workout details (replace with actual logic)
        displayWorkoutDetails();

        return rootView;
    }

    private void displayWorkoutDetails() {
        // Example logic to display workout details
        if (workoutId != null) {
            workoutDetailsTextView.setText("Details for Workout ID: " + workoutId);
        } else {
            workoutDetailsTextView.setText("No workout details available.");
        }
    }

    public static WorkoutDetailsFragment newInstance(String workoutId) {
        WorkoutDetailsFragment fragment = new WorkoutDetailsFragment();
        Bundle args = new Bundle();
        args.putString("workoutId", workoutId);
        fragment.setArguments(args);
        return fragment;
    }
}
