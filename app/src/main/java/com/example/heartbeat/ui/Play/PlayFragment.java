package com.example.heartbeat.ui.Play;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.heartbeat.R;
import com.example.heartbeat.StepAppOpenHelper;
import com.example.heartbeat.SoundManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PlayFragment extends Fragment {

    private StepAppOpenHelper databaseHelper;
    private SoundManager soundManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_play, container, false);
        // Fake heart rate (for now)
//        int fakeHeartRate = new Random().nextInt(80) + 60; // Random BPM between 60-140
        int fakeHeartRate = 120; // BPM = 120
        TextView heartRateText = root.findViewById(R.id.heart_rate_text);
        heartRateText.setText("Fake Heart Rate: " + fakeHeartRate + " BPM");

        soundManager = SoundManager.getInstance(getContext());

        // Initialize the UI elements
        TextView songTitle = root.findViewById(R.id.song_title);
        TextView songArtist = root.findViewById(R.id.song_artist);
        TextView songBPM = root.findViewById(R.id.song_bpm);
        Button playPauseButton = root.findViewById(R.id.play_pause_button);

        // Find a button and set its click listener
        Button playButton = root.findViewById(R.id.play_button);
        playButton.setOnClickListener(v -> {
            soundManager.playRandomSong(fakeHeartRate);

            // Update the UI with the song details
            songTitle.setText(soundManager.getCurrentSongTitle());
            songArtist.setText(soundManager.getCurrentSongArtist());
            songBPM.setText("BPM: " + soundManager.getCurrentSongBPM());

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
