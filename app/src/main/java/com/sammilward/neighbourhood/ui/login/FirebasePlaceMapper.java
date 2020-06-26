package com.sammilward.neighbourhood.ui.login;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

import static com.sammilward.neighbourhood.ui.login.Constants.CITY;
import static com.sammilward.neighbourhood.ui.login.Constants.CREATEDBYEMAIL;
import static com.sammilward.neighbourhood.ui.login.Constants.DESCRIPTION;
import static com.sammilward.neighbourhood.ui.login.Constants.IMAGEUPDATEDEPOCHTIME;
import static com.sammilward.neighbourhood.ui.login.Constants.IMAGEURL;
import static com.sammilward.neighbourhood.ui.login.Constants.LATLON;
import static com.sammilward.neighbourhood.ui.login.Constants.NAME;

public class FirebasePlaceMapper {
    public Place DocumentToPlace(DocumentSnapshot document)
    {
        Place place = new Place();
        place.setId(document.getId());
        place.setName(document.getString(NAME));
        place.setDescription(document.getString(DESCRIPTION));
        place.setCreatedByEmail(document.getString(CREATEDBYEMAIL));
        place.setImageURL(document.getString(IMAGEURL));
        place.setImageUpdatedEpochTime((long) document.get(IMAGEUPDATEDEPOCHTIME));
        GeoPoint geoPoint = document.getGeoPoint(LATLON);
        place.setLocation(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
        place.setCity(document.getString(CITY));
        return place;
    }

    public Map<String, Object> PlaceToHashmap(Place place)
    {
        Map<String, Object> placeHashmap = new HashMap<>();
        placeHashmap.put(NAME, place.getName());
        placeHashmap.put(DESCRIPTION, place.getDescription());
        placeHashmap.put(CREATEDBYEMAIL, place.getCreatedByEmail());
        placeHashmap.put(IMAGEURL, place.getImageURL());
        placeHashmap.put(IMAGEUPDATEDEPOCHTIME, place.getImageUpdatedEpochTime());
        placeHashmap.put(LATLON, new GeoPoint(place.getLocation().latitude, place.getLocation().longitude));
        placeHashmap.put(CITY, place.getCity());
        return placeHashmap;
    }
}
