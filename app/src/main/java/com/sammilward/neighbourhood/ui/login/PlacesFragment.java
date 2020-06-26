package com.sammilward.neighbourhood.ui.login;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.sammilward.neighbourhood.R;

import java.util.ArrayList;
import java.util.List;

import static com.sammilward.neighbourhood.ui.login.Constants.CITY;
import static com.sammilward.neighbourhood.ui.login.Constants.CREATEDBYEMAIL;
import static com.sammilward.neighbourhood.ui.login.Constants.NAME;
import static com.sammilward.neighbourhood.ui.login.Constants.PLACESCOLLECTION;
import static com.sammilward.neighbourhood.ui.login.Constants.USERSCOLLECTION;

public class PlacesFragment extends Fragment {

    private final String TAG = "PeopleFragment";

    private PlacesRecyclerAdapter placesRecyclerAdapter;
    private RecyclerView RVPlaces;
    private LinearLayoutManager placesLayoutManager;
    private ProgressBar pbPlaces;
    private RadioGroup rgFilters;
    private RadioButton rbAll, rbMyPlaces;

    private FirebaseFirestore db;
    private FirebasePlaceMapper mapper;
    private FirebaseUserMapper userMapper;
    private CollectionReference placesRef;

    private User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        placesRef = db.collection(PLACESCOLLECTION);
        mapper = new FirebasePlaceMapper();
        userMapper = new FirebaseUserMapper();
        return inflater.inflate(R.layout.fragment_places, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialise();
        getUserAndPlaces();
    }

    private void getUserAndPlaces() {
        CollectionReference collectionReference =  db.collection(USERSCOLLECTION);
        DocumentReference documentReference = collectionReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        user = userMapper.DocumentToUser(task.getResult());
                        filter();
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

    private void setPlaces(List<Place> places)
    {
        placesRecyclerAdapter = new PlacesRecyclerAdapter(getContext(), places);
        RVPlaces.setAdapter(placesRecyclerAdapter);
        if (places.size() == 0) Toast.makeText(getContext(), "No places found", Toast.LENGTH_SHORT).show();
    }

    private void initialise()
    {
        placesLayoutManager = new LinearLayoutManager(getContext());
        RVPlaces = getView().findViewById(R.id.RVPlaces);
        RVPlaces.setLayoutManager(placesLayoutManager);

        pbPlaces = getView().findViewById(R.id.pbPlaces);

        rgFilters = getView().findViewById(R.id.rgPlacesFilters);

        rbAll = getView().findViewById(R.id.rbAll);
        rbAll.setChecked(true);
        rbMyPlaces = getView().findViewById(R.id.rbMyPlaces);

        rgFilters.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                RadioButton selectedRadioButton = getView().findViewById(i);
                if(selectedRadioButton.isChecked())
                {
                    switch (i){
                        case R.id.rbAll:
                            break;
                        case R.id.rbMyPlaces:
                            break;
                    }
                    filter();
                }
            }
        });
    }

    private void filter() {
        if (user == null)
        {
            getUserAndPlaces();
        }
        else
        {
            Query query = null;
            if(rbAll.isChecked())
            {
                query = placesRef.whereEqualTo(CITY, user.getCity()).orderBy(NAME);
            }
            else if (rbMyPlaces.isChecked())
            {
                query = placesRef.whereEqualTo(CITY, user.getCity()).whereEqualTo(CREATEDBYEMAIL, FirebaseAuth.getInstance().getCurrentUser().getEmail()).orderBy(NAME);
            }
            displayFilteredPlaces(query);
        }
    }

    public void displayFilteredPlaces(Query query)
    {
        pbPlaces.setVisibility(View.VISIBLE);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<Place> places = new ArrayList<>();
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    for (DocumentSnapshot doc: docs) {
                        places.add(mapper.DocumentToPlace(doc));
                    }
                    setPlaces(places);
                    pbPlaces.setVisibility(View.INVISIBLE);
                }
                else
                {
                    pbPlaces.setVisibility(View.INVISIBLE);
                    Log.e(TAG, "LoadEventsData: failed. "+task.getException().getMessage());
                    Toast.makeText(getContext(), "An error has occured.\nPlease try again later", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        filter();
    }
}
