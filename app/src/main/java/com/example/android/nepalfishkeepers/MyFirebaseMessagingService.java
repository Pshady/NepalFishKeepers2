package com.example.android.nepalfishkeepers;

import android.app.PendingIntent;
import android.content.Intent;

import com.google.firebase.messaging.RemoteMessage;
import com.hypertrack.lib.HyperTrackFirebaseMessagingService;
import com.hypertrack.lib.internal.transmitter.utils.Constants;



public class MyFirebaseMessagingService extends HyperTrackFirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sdkNotification = remoteMessage.getData().get(Constants.HT_SDK_NOTIFICATION_KEY);
        if (sdkNotification != null && sdkNotification.equalsIgnoreCase("true")) {
            /**
             * HyperTrack notifications are received here
             * Dont handle these notifications. This might end up in a crash
             */
            return;
        }

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        NotificationType type = NotificationType.valueOf(remoteMessage.getData().get("type"));
        String postKey;
        Intent intent;
        PendingIntent pendingIntent;
        switch (type) {
            case ACTION:
                postKey = remoteMessage.getData().get("postKey");
                String collectionId = remoteMessage.getData().get("collectionId");
                intent = new Intent(getApplicationContext(), TradeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("postKey", postKey);
                intent.putExtra("collectionId", collectionId);
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                MyNotificationManager.getInstance(getApplicationContext())
                        .displayNotification(title, message, pendingIntent);
                break;
            case POST_CREATED:
                postKey = remoteMessage.getData().get("postKey");
                intent = new Intent(getApplicationContext(), SingleNfkActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("postKey", postKey);
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                MyNotificationManager.getInstance(getApplicationContext())
                        .displayNotification(title, message, pendingIntent);
                break;
            case TRADE_REQUEST:
                postKey = remoteMessage.getData().get("postKey");
                intent = new Intent(getApplicationContext(), SingleNfkActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("postKey", postKey);
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                MyNotificationManager.getInstance(getApplicationContext())
                        .displayNotification(title, message, pendingIntent);
                break;
            case GENERAL:
                MyNotificationManager.getInstance(getApplicationContext())
                        .displayNotification(title, message);
        }
    }
}
