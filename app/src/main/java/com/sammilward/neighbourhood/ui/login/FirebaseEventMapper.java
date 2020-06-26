package com.sammilward.neighbourhood.ui.login;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

import static com.sammilward.neighbourhood.ui.login.Constants.CITY;
import static com.sammilward.neighbourhood.ui.login.Constants.CREATEDBYEMAIL;
import static com.sammilward.neighbourhood.ui.login.Constants.DESCRIPTION;
import static com.sammilward.neighbourhood.ui.login.Constants.ENDDATE;
import static com.sammilward.neighbourhood.ui.login.Constants.IMAGEUPDATEDEPOCHTIME;
import static com.sammilward.neighbourhood.ui.login.Constants.IMAGEURL;
import static com.sammilward.neighbourhood.ui.login.Constants.LATLON;
import static com.sammilward.neighbourhood.ui.login.Constants.NAME;
import static com.sammilward.neighbourhood.ui.login.Constants.STARTDATE;

public class FirebaseEventMapper {
    public Event DocumentToEvent(DocumentSnapshot document)
    {
        Event event = new Event();
        event.setId(document.getId());
        event.setName(document.getString(NAME));
        event.setDescription(document.getString(DESCRIPTION));
        event.setCreatedByEmail(document.getString(CREATEDBYEMAIL));
        event.setImageURL(document.getString(IMAGEURL));
        event.setImageUpdatedEpochTime((long) document.get(IMAGEUPDATEDEPOCHTIME));
        event.setStartDate(document.getDate(STARTDATE));
        event.setEndDate(document.getDate(ENDDATE));
        GeoPoint geoPoint = document.getGeoPoint(LATLON);
        event.setLocation(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
        event.setCity(document.getString(CITY));
        return event;
    }

    public Map<String, Object> EventToHashmap(Event event)
    {
        Map<String, Object> eventHashmap = new HashMap<>();
        eventHashmap.put(NAME, event.getName());
        eventHashmap.put(DESCRIPTION, event.getDescription());
        eventHashmap.put(CREATEDBYEMAIL, event.getCreatedByEmail());
        eventHashmap.put(IMAGEURL, event.getImageURL());
        eventHashmap.put(IMAGEUPDATEDEPOCHTIME, event.getImageUpdatedEpochTime());
        eventHashmap.put(STARTDATE, event.getStartDate());
        eventHashmap.put(ENDDATE, event.getEndDate());
        eventHashmap.put(LATLON, new GeoPoint(event.getLocation().latitude, event.getLocation().longitude));
        eventHashmap.put(CITY, event.getCity());
        return eventHashmap;
    }
}
