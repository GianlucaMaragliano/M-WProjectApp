package com.example.heartbeat.ui.Play;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.heartbeat.R;
import com.example.heartbeat.SoundManager;

public class PlayFragment extends Fragment {

    private SoundManager soundManager;
    private ProgressBar songProgress;
    private Handler progressHandler;
    private Runnable progressRunnable;

    private TextView heartRateText;
    private float currentHeartRate = -1; // Placeholder for heart rate

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_play, container, false);

        int fakeHeartRate = 120; // BPM = 120

        int targetBPM = calculateTargetBPM(fakeHeartRate);
        TextView heartRateText = root.findViewById(R.id.heart_rate_text);
        heartRateText.setText("Heart Rate: " + fakeHeartRate + " BPM | Target BPM: " + targetBPM);

        // Initialize heart rate text view
        heartRateText = root.findViewById(R.id.heart_rate_text);
        heartRateText.setText("Waiting for heart rate...");

        // Initialize SoundManager
        soundManager = SoundManager.getInstance(getContext());

        // Initialize UI elements
        TextView songTitle = root.findViewById(R.id.song_title);
        TextView songArtist = root.findViewById(R.id.song_artist);
        TextView songBPM = root.findViewById(R.id.song_bpm);
        Button playPauseButton = root.findViewById(R.id.play_pause_button);
        songProgress = root.findViewById(R.id.song_progress);

        progressHandler = new Handler();

        // Play button functionality
        Button playButton = root.findViewById(R.id.play_button);
        playButton.setOnClickListener(v -> {
//            if (currentHeartRate > 0) {
//                soundManager.playRandomSong((int)currentHeartRate);
            soundManager.playRandomSong(targetBPM);

                // Update the UI with song details
                songTitle.setText(soundManager.getCurrentSongTitle());
                songArtist.setText(soundManager.getCurrentSongArtist());
                songBPM.setText("BPM: " + soundManager.getCurrentSongBPM());

                // Enable play/pause button and reset progress
                playPauseButton.setVisibility(View.VISIBLE);
                songProgress.setProgress(0);

                // Start progress updates
                startProgressUpdate();

                soundManager.setOnCompletionListener(mp -> {
                    playPauseButton.setText("Pause"); // Reset Play button text
                    stopProgressUpdate();
                    songProgress.setProgress(0);

                    Toast.makeText(getContext(), "Song finished! Average BPM: " + currentHeartRate, Toast.LENGTH_SHORT).show();
                });
//            } else {
//                Toast.makeText(getContext(), "Heart rate not received yet!", Toast.LENGTH_SHORT).show();
//            }
        });

        // Play/Pause Button functionality
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

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register the BroadcastReceiver for heart rate updates
        IntentFilter filter = new IntentFilter("com.example.heartbeat.HEART_RATE_UPDATED");
        requireContext().registerReceiver(heartRateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the BroadcastReceiver
        requireContext().unregisterReceiver(heartRateReceiver);
    }

    private final BroadcastReceiver heartRateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("heart_rate")) {
                Log.d("PlayFragment", "Heart rate broadcast received: " + intent.getFloatExtra("heart_rate", -1));
                currentHeartRate = intent.getFloatExtra("heart_rate", -1);
                heartRateText.setText("Heart Rate: " + currentHeartRate + " BPM");
            }
        }
    };

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

    // Method to convert heart rate to suggested BPM range
    private int calculateTargetBPM(int heartRate) {
        // Example conversion: Use a linear relationship for now
        if (heartRate < 100) return (int) (heartRate * 1.5);
        if (heartRate < 140) return (int) (heartRate * 1.2);
        return (int) (heartRate * 1.1); // Scale down for high heart rates
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopProgressUpdate();
        songProgress.setProgress(0);
        soundManager.stopSound();
    }
}
