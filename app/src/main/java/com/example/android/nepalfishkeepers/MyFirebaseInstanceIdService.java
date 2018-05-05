package com.example.android.nepalfishkeepers;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Save token to the database
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("firebaseToken", refreshedToken);
        editor.apply();
        Log.d("My Firebase Token:", "Refreshed token: " + refreshedToken);

//        04-03 19:31:12.332 27043-27104/com.example.android.nepalfishkeepers D/My Firebase Token:: Refreshed token: fsbWUKkFO4A:APA91bEvT5LOCY7woiVdm_rDQXX78UYwYqqz6fFXOoY0IoVZC0jpOHi1__JoUN-6HVx3Bf4NfmlMn2Zs4rG6GBr_YhFtVXgw7Y8ZLa6Lmy2txsNUoPJ6l0DP312ADzHt2n3d0J_XdIKM
    }

}
