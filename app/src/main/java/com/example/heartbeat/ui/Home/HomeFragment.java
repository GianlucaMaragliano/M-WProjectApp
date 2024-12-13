package com.example.heartbeat.ui.Home;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import com.example.heartbeat.HeartRateGenerator;
import com.example.heartbeat.R;
import com.example.heartbeat.SoundManager;
import com.example.heartbeat.databinding.FragmentWorkoutBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private StepCounterListener sensorListener;
    private SensorManager sensorManager;
    private Sensor accSensor;

    double totalDistance = 0; // Total distance in meters

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
            soundManager.saveWorkoutSong(currentWorkoutId, averageBpm, totalDistance);
            totalDistance = 0; // Reset total distance
            sensorListener.setTotalDistance(0); // Reset total distance in sensor listener
            soundManager.playRandomWorkoutSong(targetBpm, currentWorkoutId);
            songArtist.setText(soundManager.getCurrentSongArtist());
            songTitle.setText(soundManager.getCurrentSongTitle());
            startProgressUpdate();
        });

        // Initialize the step counter sensor
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        sensorListener = new StepCounterListener(this);
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
            soundManager.saveWorkoutSong(currentWorkoutId, averageBpm, totalDistance);
            totalDistance = 0; // Reset total distance
            sensorListener.setTotalDistance(0); // Reset total distance in sensor listener

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

        // Start the heart rate generator
        heartRateGenerator.startGenerating();

        // Start the step counter sensor
        sensorManager.registerListener(sensorListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        totalDistance = 0; // Reset total distance
        sensorListener.setTotalDistance(0); // Reset total distance in sensor listener

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
        soundManager.saveWorkoutSong(currentWorkoutId, averageBpm, totalDistance);
        totalDistance = 0; // Reset total distance
        sensorListener.setTotalDistance(0); // Reset total distance in sensor listener

        soundManager.stopSound();

        heartRateGenerator.stopGenerating();

        sensorManager.unregisterListener(sensorListener);

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

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
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

class StepCounterListener implements SensorEventListener {

    private long lastSensorUpdate = 0;
    private final ArrayList<Integer> accSeries = new ArrayList<>();
    private final ArrayList<String> timestampsSeries = new ArrayList<>();
    private double accMag = 0;
    private int lastAddedIndex = 1;
    private final int stepThreshold = 6;

    private String timestamp;

    private final HomeFragment homeFragment;

    private double totalDistance = 0; // Total distance in meters
    private final double STEP_LENGTH = 1.07; // Average step length in meters

    public StepCounterListener(HomeFragment homeFragment) {
        this.homeFragment = homeFragment;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            long currentTimeInMilliSecond = System.currentTimeMillis();
            if ((currentTimeInMilliSecond - lastSensorUpdate) > 1000) {
                lastSensorUpdate = currentTimeInMilliSecond;
                String sensorRawValues = "x = " + x + ", y = " + y + ", z = " + z;
                Log.d("Acc. Event", "Last sensor update at " + currentTimeInMilliSecond + " " + sensorRawValues);
            }
            accMag = Math.sqrt(x * x + y * y + z * z);
            accSeries.add((int) accMag);
            // Get timestamp
            timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
                    .format(currentTimeInMilliSecond);
            timestampsSeries.add(timestamp);
            peakDetection(); // Call peak detection logic
        }
    }

    private void peakDetection() {
        int windowSize = 20;

        // Ensure enough data for processing
        int currentSize = accSeries.size();
        if (currentSize - lastAddedIndex < windowSize) {
            return;
        }
        // Get the data window
        List<Integer> valuesInWindow = accSeries.subList(lastAddedIndex, currentSize);
        List<String> timePointList = timestampsSeries.subList(lastAddedIndex, currentSize);
        lastAddedIndex = currentSize;
        for (int i = 1; i < valuesInWindow.size() - 1; i++) {
            int forwardSlope = valuesInWindow.get(i + 1) - valuesInWindow.get(i);
            int downwardSlope = valuesInWindow.get(i) - valuesInWindow.get(i - 1);
            if (forwardSlope < 0 && downwardSlope > 0 && valuesInWindow.get(i) > stepThreshold) {
                // Update total distance
                homeFragment.setTotalDistance(totalDistance += STEP_LENGTH);
                // Log and update UI
                Log.d("ACC STEPS", "Distance: " + totalDistance + " meters");
//                homeFragment.updateRunDistance(totalDistance); // Notify fragment to update distance
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("Sensor", "Accuracy changed");
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }
}
