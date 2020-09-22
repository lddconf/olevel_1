package com.example.weather;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class WeatherPushes extends FirebaseMessagingService {
    private String TAG = "FirebaseTAG";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        // Get updated InstanceID token.
        Log.d(TAG, "Refreshed token: " + s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //Log.d(TAG, "Message data payload: " + remoteMessage.getData().get("someKey"));
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String message = remoteMessage.getNotification().getTitle();
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

}
