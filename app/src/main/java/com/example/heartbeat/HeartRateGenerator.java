package com.example.heartbeat;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeartRateGenerator {

    private final List<HeartRateListener> listeners = new ArrayList<>();
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private float currentHeartRate;

    public HeartRateGenerator(float initialHeartRate) {
        this.currentHeartRate = initialHeartRate;
    }

    public void startGenerating() {
        if (isRunning) return;
        isRunning = true;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;

                // Slightly adjust the heart rate
                currentHeartRate += (random.nextFloat() * 2 - 1); // Small random adjustment
                notifyListeners(currentHeartRate);

                // Schedule the next generation after 1 second
                handler.postDelayed(this, 1000);
            }
        });
    }

    public void stopGenerating() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    // Add a listener
    public void addListener(HeartRateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    // Remove a listener
    public void removeListener(HeartRateListener listener) {
        listeners.remove(listener);
    }

    // Notify all registered listeners of new heart rate data
    private void notifyListeners(float heartRate) {
        for (HeartRateListener listener : listeners) {
            listener.onHeartRateChanged(heartRate);
        }
    }

    // Listener interface for heart rate updates
    public interface HeartRateListener {
        void onHeartRateChanged(float heartRate);
    }
}
