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
import com.example.heartbeat.R;
import com.example.heartbeat.SoundManager;
import com.example.heartbeat.databinding.FragmentWorkoutBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    FloatingActionButton playPauseButton;
    FloatingActionButton nextSongButton;
    MaterialButton startButton;

    private DataClient dataClient;
    private HeartRateListener heartRateListener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Task<List<Node>> connectedNodes = Wearable.getNodeClient(requireContext()).getConnectedNodes();
        connectedNodes.addOnSuccessListener(nodes -> {
            for (Node node : nodes) {
                if (node.isNearby()) {
                    Log.d("Wearable", "Connected to wearable node: " + node.getId());
                } else {
                    Log.d("Wearable", "No wearable connected.");
                }
            }
        });

        // Bind heart rate data
        heartRateListener = new HeartRateListener(this);
        dataClient = Wearable.getDataClient(requireContext());
        dataClient.addListener(heartRateListener);

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
        workoutStarted = false;
        soundManager.stopSound();
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

        dataClient.removeListener(heartRateListener);

        cleanFields();
        stopProgressUpdate();
        soundManager.stopSound();
        stopTimer();
    }
}

class HeartRateListener implements DataClient.OnDataChangedListener {

    private HomeFragment homeFragment;

    public HeartRateListener(HomeFragment homeFragment) {
        this.homeFragment = homeFragment;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (Log.isLoggable("TUZIA", Log.DEBUG)) {
            Log.d("TUZIA", "onDataChanged: " + dataEvents);
        }
        // Handle the data change event
        Log.d("HeartRateListener", "Data changed event received");

//        for (DataEvent event : dataEventBuffer) {
//            if (event.getType() == DataEvent.TYPE_CHANGED) {
//                DataItem item = event.getDataItem();
//                if (item.getUri().getPath().equals("/heart_rate_path")) {
//                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
//                    float heartRate = dataMap.getFloat("heart_rate_key");
//                    Log.d("HeartRateListener", "Heart rate received: " + heartRate);
//
//                }
//            }
//        }
        Log.d("HeartRateListener", "onDataChanged triggered");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                Log.d("HeartRateListener", "Path: " + item.getUri().getPath());
            }
        }
    }
}