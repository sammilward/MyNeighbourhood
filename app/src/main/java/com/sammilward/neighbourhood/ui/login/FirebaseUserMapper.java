package com.sammilward.neighbourhood.ui.login;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sammilward.neighbourhood.ui.login.Constants.BIO;
import static com.sammilward.neighbourhood.ui.login.Constants.CITY;
import static com.sammilward.neighbourhood.ui.login.Constants.DISPLAYNAME;
import static com.sammilward.neighbourhood.ui.login.Constants.DOB;
import static com.sammilward.neighbourhood.ui.login.Constants.EMAIL;
import static com.sammilward.neighbourhood.ui.login.Constants.ETHNICITY;
import static com.sammilward.neighbourhood.ui.login.Constants.FRIENDSLIST;
import static com.sammilward.neighbourhood.ui.login.Constants.IMAGEUPDATEDEPOCHTIME;
import static com.sammilward.neighbourhood.ui.login.Constants.IMAGEURL;
import static com.sammilward.neighbourhood.ui.login.Constants.INTERESTS;
import static com.sammilward.neighbourhood.ui.login.Constants.LATLON;
import static com.sammilward.neighbourhood.ui.login.Constants.NUMBEROFEVENTS;
import static com.sammilward.neighbourhood.ui.login.Constants.NUMBEROFFRIENDS;
import static com.sammilward.neighbourhood.ui.login.Constants.NUMBEROFPLACES;
import static com.sammilward.neighbourhood.ui.login.Constants.POSTCODE;

public class FirebaseUserMapper {


    public User DocumentToUser(DocumentSnapshot document)
    {
        User user = new User();
        user.setId(document.getId());
        user.setDisplayName(document.getString(DISPLAYNAME));
        user.setEmail(document.getString(EMAIL));
        user.setBio(document.getString(BIO));
        user.setEthnicity(document.getString(ETHNICITY));
        user.setImageURL(document.getString(IMAGEURL));
        user.setPostcode(document.getString(POSTCODE));
        user.setImageUpdatedEpochTime((long) document.get(IMAGEUPDATEDEPOCHTIME));
        user.setDOB(document.getDate(DOB));
        user.setNumberEvents(Math.toIntExact((long) document.get(NUMBEROFEVENTS)));
        user.setNumberFriends(Math.toIntExact((long) document.get(NUMBEROFFRIENDS)));
        user.setNumberPlaces(Math.toIntExact((long) document.get(NUMBEROFPLACES)));
        GeoPoint geoPoint = document.getGeoPoint(LATLON);
        user.setLocation(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
        user.setCity(document.getString(CITY));
        List<String> tempFriendsList = (List<String>) document.get(FRIENDSLIST);
        if (tempFriendsList != null) user.setFriendsList(tempFriendsList);
        else user.setFriendsList(new ArrayList<String>());

        List<String> tempInterestList = (List<String>) document.get(INTERESTS);
        if (tempInterestList != null) user.setInterests(tempInterestList);
        else user.setInterests(new ArrayList<String>());
        return user;
    }

    public Map<String, Object> UserToHashMap(User user)
    {
        Map<String, Object> userHashMap = new HashMap<>();
        userHashMap.put(DISPLAYNAME, user.getDisplayName());
        userHashMap.put(EMAIL, user.getEmail());
        userHashMap.put(DOB, user.getDOB());
        userHashMap.put(BIO, user.getBio());
        userHashMap.put(POSTCODE, user.getPostcode());
        userHashMap.put(LATLON, new GeoPoint(user.getLatLon().latitude, user.getLatLon().longitude));
        userHashMap.put(CITY, user.getCity());
        userHashMap.put(IMAGEURL, user.getImageURL());
        userHashMap.put(IMAGEUPDATEDEPOCHTIME, user.getImageUpdatedEpochTime());
        userHashMap.put(ETHNICITY, user.getEthnicity());
        userHashMap.put(NUMBEROFEVENTS, user.getNumberEvents());
        userHashMap.put(NUMBEROFFRIENDS, user.getNumberFriends());
        userHashMap.put(NUMBEROFPLACES, user.getNumberPlaces());
        userHashMap.put(FRIENDSLIST, user.getFriendsList());
        userHashMap.put(INTERESTS, user.getInterests());
        return userHashMap;
    }
}
