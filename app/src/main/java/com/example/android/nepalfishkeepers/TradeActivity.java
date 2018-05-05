package com.example.android.nepalfishkeepers;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.HyperTrackMapFragment;
import com.hypertrack.lib.MapFragmentCallback;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.consumer.view.MarkerAnimation;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ActionParams;
import com.hypertrack.lib.models.ActionParamsBuilder;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.HyperTrackLocation;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.SuccessResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.example.android.nepalfishkeepers.Constants.FCM_SERVER_KEY;

public class TradeActivity extends FragmentActivity {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private HyperTrackMapFragment mHyperTrackMapFragment;
    private LinearLayout mActionLayout;
    private Button mShareButton;
    private String postKey;
    private Place mLastExpectedPlace;
    private ButtonType mButtonType;
    private FirebaseAuth mAuth;
    Marker currentLocationMarker;
    private DatabaseReference mDatabase;
    private DatabaseReference mTradeDatabase;
    private FirebaseUser mCurrentUser;
    private String collectionId;

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            Task locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = (Location) task.getResult();
                    }

                    if(mLastKnownLocation == null){
                        mLastKnownLocation = new Location(LocationManager.GPS_PROVIDER);
                        mLastKnownLocation.setLatitude(27.7172);
                        mLastKnownLocation.setLongitude(85.3240);
                    }

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()),
                            Constants.DEFAULT_ZOOM));

                    updateMarkerLocation();
                }
            });
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        mActionLayout = findViewById(R.id.live_tracking_action_layout);
        mShareButton = findViewById(R.id.share_button);

        postKey = getIntent().getExtras().getString("postKey");
        if(getIntent().hasExtra("collectionId")) {
            collectionId = getIntent().getExtras().getString("collectionId");
        } else {
            collectionId = null;
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mHyperTrackMapFragment = (HyperTrackMapFragment) getSupportFragmentManager().findFragmentById(R.id.htMapfragment);
        mHyperTrackMapFragment.setHTMapAdapter(new NfkMapAdapter(TradeActivity.this));
        mHyperTrackMapFragment.setMapFragmentCallback(mapFragmentCallback);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mTradeDatabase = FirebaseDatabase.getInstance().getReference().child("Trades");
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestPermissions(this);
        }

        setButtonType(ButtonType.SHARE_LINK);
        mShareButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            switch (mButtonType) {
                case SHARE_LINK:
                    // create action
                    createAction();
                    break;
                case SHARED:
                    shareComplete();
                    break;

            }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestPermissions(this);
        }
    }

    private void sendNotification(final String title, final String body, final String firebaseToken, final String collectionId) {
        String url ="https://fcm.googleapis.com/fcm/send";
        VolleyLog.DEBUG = true;

        JSONObject params = new JSONObject();
        JSONObject notification = new JSONObject();
        try {
            params.put("to", firebaseToken);
            notification.put("message", body);
            notification.put("title", title);
            notification.put("type", NotificationType.ACTION);
            notification.put("collectionId", collectionId);
            notification.put("postKey", postKey);
            params.put("data", notification);
        } catch (JSONException e) {
            Log.d("JsonError", e.getMessage());
        }

        // Request a string response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Toast.makeText(getApplicationContext(), "Notification sent", Toast.LENGTH_SHORT).show();;

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();;
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Authorization", "key=" + FCM_SERVER_KEY);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        RequestQueueSingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsonRequest);
    }

    private void createAction() {
        if(collectionId == null)
            collectionId = UUID.randomUUID().toString();

        ActionParams params = new ActionParamsBuilder()
                .setExpectedPlace(mLastExpectedPlace)
                .setType(Action.ACTION_TYPE_VISIT)
                .setCollectionId(collectionId)
                .build();

        // Call createAction to create an action
        HyperTrack.createAndAssignAction(params, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                if (response.getResponseObject() != null) {
                    final Action action = (Action) response.getResponseObject();
                    String shareMessage = action.getShareMessage();

                    Log.d("Message", shareMessage);

                    // Get trader's token
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Trade trade = dataSnapshot.child("Trades").child(postKey).getValue(Trade.class);
                            User trader = dataSnapshot.child("Users").child(trade.getTraderId()).getValue(User.class);

                            // Store collectionId in database for future use
                            DatabaseReference tradeReference = mDatabase.child("Trades").child(postKey);

                            trade.setCollectionId(collectionId);
                            trade.sharers.put(mCurrentUser.getUid(), action.getId());
                            trade.setTradeStatus(TradeStatus.APPROVED);

                            tradeReference.setValue(trade);

//                            tradeReference.child("collectionId").setValue(collectionId);
//                            tradeReference.child("sharers").setValue(mCurrentUser.getUid(), action.getId());
//
//                            // Change status to Approved
//                            tradeReference.child("tradeStatus").setValue(TradeStatus.APPROVED);

                            // Send notification with collectionId
                            sendNotification("Trade request accepted",
                                    "Trade location has been shared",
                                    trader.getFirebasetoken(),
                                    collectionId);

                            Intent intent = new Intent(TradeActivity.this, TradeActivity.class);
                            intent.putExtra("postKey", postKey);
                            intent.putExtra("collectionId", collectionId);
                            startActivity(intent);
                            TradeActivity.this.finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                // Handle createAction API error here
//                Toast.makeText(this, "Live location not shared", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareComplete() {
        mDatabase.child("Trades").child(postKey).addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(final DataSnapshot dataSnapshot) {

                 Log.d("completedddd", "yo yo");

                 TradeStatus status = TradeStatus.valueOf(dataSnapshot.child("tradeStatus").getValue().toString());
                 if(status == TradeStatus.TRADED) {
                     HyperTrack.removeActions(null);
                     // Handle removeActions response here
//                     Intent intent = new Intent(TradeActivity.this, SingleNfkActivity.class);
//                     intent.putExtra("postId", postKey);
//                     startActivity(intent);
                     TradeActivity.this.finish();
                 }

                 final String tradeOwner = dataSnapshot.child("tradeOwner").getValue().toString();
                 final String traderId = dataSnapshot.child("traderId").getValue().toString();

                 String actionId = dataSnapshot.child("sharers").child(tradeOwner).getValue().toString();

                 HyperTrack.completeActionInSync(actionId, new HyperTrackCallback() {
                     @Override
                     public void onSuccess(@NonNull SuccessResponse response) {
                         HyperTrack.stopTracking(new HyperTrackCallback() {
                             @Override
                             public void onSuccess(@NonNull SuccessResponse response) {
//                                 mDatabase.child("Trades").child(postKey).child("tradeStatus").setValue(TradeStatus.TRADED);
//                                 mDatabase.child("Trades").child(postKey).child("sharers").child(tradeOwner).setValue("");

//                                 HyperTrack.removeActions(null);
//                                 Intent intent = new Intent(TradeActivity.this, SingleNfkActivity.class);
//                                 intent.putExtra("postId", postKey);
//                                 startActivity(intent);
//                                 TradeActivity.this.finish();


                             }

                             @Override
                             public void onError(@NonNull ErrorResponse errorResponse) {

                             }
                         });

                         final Trade trade = dataSnapshot.getValue(Trade.class);
                         trade.setTradeStatus(TradeStatus.TRADED);
                         trade.sharers.put(tradeOwner, "");

                         if(dataSnapshot.child("sharers").hasChild(traderId)) {
                             String anotherActionId = dataSnapshot.child("sharers").child(traderId).getValue().toString();
                             HyperTrack.completeActionInSync(anotherActionId, new HyperTrackCallback() {
                                 @Override
                                 public void onSuccess(@NonNull SuccessResponse response) {
                                     HyperTrack.stopTracking(new HyperTrackCallback() {
                                         @Override
                                         public void onSuccess(@NonNull SuccessResponse response) {

                                             trade.sharers.put(traderId, "");
                                             mTradeDatabase.child(postKey).setValue(trade);

//                                     mDatabase.child("Trades").child(postKey).child("tradeStatus").setValue(TradeStatus.TRADED);
//                                     mDatabase.child("Trades").child(postKey).child("sharers").child(traderId).setValue("");

                                             HyperTrack.removeActions(null);
//                                             Intent intent = new Intent(TradeActivity.this, SingleNfkActivity.class);
//                                             intent.putExtra("postId", postKey);
//                                             startActivity(intent);
                                             TradeActivity.this.finish();
                                         }

                                         @Override
                                         public void onError(@NonNull ErrorResponse errorResponse) {

                                         }
                                     });
                                 }

                                 @Override
                                 public void onError(@NonNull ErrorResponse errorResponse) {

                                 }
                             });
                         } else {
                             mTradeDatabase.child(postKey).setValue(trade);

                             HyperTrack.removeActions(null);
                             Intent intent = new Intent(TradeActivity.this, SingleNfkActivity.class);
                             intent.putExtra("postId", postKey);
                             startActivity(intent);
                             TradeActivity.this.finish();
                         }
                     }

                     @Override
                     public void onError(@NonNull ErrorResponse errorResponse) {

                     }
                 });




             }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
         });




        Log.d("Message", "Share complete");
    }

    private void trackAction() {
        HyperTrack.trackActionByCollectionId(collectionId, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                // Handle trackActionByCollectionId API success here
                // Get trader's token
                mDatabase.child("Trades").child(postKey).child("sharers").child(mCurrentUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            setButtonType(ButtonType.SHARED);
                        } else {
                            setButtonType(ButtonType.SHARE_LINK);
                        }
                        mActionLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
//                getDeviceLocation();
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                // Handle trackActionByCollectionId API error here
            }
        });
    }

    private void updateMarkerLocation() {
        LatLng latLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        if (currentLocationMarker == null) {
            currentLocationMarker = mMap.addMarker(new MarkerOptions().
                    position(latLng).
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ht_source_place_marker))
                    .anchor(0.5f, 0.5f));
        } else {
            currentLocationMarker.setVisible(true);
            MarkerAnimation.animateMarker(currentLocationMarker, latLng);
        }
    }

    public enum ButtonType {
        SHARE_LINK,
        SHARED
    }

    private void setButtonType(ButtonType type) {
        mButtonType = type;
        if(type == ButtonType.SHARE_LINK) {
            mShareButton.setText("Share");
            mShareButton.setBackgroundColor(Color.parseColor("#00ff00"));
        } else {
            mShareButton.setText("Stop");
            mShareButton.setBackgroundColor(Color.parseColor("#ff0000"));
        }
    }

    private MapFragmentCallback mapFragmentCallback = new MapFragmentCallback() {
        @Override
        public void onMapReadyCallback(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
            super.onMapReadyCallback(hyperTrackMapFragment, map);

            mMap = map;
            if(collectionId == null)
                getDeviceLocation();
        }

        @Override
        public void onExpectedPlaceSelected(Place expectedPlace) {
            super.onExpectedPlaceSelected(expectedPlace);
            if (expectedPlace != null) {
                // Use this place to createAndAssignAction for current userId
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(expectedPlace.getLocation().getLatitude(), expectedPlace.getLocation().getLongitude()),
                        Constants.DEFAULT_ZOOM));

                mLastExpectedPlace = expectedPlace;

                if(collectionId == null)
                    mActionLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onMapLoadedCallback(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
            super.onMapLoadedCallback(hyperTrackMapFragment, map);

            if(collectionId != null) {
                trackAction();

            } else {
                mHyperTrackMapFragment.openPlaceSelectorView();
            }
        }

        @Override
        public void onBackButtonIconPressed() {
            super.onBackButtonIconPressed();
        }

        @Override
        public void onChooseOnMapSelected() {
            super.onChooseOnMapSelected();
//            updateMarkerLocation();
            mActionLayout.setVisibility(View.GONE);
        }
    };
}
