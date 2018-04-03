package com.example.android.nepalfishkeepers;

import com.google.firebase.messaging.RemoteMessage;
import com.hypertrack.lib.HyperTrackFirebaseMessagingService;

import static com.hypertrack.lib.internal.transmitter.utils.Constants.HT_SDK_NOTIFICATION_KEY;

public class MyFirebaseMessagingService extends HyperTrackFirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData() != null) {
            String sdkNotification = remoteMessage.getData().get(HT_SDK_NOTIFICATION_KEY);
            if (sdkNotification != null && sdkNotification.equalsIgnoreCase("true")) {
                /**
                 * HyperTrack notifications are received here
                 * Dont handle these notifications. This might end up in a crash
                 */
            }
        }
        // Handle your notifications here.

    }


}
