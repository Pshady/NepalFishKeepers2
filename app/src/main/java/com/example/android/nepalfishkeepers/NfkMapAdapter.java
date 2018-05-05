package com.example.android.nepalfishkeepers;

import android.content.Context;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.lib.HyperTrackMapAdapter;
import com.hypertrack.lib.HyperTrackMapFragment;

public class NfkMapAdapter extends HyperTrackMapAdapter {
    private Context context;

    public NfkMapAdapter(Context mContext) {
        super(mContext);
        context = mContext;
    }

    @Override
    public boolean showPlaceSelectorView() {
        return true;
    }

    @Override
    public boolean showTrailingPolyline() {
        return true;
    }

    @Override
    public boolean showTrafficLayer(HyperTrackMapFragment hyperTrackMapFragment) {
        return false;
    }

    @Override
    public boolean enableLiveLocationSharingView() {
        return true;
    }

    @Override
    public boolean showLocationDoneButton() {
        return true;
    }

    @Override
    public boolean showSourceMarkerForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return false;
    }

    @Override
    public boolean showLiveLocationSharingSummaryView() {
        return true;
    }

    @Override
    public boolean showActionSummaryForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return false;
    }

    @Override
    public boolean showBackButton() {
        return true;
    }
}
