package com.sammilward.neighbourhood.ui.login;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.sammilward.neighbourhood.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.sammilward.neighbourhood.ui.login.Constants.CREATEDBYEMAIL;
import static com.sammilward.neighbourhood.ui.login.Constants.DIARYCOLLECTION;
import static com.sammilward.neighbourhood.ui.login.Constants.DIARYDATE;

public class DiaryFragment extends Fragment {

    private final String TAG = "DiaryFragment";

    private DiaryDaysRecyclerAdapter diaryDaysRecyclerAdapter;
    private RecyclerView RVDiaryDays;
    private LinearLayoutManager diaryDaysLayoutManager;
    private ProgressBar pbDiaryFragment;
    private FloatingActionButton FABAddDiaryDay;

    private FirebaseDiaryDayMapper mapper;

    private FirebaseFirestore db;
    private CollectionReference diaryRef;

    private DatePickerDialog datePickerDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mapper = new FirebaseDiaryDayMapper();
        db = FirebaseFirestore.getInstance();
        diaryRef = db.collection(DIARYCOLLECTION);
        return inflater.inflate(R.layout.fragment_diary, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialise();
    }

    private void setDiaryDays(List<DiaryDay> diaryDays)
    {
        diaryDaysRecyclerAdapter = new DiaryDaysRecyclerAdapter(getContext(), diaryDays);
        RVDiaryDays.setAdapter(diaryDaysRecyclerAdapter);
        if (diaryDays.size() == 0) Toast.makeText(getContext(), "No diary entries found", Toast.LENGTH_SHORT).show();
    }

    private void initialise()
    {
        diaryDaysLayoutManager = new LinearLayoutManager(getContext());
        FABAddDiaryDay = getView().findViewById(R.id.FABAddDiaryDay);
        RVDiaryDays = getView().findViewById(R.id.RVDiaryDays);
        RVDiaryDays.setLayoutManager(diaryDaysLayoutManager);

        pbDiaryFragment = getView().findViewById(R.id.pbDiaryFragment);

        FABAddDiaryDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener newDiaryDateSelected = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        addNewDiaryDate(i, i1+1, i2);
                    }
                };

                LocalDate date = LocalDate.now();
                datePickerDialog = new DatePickerDialog(getContext(), newDiaryDateSelected, date.getYear(), date.getMonthValue()-1, date.getDayOfMonth());
                datePickerDialog.show();
            }
        });
    }

    private void addNewDiaryDate(int year, int month, int day) {

        LocalDate localDate = LocalDate.of(year, month, day);

        DiaryDay diaryDay = new DiaryDay();
        diaryDay.setCreatedByEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        diaryDay.setDate(localDate);

        final String diaryDateId = UUID.randomUUID().toString();
        CollectionReference collectionReference =  db.collection(DIARYCOLLECTION);
        DocumentReference documentReference = collectionReference.document(diaryDateId);
        documentReference.set(mapper.DiaryDayToHashMap(diaryDay)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(getContext(), "Diary day added", Toast.LENGTH_SHORT).show();
                    displayDiaryDays();
                }
            }
        });
    }

    public void displayDiaryDays()
    {
        pbDiaryFragment.setVisibility(View.VISIBLE);
        Query query = diaryRef.whereEqualTo(CREATEDBYEMAIL, FirebaseAuth.getInstance().getCurrentUser().getEmail()).orderBy(DIARYDATE, Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<DiaryDay> diaryDays = new ArrayList<>();
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    for (DocumentSnapshot doc: docs) {
                        diaryDays.add(mapper.DocumentToDiaryDay(doc));
                    }
                    setDiaryDays(diaryDays);
                    pbDiaryFragment.setVisibility(View.INVISIBLE);
                }
                else
                {
                    pbDiaryFragment.setVisibility(View.INVISIBLE);
                    Log.e(TAG, "DisaplyDiaryDays: failed. "+task.getException().getMessage());
                    Toast.makeText(getContext(), "An error has occured.\nPlease try again later", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        displayDiaryDays();
    }
}
