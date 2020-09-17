package com.example.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.util.Objects;

public class WifiStateChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int extra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0 );

            if ( extra == WifiManager.WIFI_STATE_ENABLED ) {
                Toast.makeText(context, "Wifi connection establish", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Wifi connection lost", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
