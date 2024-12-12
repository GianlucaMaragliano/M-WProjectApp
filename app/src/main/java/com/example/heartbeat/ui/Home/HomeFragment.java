package com.example.heartbeat.ui.Home;

import java.util.List;
import java.util.UUID;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.heartbeat.HeartBeatOpenHelper;
import com.example.heartbeat.HeartRateGenerator;
import com.example.heartbeat.R;
import com.example.heartbeat.SoundManager;
import com.example.heartbeat.databinding.FragmentWorkoutBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends Fragment {

    private FragmentWorkoutBinding binding;

    TextView hearRateView;

    private SoundManager soundManager;
    private ProgressBar songProgress;
    private Handler progressHandler;
    private Runnable progressRunnable;

    private Handler timerHandler; // Handler for the timer
    private Runnable timerRunnable; // Runnable for updating timer
    private int timerSeconds = 0; // Timer counter in seconds

    private boolean workoutStarted = false;
    private int targetBpm;
    private double averageBpm = 0;
    private int heartRate = 0;
    private String currentWorkoutId; // Track current workout ID

    TextView songTitle;
    TextView songArtist;
    TextView timerView;

    FloatingActionButton playPauseButton;
    FloatingActionButton nextSongButton;
    MaterialButton startButton;

    HeartRateGenerator heartRateGenerator;
    HeartRateListener heartRateListener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        int fakeHeartRate = 120; // BPM = 120
        heartRate = fakeHeartRate;
        targetBpm = calculateTargetBPM(fakeHeartRate);

        // Initialize the heart rate generator
        heartRateGenerator = new HeartRateGenerator(fakeHeartRate);
        heartRateListener = new HeartRateListener(this);
        heartRateGenerator.addListener(heartRateListener);

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

        startButton = root.findViewById(R.id.start_button);
        playPauseButton = root.findViewById(R.id.play_pause_button);
        nextSongButton = root.findViewById(R.id.next_button);

        startButton.setOnClickListener(v-> {
            Log.d("Button", "Start button clicked");
            handleWorkout();
        });

        playPauseButton.setOnClickListener(v -> {
            soundManager.togglePlayPause();
            if (soundManager.isPlaying()) {
                playPauseButton.setImageDrawable(
                        ContextCompat.getDrawable(
                                requireContext(), R.drawable.media_player_ui_button_pause_svgrepo_com));
                startProgressUpdate();
            } else {
                playPauseButton.setImageDrawable(
                        ContextCompat.getDrawable(
                                requireContext(), R.drawable.media_player_ui_button_play_svgrepo_com));
                stopProgressUpdate();
            }
        });

        nextSongButton.setOnClickListener(v -> {
            soundManager.saveWorkoutSong(currentWorkoutId, averageBpm);
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
            soundManager.saveWorkoutSong(currentWorkoutId, averageBpm);
            Log.d("MediaPlayer", "Song finished!");
            stopProgressUpdate();

            targetBpm = calculateTargetBPM(heartRate);
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
        if (workoutStarted) return;
        workoutStarted = true;

        heartRateGenerator.startGenerating();

        currentWorkoutId = UUID.randomUUID().toString();
        Log.d("Workout", "Workout started with ID: " + currentWorkoutId);

        soundManager.playRandomWorkoutSong(targetBpm, currentWorkoutId);

        // Update UI
        songArtist.setText(soundManager.getCurrentSongArtist());
        songTitle.setText(soundManager.getCurrentSongTitle());

        crossfadeView(songArtist, null, null);
        crossfadeView(songTitle, null, null);
        crossfadeView(playPauseButton, null, null);
        crossfadeView(nextSongButton, null, null);

        songProgress.setProgress(0);
        startProgressUpdate();

        startTimer(); // Start the timer
        updateSongOnFinish();
    }

    private void handleWorkout() {
        if (workoutStarted) {
            startButton.setText(R.string.start_text);
            int icon = R.drawable.baseline_play_circle_filled_24;
            startButton.setIconResource(icon);
            stopWorkout();
        } else {
            startButton.setText(R.string.stop_text);
            int icon = R.drawable.baseline_stop_circle_24;
            startButton.setIconResource(icon);
            startWorkout();
        }
    }

    private void stopWorkout() {
        if (!workoutStarted) return;
        workoutStarted = false;
        soundManager.saveWorkoutSong(currentWorkoutId, averageBpm);

        soundManager.stopSound();

        heartRateGenerator.stopGenerating();

        cleanFields();
        stopProgressUpdate();
        stopTimer();
    }

    private void crossfadeView(View viewToShow, View viewToHide, Runnable onHideComplete) {
        if (viewToHide != null && viewToHide.getVisibility() == View.VISIBLE) {
            viewToHide.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                viewToHide.setVisibility(View.INVISIBLE);
                if (onHideComplete != null) onHideComplete.run();
            }).start();
        }

        if (viewToShow != null && viewToShow.getVisibility() != View.VISIBLE) {
            viewToShow.setVisibility(View.VISIBLE);
            viewToShow.setAlpha(0f);
            viewToShow.animate().alpha(1f).setDuration(300).start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        stopWorkout();
    }

    public double getAverageBpm() {
        return averageBpm;
    }

    public void setAverageBpm(double averageBpm) {
        this.averageBpm = averageBpm;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }
}

class HeartRateListener implements HeartRateGenerator.HeartRateListener{

    private HomeFragment homeFragment;

    public HeartRateListener(HomeFragment homeFragment) {
        this.homeFragment = homeFragment;
    }

    @Override
    public void onHeartRateChanged(float heartRate) {
        // Handle the heart rate data
        Log.d("MainActivity", "Received heart rate: " + heartRate);
        double averageBpm = homeFragment.getAverageBpm();
        int bpm = ((int) heartRate);
        averageBpm = (averageBpm + bpm) / 2.0;
        homeFragment.setAverageBpm(averageBpm);
        homeFragment.hearRateView.setText(bpm + " BPM");
        homeFragment.setHeartRate(bpm);
    }
}