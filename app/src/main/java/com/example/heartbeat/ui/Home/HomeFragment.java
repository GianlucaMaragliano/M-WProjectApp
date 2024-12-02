package com.example.heartbeat.ui.Home;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.heartbeat.HeartBeatOpenHelper;
import com.example.heartbeat.R;
import com.example.heartbeat.SoundManager;
import com.example.heartbeat.databinding.FragmentWorkoutBinding;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class HomeFragment extends Fragment {

    private FragmentWorkoutBinding binding;

    private TextView hearRateView;

    private SoundManager soundManager;
    private ProgressBar songProgress;
    private Handler progressHandler;
    private Runnable progressRunnable;

    private boolean workoutStarted = false;
    private int targetBpm;

    TextView songTitle;
    TextView songArtist;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        int fakeHeartRate = 120; // BPM = 120
        targetBpm = calculateTargetBPM(fakeHeartRate);

        hearRateView = (TextView) root.findViewById(R.id.counter);
        hearRateView.setText(fakeHeartRate + " BPM");

        // Initialize SoundManager
        soundManager = SoundManager.getInstance(getContext());

        // Initialize UI elements
        songTitle = root.findViewById(R.id.title_view);
        songArtist = root.findViewById(R.id.artist_view);
        songProgress = root.findViewById(R.id.progressBar);

        progressHandler = new Handler();

        Button startButton = root.findViewById(R.id.start_button);
        Button stopButton = root.findViewById(R.id.stop_button);

        startButton.setOnClickListener(v-> {
            Log.d("Button", "Start button clicked");
            if (workoutStarted) return;
            workoutStarted = true;

            soundManager.playRandomWorkoutSong(targetBpm);
//            soundManager.playRandomWorkoutSong(targetBpm, workoutId);

            // Update UI
            songArtist.setText(soundManager.getCurrentSongArtist());
            songTitle.setText(soundManager.getCurrentSongTitle());

            songProgress.setProgress(0);
            startProgressUpdate();

            updateSongOnFinish();
        });

        stopButton.setOnClickListener(nv -> {
            Log.d("Button", "Stop button clicked");
            if (!workoutStarted) return;
            workoutStarted = false;

            soundManager.stopSound();
            stopProgressUpdate();
        });


        HeartBeatOpenHelper databaseOpenHelper = new HeartBeatOpenHelper(this.getContext());
        SQLiteDatabase database = databaseOpenHelper.getWritableDatabase();

        return root;
    }

    // Method to convert heart rate to suggested BPM range
    public int calculateTargetBPM(int heartRate) {
        // Example conversion: Use a linear relationship for now
        if (heartRate < 100) return (int) (heartRate * 1.5);
        if (heartRate < 140) return (int) (heartRate * 1.2);
        return (int) (heartRate * 1.1); // Scale down for high heart rates
    }

    private void startProgressUpdate() {
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (soundManager.isPlaying()) {
                    int currentPosition = soundManager.getCurrentPosition();
                    int duration = soundManager.getDuration();

                    // Update progress bar
                    if (duration > 0) {
                        int progress = (int) ((currentPosition / (float) duration) * 100);
                        songProgress.setProgress(progress);
                    }

                    // Schedule the next update
                    progressHandler.postDelayed(this, 1000);
                }
            }
        };

        progressHandler.post(progressRunnable);
    }

    private void stopProgressUpdate() {
        songProgress.setProgress(0);
        progressHandler.removeCallbacks(progressRunnable);
    }

    private void updateSongOnFinish() {
        // Set the completion listener
        soundManager.setOnCompletionListener(mp -> {
            Log.d("MediaPlayer", "Song finished!");
            stopProgressUpdate();

            soundManager.playRandomWorkoutSong(targetBpm);

            songArtist.setText(soundManager.getCurrentSongArtist());
            songTitle.setText(soundManager.getCurrentSongTitle());
            startProgressUpdate();

            updateSongOnFinish();
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        stopProgressUpdate();
        soundManager.stopSound();
    }
}