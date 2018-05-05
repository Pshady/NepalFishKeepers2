package com.example.android.nepalfishkeepers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.hypertrack.lib.HyperTrackMapFragment;
import com.hypertrack.lib.MapFragmentCallback;

public class NfkMapFragment extends HyperTrackMapFragment {

    private MapFragmentCallback mMapFragmentCallback;
    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

//    @Override
//    public void setMapCallback(MapFragmentCallback mapCallback) {
//        super.setMapFragmentCallback(mapCallback);
//
//        mMapFragmentCallback = mapCallback;
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        mMap = googleMap;
    }
//
//    @Override
//    public void OnBackButtonClicked() {
//        super.OnBackButtonClicked();
//
//        if (mMapFragmentCallback != null)
//            mMapFragmentCallback.onBackButtonPressed();
//    }
//
//    @Override
//    public void onChooseOnMapClicked() {
//        super.onChooseOnMapClicked();
//    }
}
