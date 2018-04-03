package com.example.android.nepalfishkeepers;

import android.content.Context;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.lib.HyperTrackMapAdapter;
import com.hypertrack.lib.HyperTrackMapFragment;

import com.example.android.nepalfishkeepers.R;
import com.example.android.nepalfishkeepers.sendeta.store.SharedPreferenceManager;

class HomeMapAdapter extends HyperTrackMapAdapter {

    private Context context;

    HomeMapAdapter(Context mContext) {
        super(mContext);
        context = mContext;
    }

    @Override
    public CameraUpdate getMapFragmentInitialState(HyperTrackMapFragment hyperTrackMapFragment) {
        if (SharedPreferenceManager.getLastKnownLocation(context) != null) {
            LatLng latLng = new LatLng(SharedPreferenceManager.getLastKnownLocation(context).getLatitude(),
                    SharedPreferenceManager.getLastKnownLocation(context).getLongitude());
            return CameraUpdateFactory.newLatLngZoom(latLng, 15.0f);
        }
        return super.getMapFragmentInitialState(hyperTrackMapFragment);
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
    public int getResetBoundsButtonIcon(HyperTrackMapFragment hyperTrackMapFragment) {
        return R.drawable.ic_reset_bounds_button;
    }

    @Override
    public boolean showLocationDoneButton() {
        return false;
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