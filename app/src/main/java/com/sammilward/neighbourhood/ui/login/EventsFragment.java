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

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.sammilward.neighbourhood.ui.login.Constants.CITY;
import static com.sammilward.neighbourhood.ui.login.Constants.CREATEDBYEMAIL;
import static com.sammilward.neighbourhood.ui.login.Constants.EVENTSCOLLECTION;
import static com.sammilward.neighbourhood.ui.login.Constants.NAME;
import static com.sammilward.neighbourhood.ui.login.Constants.STARTDATE;
import static com.sammilward.neighbourhood.ui.login.Constants.USERSCOLLECTION;

public class EventsFragment extends Fragment {

    private final String TAG = "EventFragment";

    private EventsRecyclerAdapter eventsRecyclerAdapter;
    private RecyclerView RVEvents;
    private LinearLayoutManager eventsLayoutManager;
    private ProgressBar pbEvents;
    private RadioGroup rgFilters;
    private RadioButton rbToday, rbWithinAWeek, rbWithinAMonth, rbMyEvents;

    private FirebaseFirestore db;
    private FirebaseEventMapper mapper;
    private FirebaseUserMapper userMapper;
    private CollectionReference eventsRef;

    private User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        mapper = new FirebaseEventMapper();
        userMapper = new FirebaseUserMapper();
        eventsRef = db.collection(EVENTSCOLLECTION);
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialise();
        getUserAndEvents();
    }

    private void getUserAndEvents()
    {
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

    private void setEvents(List<Event> events)
    {
        eventsRecyclerAdapter = new EventsRecyclerAdapter(getContext(), events);
        RVEvents.setAdapter(eventsRecyclerAdapter);
        if (events.size() == 0) Toast.makeText(getContext(), "No events found", Toast.LENGTH_SHORT).show();
    }

    private void initialise()
    {
        eventsLayoutManager = new LinearLayoutManager(getContext());
        RVEvents = getView().findViewById(R.id.RVEvents);
        RVEvents.setLayoutManager(eventsLayoutManager);

        pbEvents = getView().findViewById(R.id.pbEvents);

        rgFilters = getView().findViewById(R.id.rgEventsFilters);

        rbToday = getView().findViewById(R.id.rbToday);
        rbToday.setChecked(true);
        rbWithinAWeek = getView().findViewById(R.id.rbWithinAWeek);
        rbWithinAMonth = getView().findViewById(R.id.rbWithinAMonth);
        rbMyEvents = getView().findViewById(R.id.rbMyEvents);

        rgFilters.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                filter();
            }
        });
    }

    private void filter() {
        if (user == null)
        {
            getUserAndEvents();
        }
        else {
            Query query = null;
            LocalDate current = LocalDate.now();
            if (rbToday.isChecked()) {
                query = eventsRef.whereEqualTo(CITY, user.getCity()).whereEqualTo(STARTDATE, Date.valueOf(current.toString()));
            } else if (rbWithinAWeek.isChecked()) {
                LocalDate currentPlusAWeek = current.plusDays(7);
                query = eventsRef.whereEqualTo(CITY, user.getCity()).whereGreaterThanOrEqualTo(STARTDATE, Date.valueOf(current.toString())).whereLessThanOrEqualTo(STARTDATE, Date.valueOf(currentPlusAWeek.toString())).orderBy(STARTDATE);
            } else if (rbWithinAMonth.isChecked()) {
                LocalDate currentPlusAMonth = current.plusDays(31);
                query = eventsRef.whereEqualTo(CITY, user.getCity()).whereGreaterThanOrEqualTo(STARTDATE, Date.valueOf(current.toString())).whereLessThanOrEqualTo(STARTDATE, Date.valueOf(currentPlusAMonth.toString())).orderBy(STARTDATE);
            } else if (rbMyEvents.isChecked()) {
                query = eventsRef.whereEqualTo(CREATEDBYEMAIL, FirebaseAuth.getInstance().getCurrentUser().getEmail()).orderBy(NAME).orderBy(STARTDATE);
            }
            displayFilteredEvents(query);
        }
    }

    public void displayFilteredEvents(Query query)
    {
        pbEvents.setVisibility(View.VISIBLE);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<Event> events = new ArrayList<>();
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    for (DocumentSnapshot doc: docs) {
                        events.add(mapper.DocumentToEvent(doc));
                    }
                    setEvents(events);
                    pbEvents.setVisibility(View.INVISIBLE);
                }
                else
                {
                    Log.e(TAG, "LoadEventsData: failed. "+task.getException().getMessage());
                    pbEvents.setVisibility(View.INVISIBLE);
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
