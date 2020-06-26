package com.sammilward.neighbourhood.ui.login;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.Arrays;
import java.util.List;

import static com.sammilward.neighbourhood.ui.login.Constants.DISPLAYNAME;
import static com.sammilward.neighbourhood.ui.login.Constants.DOB;
import static com.sammilward.neighbourhood.ui.login.Constants.ETHNICITY;
import static com.sammilward.neighbourhood.ui.login.Constants.FRIENDSLIST;
import static com.sammilward.neighbourhood.ui.login.Constants.USERSCOLLECTION;

public class FriendsFragment extends Fragment {

    private final String TAG = "FriendFragment";

    private ProfileRecyclerAdapter profileRecyclerAdapter;
    private RecyclerView RVFriends;
    private LinearLayoutManager friendsLayoutManager;
    private ProgressBar pbFriends;
    private RadioGroup rgFilters;
    private RadioButton rbName, rbAge, rbInterests, rbEthnicity;
    private Button cmdFilter, cmdRemoveFilter;
    private Spinner spnEthnicity;
    private LinearLayout llAllFilters, llNameFilterBar, llAgeFilterBar, llInterestsFilterBar, llEthnicityFilterBar, currentFilterBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUserMapper mapper;
    private FirebaseUserMapper userMapper;
    private CollectionReference userRef;
    private String currentUserId;

    private User user;
    private Query latestQuery;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mapper = new FirebaseUserMapper();
        userMapper = new FirebaseUserMapper();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        return inflater.inflate(R.layout.fragment_people, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialise();
        getUserAndPeople();
    }

    private void getUserAndPeople() {
        pbFriends.setVisibility(View.VISIBLE);
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
                        userRef = db.collection(USERSCOLLECTION);
                        Query allFriends = userRef.whereArrayContains(FRIENDSLIST, user.getEmail());
                        latestQuery = allFriends;
                        allFriends.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful())
                                {
                                    List<User> users = new ArrayList<>();
                                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                                    for (DocumentSnapshot doc: docs) {
                                        if(!currentUserId.equals(doc.getId())) users.add(mapper.DocumentToUser(doc));
                                    }
                                    setPeople(users);
                                    pbFriends.setVisibility(View.INVISIBLE);
                                }
                                else
                                {
                                    Log.e(TAG, "LoadUsersData: failed. "+task.getException().getMessage());
                                }
                            }
                        });
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

    private void setPeople(List<User> users)
    {
        profileRecyclerAdapter = new ProfileRecyclerAdapter(getContext(), users, user);
        RVFriends.setAdapter(profileRecyclerAdapter);
    }

    private void initialise()
    {

        friendsLayoutManager = new LinearLayoutManager(getContext());
        RVFriends = getView().findViewById(R.id.RVPeople);
        RVFriends.setLayoutManager(friendsLayoutManager);

        pbFriends = getView().findViewById(R.id.pbPeople);

        cmdFilter = getView().findViewById(R.id.cmdPeopleFilter);
        cmdRemoveFilter= getView().findViewById(R.id.cmdPeopleRemoveFilter);

        rgFilters = getView().findViewById(R.id.rgMapFilters);

        llAllFilters = getView().findViewById(R.id.llAllFilters);
        llNameFilterBar = getView().findViewById(R.id.llNameFilterBar);
        llAgeFilterBar = getView().findViewById(R.id.llAgeFilterBar);
        llInterestsFilterBar = getView().findViewById(R.id.llInterestsFilterBar);
        llEthnicityFilterBar = getView().findViewById(R.id.llEthnicityFilterBar);

        rbName = getView().findViewById(R.id.rbPeopleName);
        rbAge = getView().findViewById(R.id.rbPeopleAge);
        rbInterests = getView().findViewById(R.id.rbPeopleInterests);
        rbEthnicity = getView().findViewById(R.id.rbPeopleEthnicity);

        setupEthnicitySpinner();

        cmdRemoveFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(llAllFilters.getVisibility() == View.VISIBLE)
                {
                    llAllFilters.setVisibility(View.GONE);
                    currentFilterBar.setVisibility(View.GONE);
                    cmdRemoveFilter.setVisibility(View.GONE);
                    getUserAndPeople();
                    RadioButton selectedRadioButton = getView().findViewById(rgFilters.getCheckedRadioButtonId());
                    selectedRadioButton.setChecked(false);
                }
            }
        });

        rgFilters.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                RadioButton selectedRadioButton = getView().findViewById(i);
                if(selectedRadioButton.isChecked())
                {
                    if(llAllFilters.getVisibility() == View.GONE) {
                        llAllFilters.setVisibility(View.VISIBLE);
                        cmdRemoveFilter.setVisibility(View.VISIBLE);
                    }
                    switch (i){
                        case R.id.rbPeopleName:
                            switchFilterBar(llNameFilterBar);
                            break;
                        case R.id.rbPeopleAge:
                            switchFilterBar(llAgeFilterBar);
                            break;
                        case R.id.rbPeopleInterests:
                            switchFilterBar(llInterestsFilterBar);
                            break;
                        case R.id.rbPeopleEthnicity:
                            switchFilterBar(llEthnicityFilterBar);
                            break;
                    }
                }
            }
        });

        cmdFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter();
            }
        });
    }

    private void filter() {
        Query query = null;

        if(rbName.isChecked())
        {
            EditText txtName = getView().findViewById(R.id.txtPeopleNameFilter);
            String name = txtName.getText().toString();
            query = userRef.whereEqualTo(DISPLAYNAME, name).whereArrayContains(FRIENDSLIST, user.getEmail());
            displayFilteredUsers(query);
        }
        else if (rbAge.isChecked())
        {
            EditText txtFrom = getView().findViewById(R.id.txtPeopleAgeFrom);
            EditText txtTo = getView().findViewById(R.id.txtPeopleAgeTo);

            int ageFrom;
            int ageTo;

            LocalDate currentDate = LocalDate.now();

            if(!txtFrom.getText().toString().isEmpty() && !txtTo.getText().toString().isEmpty())
            {
                ageFrom = Integer.parseInt(txtFrom.getText().toString());
                ageTo = Integer.parseInt(txtTo.getText().toString());

                LocalDate fromLocalDate = currentDate.minusYears(ageFrom);
                LocalDate toLocalDate = currentDate.minusYears(ageTo+1);

                Date fromDate = Date.valueOf(fromLocalDate.toString());
                Date toDate = Date.valueOf(toLocalDate.toString());

                query = userRef.whereLessThanOrEqualTo(DOB, fromDate).whereGreaterThanOrEqualTo(DOB, toDate).whereArrayContains(FRIENDSLIST, user.getEmail());
            }
            else if (!txtFrom.getText().toString().isEmpty())
            {
                ageFrom = Integer.parseInt(txtFrom.getText().toString());
                LocalDate fromLocalDate = currentDate.minusYears(ageFrom);
                Date fromDate = Date.valueOf(fromLocalDate.toString());
                query = userRef.whereLessThanOrEqualTo(DOB, fromDate).whereArrayContains(FRIENDSLIST, user.getEmail());
            }
            else if (!txtTo.getText().toString().isEmpty())
            {
                ageTo = Integer.parseInt(txtTo.getText().toString());
                LocalDate toLocalDate = currentDate.minusYears(ageTo+1);
                Date toDate = Date.valueOf(toLocalDate.toString());

                query = userRef.whereGreaterThanOrEqualTo(DOB, toDate).whereArrayContains(FRIENDSLIST, user.getEmail());
            }
        }
        else if (rbInterests.isChecked())
        {
            query = userRef.whereArrayContains(FRIENDSLIST, user.getEmail());
        }
        else if (rbEthnicity.isChecked())
        {
            String ethnicity = spnEthnicity.getSelectedItem().toString();
            query = userRef.whereEqualTo(ETHNICITY, ethnicity).whereArrayContains(FRIENDSLIST, user.getEmail());
            displayFilteredUsers(query);
        }

        latestQuery = query;
        displayFilteredUsers(query);
    }

    public void displayFilteredUsers(Query query)
    {
        pbFriends.setVisibility(View.VISIBLE);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<User> users = new ArrayList<>();
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    for (DocumentSnapshot doc: docs) {
                        User filteredUser = mapper.DocumentToUser(doc);
                        if(!currentUserId.equals(doc.getId()))
                        {
                            if (rbInterests.isChecked())
                            {
                                EditText txtPeopleInterests = getView().findViewById(R.id.txtPeopleInterests);
                                String interest = txtPeopleInterests.getText().toString();
                                if (filteredUser.getInterests().contains(interest)) users.add(filteredUser);
                            }
                            else users.add(filteredUser);
                        }
                    }

                    setPeople(users);
                    pbFriends.setVisibility(View.INVISIBLE);
                }
                else
                {
                    Log.e(TAG, "LoadUsersData: failed. "+task.getException().getMessage());
                }
            }
        });
    }

    private void switchFilterBar(LinearLayout layout)
    {
        if(currentFilterBar != null) currentFilterBar.setVisibility(View.GONE);
        layout.setVisibility(View.VISIBLE);
        currentFilterBar = layout;
    }

    private void setupEthnicitySpinner() {
        spnEthnicity = getView().findViewById(R.id.spnPeopleEthnicity);
        List<String> ethnicities = Arrays.asList(getResources().getStringArray(R.array.ethnicities));
        ArrayAdapter<String> adapterEthnicities = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, ethnicities)
        {
            @Override
            public boolean isEnabled(int position) {
                if(position == 0) return false;
                else return true;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position==0) tv.setTextColor(Color.GRAY);
                return view;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setPadding(0, view.getPaddingTop(), 0, view.getPaddingBottom());
                return view;
            }
        };
        adapterEthnicities.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnEthnicity.setAdapter(adapterEthnicities);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (user != null)
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
                            latestQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        List<User> users = new ArrayList<>();
                                        List<DocumentSnapshot> docs = task.getResult().getDocuments();
                                        for (DocumentSnapshot doc: docs) {
                                            if(!currentUserId.equals(doc.getId())) users.add(mapper.DocumentToUser(doc));
                                        }
                                        setPeople(users);
                                    }
                                    else
                                    {
                                        Log.e(TAG, "LoadUsersData: failed. "+task.getException().getMessage());
                                    }
                                }
                            });
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
    }
}
