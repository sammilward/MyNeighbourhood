package com.sammilward.neighbourhood.ui.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.sammilward.neighbourhood.R;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.WHITE;
import static com.sammilward.neighbourhood.ui.login.Constants.CITY;
import static com.sammilward.neighbourhood.ui.login.Constants.EVENTSCOLLECTION;
import static com.sammilward.neighbourhood.ui.login.Constants.PLACESCOLLECTION;
import static com.sammilward.neighbourhood.ui.login.Constants.STARTDATE;
import static com.sammilward.neighbourhood.ui.login.Constants.USERSCOLLECTION;

public class MapsFragment extends Fragment implements GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private FloatingActionButton FABLocate, FABAddPlace, FABAddEvent;
    private RadioGroup rgMapFilters;
    private RadioButton rbAll, rbPlaces, rbEvents;
    private ProgressBar pbMap;

    private static final float DEFAULT_ZOOM = 17f;
    private static final String TAG = "MapsFragment";

    private FirebaseFirestore db;
    private CollectionReference placesRef;
    private CollectionReference eventRef;
    private CollectionReference userRef;

    private FirebasePlaceMapper firebasePlaceMapper;
    private FirebaseUserMapper firebaseUserMapper;
    private FirebaseEventMapper firebaseEventMapper;

    private Marker newPlaceMarker;
    private Marker newEventMarker;

    private Boolean addingNewPlace = false;
    private Boolean addingNewEvent = false;

    private List<Marker> eventMarkers;
    private List<Marker> placeMarkers;

    private HashMap<String, Place> placeMarkerHash;
    private HashMap<String, Event> eventMarkerHash;

    private HeatmapTileProvider heatmapTileProvider;
    private TileOverlay heatmapOverlay;

    private List<User> users;

    private User user;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        placesRef = db.collection(PLACESCOLLECTION);
        eventRef = db.collection(EVENTSCOLLECTION);
        userRef = db.collection(USERSCOLLECTION);

        firebasePlaceMapper = new FirebasePlaceMapper();
        firebaseUserMapper = new FirebaseUserMapper();
        firebaseEventMapper = new FirebaseEventMapper();

        placeMarkerHash = new HashMap<>();
        eventMarkerHash = new HashMap<>();

        eventMarkers = new ArrayList<>();
        placeMarkers = new ArrayList<>();

        heatmapTileProvider = null;
        heatmapOverlay = null;
        users = new ArrayList<>();

        pbMap = getView().findViewById(R.id.pbMap);
        FABLocate = getView().findViewById(R.id.FABLocate);
        FABAddPlace = getView().findViewById(R.id.FABAddPlace);
        FABAddEvent = getView().findViewById(R.id.FABAddEvent);
        rgMapFilters = getView().findViewById(R.id.rgMapFilters);
        rbAll = getView().findViewById(R.id.rbAll);
        rbEvents = getView().findViewById(R.id.rbEvents);
        rbPlaces = getView().findViewById(R.id.rbPlaces);

        FABLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MoveCameraToCurrentLocation();
            }
        });

        FABAddPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HandleNewPlace();
            }
        });

        FABAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HandleNewEvent();            }
        });

        rbAll.setChecked(true);

        rgMapFilters.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.rbAll:
                        toggleVisibilityMarkers(eventMarkers, true);
                        toggleVisibilityMarkers(placeMarkers, true);
                        if (heatmapOverlay != null) heatmapOverlay.setVisible(false);
                        break;
                    case R.id.rbEvents:
                        toggleVisibilityMarkers(eventMarkers, true);
                        toggleVisibilityMarkers(placeMarkers, false);
                        if (heatmapOverlay != null) heatmapOverlay.setVisible(false);
                        break;
                    case R.id.rbPlaces:
                        toggleVisibilityMarkers(placeMarkers, true);
                        toggleVisibilityMarkers(eventMarkers, false);
                        if (heatmapOverlay != null) heatmapOverlay.setVisible(false);
                        break;
                    case R.id.rbUserHeat:
                        toggleVisibilityMarkers(placeMarkers, false);
                        toggleVisibilityMarkers(eventMarkers, false);

                        List<LatLng> usersLocations = new ArrayList<>();
                        for (User user : users) {
                            usersLocations.add(user.getLatLon());
                        }
                        heatmapTileProvider = new HeatmapTileProvider.Builder().data(usersLocations).opacity(1).radius(40).build();
                        heatmapOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
                }
            }
        });
    }

    private void GetMarkers()
    {
        if(mMap != null) mMap.clear();
        Query placesWithinTheCity = placesRef.whereEqualTo(CITY, user.getCity());
        placesWithinTheCity.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<Place> places = new ArrayList<>();
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    for (DocumentSnapshot doc: docs) {
                        places.add(firebasePlaceMapper.DocumentToPlace(doc));
                    }
                    intialisePlaceMarkers(places);
                    pbMap.setVisibility(View.INVISIBLE);
                }
                else
                {
                    Log.e(TAG, "LoadUserData: failed. "+task.getException().getMessage());
                }
            }
        });


        LocalDate current = LocalDate.now();
        LocalDate twoWeeksAhead = current.plusWeeks(2);
        Query withinTwoWeeksEventAndInTheCityQuery = eventRef.whereGreaterThanOrEqualTo(STARTDATE, Date.valueOf(current.toString())).whereLessThanOrEqualTo(STARTDATE, Date.valueOf(twoWeeksAhead.toString())).whereEqualTo(CITY, user.getCity());
        withinTwoWeeksEventAndInTheCityQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<Event> events = new ArrayList<>();
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    for (DocumentSnapshot doc: docs) {
                        events.add(firebaseEventMapper.DocumentToEvent(doc));
                    }
                    intialiseEventMarkers(events);
                    pbMap.setVisibility(View.INVISIBLE);
                }
                else
                {
                    Log.e(TAG, "LoadUserData: failed. "+task.getException().getMessage());
                }
            }
        });

        Query usersWithinTheCity = userRef.whereEqualTo(CITY, user.getCity());
        usersWithinTheCity .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    users = new ArrayList<>();
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    for (DocumentSnapshot doc: docs) {
                        users.add(firebaseUserMapper.DocumentToUser(doc));
                    }
                    pbMap.setVisibility(View.INVISIBLE);
                }
                else
                {
                    Log.e(TAG, "LoadUserData: failed. "+task.getException().getMessage());
                }
            }
        });
    }

    private void intialisePlaceMarkers(List<Place> places)
    {
        placeMarkerHash = new HashMap<>();
        for(Place place: places)
        {
            Marker marker = mMap.addMarker(CreateMarkerOptionsForPlaces(place));
            placeMarkerHash.put(marker.getId(), place);
            placeMarkers.add(marker);
        }
    }

    private void intialiseEventMarkers(List<Event> events)
    {
        eventMarkerHash = new HashMap<>();
        for(Event event: events)
        {
            Marker marker = mMap.addMarker(CreateMarkerOptionsForEvent(event));
            eventMarkerHash.put(marker.getId(), event);
            eventMarkers.add(marker);
        }
    }

    private MarkerOptions CreateMarkerOptionsForPlaces(Place place)
    {
        MarkerOptions markerOptions = new MarkerOptions().title(place.getName())
                .position(place.getLocation()).snippet(place.getDescription())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        return markerOptions;
    }

    private MarkerOptions CreateMarkerOptionsForEvent(Event event)
    {
        MarkerOptions markerOptions = new MarkerOptions().title(event.getName())
                .position(event.getLocation()).snippet(new SimpleDateFormat("dd-MM-yyyy").format(event.getStartDate()) + "\n" + event.getDescription())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        return markerOptions;
    }

    private void toggleVisibilityMarkers(List<Marker> markers, boolean setVisible)
    {
        for(Marker marker: markers)
        {
            marker.setVisible(setVisible);
        }
    }

    private void HandleNewPlace() {
        if(addingNewEvent)
        {
            RemoveFABConfirm(FABAddEvent);
            addingNewEvent = false;
        }

        if(newEventMarker != null)
        {
            newEventMarker.remove();
            newEventMarker = null;
        }
        addingNewPlace = true;
        if(newPlaceMarker == null)
        {
            try {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful())
                        {
                            Location currentLocation = (Location) task.getResult();
                            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            newPlaceMarker = mMap.addMarker(new MarkerOptions().title("New Place").snippet("Drag to position")
                            .position(currentLatLng).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                            MoveCamera(newPlaceMarker.getPosition(), DEFAULT_ZOOM);
                            ChangeFABToConfirm(FABAddPlace);
                        }
                        else
                        {
                            Toast.makeText(getContext(), "Could not get current location", Toast.LENGTH_SHORT);
                            Log.e(TAG, "Could not get current location");
                        }
                    }
                });
            }catch(SecurityException e)
            {
                Log.e(TAG, e.getMessage());
            }
        }
        else
        {
            MoveCamera(newPlaceMarker.getPosition(), DEFAULT_ZOOM);
        }
    }

    private void HandleNewEvent() {
        if(addingNewPlace)
        {
            RemoveFABConfirm(FABAddPlace);
            addingNewPlace = false;
        }

        if(newPlaceMarker != null)
        {
            newPlaceMarker.remove();
            newPlaceMarker = null;
        }
        addingNewEvent = true;
        if(newEventMarker == null)
        {
            try {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful())
                        {
                            Location currentLocation = (Location) task.getResult();
                            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            newEventMarker = mMap.addMarker(new MarkerOptions().title("New Event").snippet("Drag to position")
                                    .position(currentLatLng).draggable(true).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                            MoveCamera(newEventMarker.getPosition(), DEFAULT_ZOOM);
                            ChangeFABToConfirm(FABAddEvent);
                        }
                        else
                        {
                            Toast.makeText(getContext(), "Could not get current location", Toast.LENGTH_SHORT);
                            Log.e(TAG, "Could not get current location");
                        }
                    }
                });
            }catch(SecurityException e)
            {
                Log.e(TAG, e.getMessage());
            }
        }
        else
        {
            MoveCamera(newEventMarker.getPosition(), DEFAULT_ZOOM);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        SetupMap();
        MoveCameraToCurrentLocation();
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void SetupMap()
    {
        SetMapSettings();
        GetUsersMarkers();
    }

    private void GetUsersMarkers() {
        CollectionReference collectionReference =  db.collection(USERSCOLLECTION);
        DocumentReference documentReference = collectionReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        user = firebaseUserMapper.DocumentToUser(task.getResult());
                        GetMarkers();
                    }
                    else
                    {
                        Log.e(TAG, "getCurrentUser: Failed. " + task.getException().getMessage());
                    }
                }
                else
                {
                    Log.e(TAG, "getCurrentUser: Failed. " + task.getException().getMessage());
                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void SetMapSettings()
    {
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMaxZoomPreference(17f);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

                if(addingNewPlace)
                {
                    Toast.makeText(getContext(), "Drag marker to location of new place", Toast.LENGTH_SHORT).show();
                }
                else if (addingNewEvent)
                {
                    Toast.makeText(getContext(), "Drag marker to location of new event", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if(addingNewPlace)
                {
                    newPlaceMarker = marker;
                }
                else if (addingNewEvent)
                {
                    newEventMarker = marker;
                }
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {

                if(placeMarkerHash.containsKey(marker.getId()))
                {
                    View v = getLayoutInflater().inflate(R.layout.item_map_place_list, null);
                    Place place = placeMarkerHash.get(marker.getId());
                    TextView name = v.findViewById(R.id.lblPlacesListName);
                    TextView description = v.findViewById(R.id.lblPlacesListDescription);
                    name.setText(place.getName());
                    description.setText(place.getDescription());
                    return v;
                }
                else if (eventMarkerHash.containsKey(marker.getId()))
                {
                    View v = getLayoutInflater().inflate(R.layout.item_map_event_list, null);
                    Event event = eventMarkerHash.get(marker.getId());
                    TextView name = v.findViewById(R.id.lblEventsListName);
                    TextView dates = v.findViewById(R.id.lblEventsListStartDate);
                    TextView description = v.findViewById(R.id.lblEventsListDescription);
                    name.setText(event.getName());
                    dates.setText(event.getEventDatesText());
                    description.setText(event.getDescription());
                    return v;
                }
                else return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = null;
                if(placeMarkerHash.containsKey(marker.getId()))
                {
                    Place place = placeMarkerHash.get(marker.getId());
                    intent = new Intent(getContext(), ViewEditPlaceActivity.class);
                    intent.putExtra("PLACE", place);
                }
                else if(eventMarkerHash.containsKey(marker.getId()))
                {
                    Event event = eventMarkerHash.get(marker.getId());
                    intent = new Intent(getContext(), ViewEditEventActivity.class);
                    intent.putExtra("EVENT", event);
                }
                if(intent != null) startActivity(intent);
            }
        });
    }

    private void MoveCameraToCurrentLocation()
    {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        try {
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        Location currentLocation = (Location) task.getResult();
                        MoveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),DEFAULT_ZOOM );
                    }
                    else
                    {
                        Toast.makeText(getContext(), "Could not get current location", Toast.LENGTH_SHORT);
                        Log.e(TAG, "Could not get current location");
                    }
                }
            });
        }catch(SecurityException e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    private void MoveCamera(LatLng latLng, float zoom)
    {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    public void ChangeFABToConfirm(FloatingActionButton fab)
    {
        fab.setBackgroundTintList(ColorStateList.valueOf(GREEN));
        fab.setImageResource(R.drawable.ic_check_black_24dp);
        fab.setImageTintList(ColorStateList.valueOf(WHITE));

        if(addingNewPlace)
        {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), AddPlaceActivity.class);
                    Place place = new Place();
                    place.setLocation(newPlaceMarker.getPosition());
                    intent.putExtra("PLACE", place);
                    startActivity(intent);
                }
            });
        }
        else if (addingNewEvent)
        {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), AddEventActivity.class);
                    Event event = new Event();
                    event.setLocation(newEventMarker.getPosition());
                    intent.putExtra("EVENT", event);
                    startActivity(intent);
                }
            });
        }
    }

    public void RemoveFABConfirm(FloatingActionButton fab)
    {
        fab.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));

        if(addingNewPlace)
        {
            fab.setImageResource(R.drawable.ic_location_black_24dp);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HandleNewPlace();
                }
            });
        }
        else if(addingNewEvent)
        {
            fab.setImageResource(R.drawable.ic_event_black_24dp);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HandleNewEvent();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null) GetUsersMarkers();
    }
}