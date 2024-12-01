package com.example.heartbeat;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class SoundManager {

    private static SoundManager soundManagerInstance = null;
    private MediaPlayer mediaPlayer;
    private final Context context;
    private HeartBeatOpenHelper databaseHelper;

    // To track the current song details
    private String currentSongTitle;
    private String currentSongArtist;
    private int currentSongBPM;


    public static SoundManager getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (soundManagerInstance == null) {
            soundManagerInstance = new SoundManager(context.getApplicationContext());
        }

        return soundManagerInstance;
    }


    // Constructor
    private SoundManager(Context context) {
        this.context = context;
        databaseHelper = new HeartBeatOpenHelper(context);
    }


    public void playRandomSong(int heartRate) {
        // Query songs based on BPM range
        List<Map<String, String>> songs = databaseHelper.getSongsByBpmRange(heartRate, heartRate + 10);
        if (songs.isEmpty()) {
            Toast.makeText(context, "No songs found for current BPM range!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pick a random song
        Random random = new Random();
        int randomIndex = random.nextInt(songs.size());
        Log.d("RandomIndex", "Index: " + randomIndex);
        Map<String, String> randomSong = songs.get(randomIndex);

        String audioPath = randomSong.get("audioPath");
        currentSongTitle = randomSong.get("title");
        currentSongArtist = randomSong.get("artist");
        currentSongBPM = Integer.parseInt(randomSong.get("bpm"));

        playSong("SoundLib/" + randomSong.get("title") + ".m4a");

    }

    private void playSong(String file_name) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            AssetFileDescriptor afd = context.getAssets().openFd(file_name);
            mediaPlayer.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength());
            afd.close();
            mediaPlayer.prepare();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
        Log.d("AudioPath", "Playing: " + file_name);
    }


    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        }
    }

    public void stopSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public String getCurrentSongTitle() {
        return currentSongTitle;
    }

    public String getCurrentSongArtist() {
        return currentSongArtist;
    }

    public int getCurrentSongBPM() {
        return currentSongBPM;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
}