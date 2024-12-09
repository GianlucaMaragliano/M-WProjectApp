package com.example.heartbeat.ui.WorkoutHistory;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.heartbeat.R;

public class CreateExerciseActivity extends AppCompatActivity {
    private String workoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_exercise);

        // Get the workoutId passed from the previous activity
        workoutId = getIntent().getStringExtra("workoutId");

        // Set up your views, such as a list of exercises and the option to select and associate them with songs
    }
}

