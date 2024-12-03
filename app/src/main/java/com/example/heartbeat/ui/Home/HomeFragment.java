package com.example.heartbeat.ui.Home;

import java.util.UUID;

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

public class HomeFragment extends Fragment {

    private FragmentWorkoutBinding binding;

    private TextView hearRateView;

    private SoundManager soundManager;
    private ProgressBar songProgress;
    private Handler progressHandler;
    private Runnable progressRunnable;

    private Handler timerHandler; // Handler for the timer
    private Runnable timerRunnable; // Runnable for updating timer
    private int timerSeconds = 0; // Timer counter in seconds

    private boolean workoutStarted = false;
    private int targetBpm;
    private String currentWorkoutId; // Track current workout ID

    TextView songTitle;
    TextView songArtist;
    TextView timerView;

    Button playPauseButton;
    Button nextSongButton;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        int fakeHeartRate = 120; // BPM = 120
        targetBpm = calculateTargetBPM(fakeHeartRate);

        hearRateView = (TextView) root.findViewById(R.id.counter);
        hearRateView.setText(fakeHeartRate + " BPM");

        timerView = root.findViewById(R.id.timer_view); // Timer TextView
        timerView.setText("00:00");

        // Initialize SoundManager
        soundManager = SoundManager.getInstance(getContext());

        // Initialize UI elements
        songTitle = root.findViewById(R.id.title_view);
        songArtist = root.findViewById(R.id.artist_view);
        songProgress = root.findViewById(R.id.progressBar);

        progressHandler = new Handler();
        timerHandler = new Handler(); // Initialize timer handler

        Button startButton = root.findViewById(R.id.start_button);
        Button stopButton = root.findViewById(R.id.stop_button);
        playPauseButton = root.findViewById(R.id.play_pause_button);
        nextSongButton = root.findViewById(R.id.next_button);

        startButton.setOnClickListener(v-> {
            Log.d("Button", "Start button clicked");
            if (workoutStarted) return;
            startWorkout();
        });

        stopButton.setOnClickListener(nv -> {
            Log.d("Button", "Stop button clicked");
            if (!workoutStarted) return;
            stopWorkout();
        });

        playPauseButton.setOnClickListener(v -> {
            soundManager.togglePlayPause();
            if (soundManager.isPlaying()) {
                playPauseButton.setText("Pause");
                startProgressUpdate();
            } else {
                playPauseButton.setText("Play");
                stopProgressUpdate();
            }
        });

        nextSongButton.setOnClickListener(v -> {
            soundManager.playRandomWorkoutSong(targetBpm, currentWorkoutId);
            songArtist.setText(soundManager.getCurrentSongArtist());
            songTitle.setText(soundManager.getCurrentSongTitle());
            startProgressUpdate();
        });

        HeartBeatOpenHelper databaseOpenHelper = new HeartBeatOpenHelper(this.getContext());
        SQLiteDatabase database = databaseOpenHelper.getWritableDatabase();

        return root;
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                timerSeconds++;
                int minutes = timerSeconds / 60;
                int seconds = timerSeconds % 60;

                timerView.setText(String.format("%02d:%02d", minutes, seconds));

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
        timerSeconds = 0;
        timerView.setText("00:00");
        cleanFields();
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
        progressHandler.removeCallbacks(progressRunnable);
    }

    private void updateSongOnFinish() {
        // Set the completion listener
        soundManager.setOnCompletionListener(mp -> {
            Log.d("MediaPlayer", "Song finished!");
            stopProgressUpdate();

            soundManager.playRandomWorkoutSong(targetBpm, currentWorkoutId);

            songArtist.setText(soundManager.getCurrentSongArtist());
            songTitle.setText(soundManager.getCurrentSongTitle());
            startProgressUpdate();

            updateSongOnFinish();
        });
    }

    private void cleanFields() {
        crossfadeView(null, playPauseButton, null);
        crossfadeView(null, nextSongButton, null);

        crossfadeView(null, songArtist, ()-> songArtist.setText(""));
        crossfadeView(null, songTitle, ()-> songTitle.setText(""));

        songProgress.setProgress(0);
    }

    private void startWorkout() {

        currentWorkoutId = UUID.randomUUID().toString();
        Log.d("Workout", "Workout started with ID: " + currentWorkoutId);

        crossfadeView(playPauseButton, null, null);
        crossfadeView(nextSongButton, null, null);

        workoutStarted = true;
        soundManager.playRandomWorkoutSong(targetBpm, currentWorkoutId);

        // Update UI
        songArtist.setText(soundManager.getCurrentSongArtist());
        songTitle.setText(soundManager.getCurrentSongTitle());

        crossfadeView(songArtist, null, null);
        crossfadeView(songTitle, null, null);

        songProgress.setProgress(0);
        startProgressUpdate();

        startTimer(); // Start the timer
        updateSongOnFinish();
    }

    private void stopWorkout() {
        workoutStarted = false;
        soundManager.stopSound();
        cleanFields();
        stopProgressUpdate();
        stopTimer();
    }

    private void crossfadeView(View viewToShow, View viewToHide, Runnable onHideComplete) {
        // Fade out the view to hide
        if (viewToHide != null && viewToHide.getVisibility() == View.VISIBLE) {
            viewToHide.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        viewToHide.setVisibility(View.INVISIBLE);
                        if (onHideComplete != null) {
                            onHideComplete.run();
                        }
                    })
                    .start();
        }

        // Fade in the view to show
        if (viewToShow != null && viewToShow.getVisibility() != View.VISIBLE) {
            viewToShow.setAlpha(0f);
            viewToShow.setVisibility(View.VISIBLE);
            viewToShow.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        cleanFields();
        stopProgressUpdate();
        soundManager.stopSound();
        stopTimer();
    }
}