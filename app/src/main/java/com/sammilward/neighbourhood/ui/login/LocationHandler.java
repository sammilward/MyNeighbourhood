package com.sammilward.neighbourhood.ui.login;

import android.content.Context;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

public class LocationHandler {

    private Geocoder geocoder;

    public LocationHandler(Context context) {
        geocoder = new Geocoder(context);
    }

    public LatLng getLatLngFromLocationName(String name)
    {
        try {
            return new LatLng(geocoder.getFromLocationName(name, 1).get(0).getLatitude(), geocoder.getFromLocationName(name, 1).get(0).getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
            return new LatLng(0,0);
        }
    }
}
