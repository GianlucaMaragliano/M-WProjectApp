package com.example.heartbeat.ui.Play;

import android.os.Bundle;
import android.os.Handler;
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
import com.example.heartbeat.HeartBeatOpenHelper;
import com.example.heartbeat.SoundManager;

public class PlayFragment extends Fragment {

    private HeartBeatOpenHelper databaseHelper;
    private SoundManager soundManager;
    private ProgressBar songProgress;
    private Handler progressHandler;
    private Runnable progressRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_play, container, false);

        int fakeHeartRate = 120; // BPM = 120
        TextView heartRateText = root.findViewById(R.id.heart_rate_text);
        heartRateText.setText("Fake Heart Rate: " + fakeHeartRate + " BPM");

        soundManager = SoundManager.getInstance(getContext());

        // Initialize the UI elements
        TextView songTitle = root.findViewById(R.id.song_title);
        TextView songArtist = root.findViewById(R.id.song_artist);
        TextView songBPM = root.findViewById(R.id.song_bpm);
        Button playPauseButton = root.findViewById(R.id.play_pause_button);
        songProgress = root.findViewById(R.id.song_progress);

        progressHandler = new Handler();

        // Play button functionality
        Button playButton = root.findViewById(R.id.play_button);
        playButton.setOnClickListener(v -> {
            soundManager.playRandomSong(fakeHeartRate);

            // Update the UI with the song details
            songTitle.setText(soundManager.getCurrentSongTitle());
            songArtist.setText(soundManager.getCurrentSongArtist());
            songBPM.setText("BPM: " + soundManager.getCurrentSongBPM());

            // Enable play/pause button and reset progress
            playPauseButton.setVisibility(View.VISIBLE);
            songProgress.setProgress(0);

            // Start progress updates
            startProgressUpdate();

            soundManager.setOnCompletionListener(mp -> {
                playPauseButton.setText("Play"); // Reset Play button text
                Toast.makeText(getContext(), "Song finished!", Toast.LENGTH_SHORT).show();
                // Stop any progress tracking if added
                stopProgressUpdate();
                songProgress.setProgress(0);
            });
        });

        // Play/Pause Button functionality
        playPauseButton.setOnClickListener(v -> {
            soundManager.togglePlayPause();
            if (soundManager.isPlaying()) {
                playPauseButton.setText("Pause");
                startProgressUpdate(); // Resume progress update when song is resumed
            } else {
                playPauseButton.setText("Play");
                stopProgressUpdate(); // Pause progress update when song is paused
            }
        });

        return root;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopProgressUpdate();
        songProgress.setProgress(0);
        soundManager.stopSound();
    }
}
