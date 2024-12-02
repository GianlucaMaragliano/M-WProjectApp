package com.example.heartbeat;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

public class HeartRateReceiverActivity extends AppCompatActivity implements DataClient.OnDataChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = event.getDataItem();
                if ("/heart_rate_path".equals(dataItem.getUri().getPath())) {
                    DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                    float heartRate = dataMap.getFloat("heart_rate_key");
                    long timestamp = dataMap.getLong("timestamp");

                    Log.d("HeartRateReceiver", "Received heart rate: " + heartRate + " at " + timestamp);

                    // Handle the received heart rate data (e.g., display or store it)
                }
            }
        }
    }
}

