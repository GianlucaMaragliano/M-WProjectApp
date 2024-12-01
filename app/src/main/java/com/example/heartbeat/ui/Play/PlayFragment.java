package com.example.heartbeat.ui.Play;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.heartbeat.R;
import com.example.heartbeat.HeartBeatOpenHelper;
import com.example.heartbeat.SoundManager;

public class PlayFragment extends Fragment {

    private HeartBeatOpenHelper databaseHelper;
    private SoundManager soundManager;

    // Method to convert heart rate to suggested BPM range
    private int calculateTargetBPM(int heartRate) {
        // Example conversion: Use a linear relationship for now
        if (heartRate < 100) return (int) (heartRate * 1.5);
        if (heartRate < 140) return (int) (heartRate * 1.2);
        return (int) (heartRate * 1.1); // Scale down for high heart rates
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_play, container, false);
        // Fake heart rate (for now)
//        int fakeHeartRate = new Random().nextInt(80) + 60; // Random BPM between 60-140
        int fakeHeartRate = 120; // BPM = 120
        int targetBPM = calculateTargetBPM(fakeHeartRate);
        TextView heartRateText = root.findViewById(R.id.heart_rate_text);
        heartRateText.setText("Heart Rate: " + fakeHeartRate + " BPM | Target BPM: " + targetBPM);

        soundManager = SoundManager.getInstance(getContext());

        // Initialize the UI elements
        TextView songTitle = root.findViewById(R.id.song_title);
        TextView songArtist = root.findViewById(R.id.song_artist);
        TextView songBPM = root.findViewById(R.id.song_bpm);
        Button playPauseButton = root.findViewById(R.id.play_pause_button);

        // Find a button and set its click listener
        Button playButton = root.findViewById(R.id.play_button);
        playButton.setOnClickListener(v -> {
            soundManager.playRandomSong(targetBPM);

            // Update the UI with the song details
            String titleSong = soundManager.getCurrentSongTitle();
            String artistSong = soundManager.getCurrentSongArtist();
            int bpmSong = soundManager.getCurrentSongBPM();

            songTitle.setText(titleSong);
            songArtist.setText(artistSong);
            songBPM.setText("BPM: " + bpmSong);
            // Enable the play/pause button after starting the song
            playPauseButton.setVisibility(View.VISIBLE);
        });

        // Play/Pause Button functionality
        playPauseButton.setOnClickListener(v -> {
            soundManager.togglePlayPause();
            if (soundManager.isPlaying()) {
                playPauseButton.setText("Pause");
            } else {
                playPauseButton.setText("Play");
            }
        });

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        soundManager.stopSound();
    }
}
