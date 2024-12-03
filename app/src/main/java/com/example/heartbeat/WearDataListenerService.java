package com.example.heartbeat;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class WearDataListenerService extends WearableListenerService {

    private static final String TAG = "WearDataListenerService";
    private static final String HEART_RATE_PATH = "/heart_rate_path";
    private static final String HEART_RATE_KEY = "heart_rate_key";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().equals(HEART_RATE_PATH)) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    float heartRate = dataMap.getFloat(HEART_RATE_KEY);

                    Log.d(TAG, "Received heart rate: " + heartRate);

                    // Broadcast the heart rate data
                    Intent intent = new Intent("com.example.heartbeat.HEART_RATE_UPDATED");
                    intent.putExtra("heart_rate", heartRate);
                    sendBroadcast(intent);
                }
            }
        }
    }
}
