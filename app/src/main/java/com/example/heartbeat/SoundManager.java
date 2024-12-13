package com.example.heartbeat;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class SoundManager {

    private static SoundManager soundManagerInstance = null;
    private MediaPlayer mediaPlayer;
    private final Context context;
    private final HeartBeatOpenHelper databaseHelper;

    private MediaPlayer.OnCompletionListener onCompletionListener;

    // To track the current song details
    private String currentSongId;
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

    public void playRandomWorkoutSong(int heartrate, String workoutId) {
        playRandomSong(heartrate, workoutId);
    }

    public void saveWorkoutSong(String workoutId, double avgBpm, double runDistance) {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String timeStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());  // Gets current time in the format "23:59:59"

        Log.d("WorkoutSong", "Saving workout song: " + currentSongTitle + " - " + currentSongArtist + " - " + avgBpm + " - " + runDistance);

        databaseHelper.insertWorkoutSong(workoutId, timeStr, dateStr, currentSongId, avgBpm, runDistance);
    }

    public void playRandomSong(int heartRate, String workoutId) {
        // Query songs based on BPM range
        List<Map<String, String>> songs = databaseHelper.getSongsByBpmRange(heartRate, heartRate + 10);
        if (songs.isEmpty()) {
            Toast.makeText(context, "No songs found for current BPM range!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (workoutId != null) {
            List<Map<String, String>> workoutSongs = databaseHelper.getWorkoutSongsByWorkoutId(workoutId);
            // Filter songs that are in the workout history
            songs.removeIf(song -> {
                for (Map<String, String> workoutSong : workoutSongs) {
                    if (song.get("title").equals(workoutSong.get("title"))) {
                        return true;
                    }
                }
                return false;
            });
        }

        if (songs.isEmpty())
            songs = databaseHelper.getSongsByBpmRange(heartRate, heartRate + 10);

        // Pick a random song
        Random random = new Random();
        int randomIndex = random.nextInt(songs.size());
        Log.d("RandomIndex", "Index: " + randomIndex);
        Map<String, String> randomSong = songs.get(randomIndex);

        currentSongId = randomSong.get("id");
        currentSongTitle = randomSong.get("title");
        currentSongArtist = randomSong.get("artist");
        currentSongBPM = Integer.parseInt(randomSong.get("bpm"));

        playSong("SoundLib/" + currentSongTitle + ".m4a");
    }

    private void playSong(String file_name) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
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

            // Detect when the song finishes
            mediaPlayer.setOnCompletionListener(mp -> {
                Toast.makeText(context, "Song finished!", Toast.LENGTH_SHORT).show();
            });
        } catch (final Exception e) {
            e.printStackTrace();
            Log.e("MediaPlayer", "Error occurred while playing song: " + file_name, e);
        }
        mediaPlayer.start();
        Log.d("AudioPath", "Playing: " + file_name);
    }


    public void togglePlayPause() {
        if (mediaPlayer == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    public void stopSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public String getCurrentSongId() {
        return currentSongId;
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

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        this.onCompletionListener = listener;
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(onCompletionListener);
        }
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }
}