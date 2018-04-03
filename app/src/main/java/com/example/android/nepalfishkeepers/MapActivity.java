package com.example.android.nepalfishkeepers;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.HyperTrackMapFragment;
import com.hypertrack.lib.MapFragmentCallback;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.consumer.view.Placeline.PlacelineFragment;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ActionParams;
import com.hypertrack.lib.models.ActionParamsBuilder;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.SuccessResponse;


public class MapActivity extends AppCompatActivity {

    HyperTrackMapFragment htMapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        PlacelineFragment placelineFragment = (PlacelineFragment) getSupportFragmentManager().findFragmentById(R.id.placeline_fragment);
        MapFragmentCallback callback = new MapFragmentCallback() {
            @Override
            public void onMapReadyCallback(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
                // Handle onMapReadyCallback API here
            }

            @Override
            public void onExpectedPlaceSelected(Place expectedPlace) {
                // Check if expected place was selected
                if (expectedPlace != null) {
                    ActionParams actionParams = new ActionParamsBuilder()
                            .setCollectionId(collectionId != null ? collectionId : UUID.randomUUID().toString())
                            .setType(Action.ACTION_TYPE_VISIT)
                            .setExpectedPlace(expectedPlace)
                            .build();

// Call assignAction to start the tracking action
                    HyperTrack.createAndAssignAction(actionParams, new HyperTrackCallback() {
                        @Override
                        public void onSuccess(@NonNull SuccessResponse response) {
                            if (response.getResponseObject() != null) {
                                Action action = (Action) response.getResponseObject();

                                // Handle createAndAssign Action API success here
                                Toast.makeText(MapActivity.this, "Live Location successful shared back", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull ErrorResponse errorResponse) {
                            // Handle createAndAssign Action API error here
                            Toast.makeText(MapActivity.this, "Live Location successful shared back", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
        htMapFragment = (HyperTrackMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.placeline_fragment);
        HomeMapAdapter adapter = new HomeMapAdapter(this);
        htMapFragment.setHTMapAdapter(adapter);
        htMapFragment.setMapFragmentCallback(callback);

    }


}
