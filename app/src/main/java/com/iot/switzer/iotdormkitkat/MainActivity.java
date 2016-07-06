package com.iot.switzer.iotdormkitkat;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.iot.switzer.iotdormkitkat.presets.WatchPresetButton;

import java.util.HashMap;

public class MainActivity extends Activity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Button.OnClickListener {

    private HashMap<String, WatchPresetButton> buttonMap;

    private static final String KEY_HEADER = "com.switzer.iotdorm.";
    private static final String TABLE_HEADER = "/presets";

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("WEARABLE:WATCH", "onCreate()");
        buttonMap = new HashMap<>();

        setContentView(R.layout.round_activity_main);
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(googleApiClient, this);
        googleApiClient.disconnect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d("WEARABLE:WATCH", "Data Change!");
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                DataItem item = event.getDataItem();
                Log.d("WEARABLE:WATCH", "URI:" + item.getUri().getPath());
                if (item.getUri().getPath().compareTo(TABLE_HEADER) == 0) {

                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    updateButtonFromDataMap(dataMap);

                } else if (event.getType() == DataEvent.TYPE_DELETED) {

                }
            }
        }
        dataEventBuffer.release();
    }


    public void updateButtonFromDataMap(DataMap map) {
        for (String key : map.keySet()) {
            Log.d("WEARABLE:WATCH", "Key:" + key);
            boolean enabled = map.getBoolean(key);
            if (buttonMap.get(key) == null) {
                WatchPresetButton newButton = new WatchPresetButton(getApplicationContext(), fromKeySyntax(key));
                newButton.setOnClickListener(this);
                LinearLayout l = (LinearLayout) findViewById(R.id.presetScrollViewLinearLayout);
                l.addView(newButton);
                buttonMap.put(key, newButton);
            }

            if (enabled)
                buttonMap.get(key).enable();
            else
                buttonMap.get(key).disable();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("WEARABLE:WATCH", "Connected!!");
        Wearable.DataApi.addListener(googleApiClient, this);

        /**
         * TODO: Make the watch query for all currently available
         * presets.
         */

        /*PendingResult<DataItemBuffer> buf = Wearable.DataApi.getDataItems(googleApiClient);
        buf.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(@NonNull DataItemBuffer dataItems) {
                for (DataItem i : dataItems)
                    updateButtonFromDataMap(DataMapItem.fromDataItem(i).getDataMap());
                dataItems.release();
            }
        });*/
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("WEARABLE:WATCH", "Connection Suspended!!");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("WEARABLE:WATCH", "Connection Failed!");
        LinearLayout l = (LinearLayout) findViewById(R.id.presetScrollViewLinearLayout);
        l.addView(new WatchPresetButton(l.getContext(), String.valueOf(connectionResult.getErrorCode())));


    }

    private static String toKeySyntax(String s) {
        return (KEY_HEADER + s);
    }

    private static String fromKeySyntax(String s) {
        return s.substring(KEY_HEADER.length(), s.length());
    }


    public void sendUpdate(String name, boolean enabled) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(TABLE_HEADER);

        putDataMapReq.getDataMap().putBoolean(toKeySyntax(name), enabled);


        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
    }

    @Override
    public void onClick(View v) {
        Log.d("WATCH", "Click!");
        sendUpdate(((WatchPresetButton) (v)).getName(), !((WatchPresetButton) (v)).isPresetEnabled());
    }
}
