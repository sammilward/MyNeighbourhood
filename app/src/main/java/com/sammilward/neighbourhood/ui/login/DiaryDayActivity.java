package com.sammilward.neighbourhood.ui.login;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sammilward.neighbourhood.R;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.sammilward.neighbourhood.ui.login.Constants.DIARYCOLLECTION;

public class DiaryDayActivity extends AppCompatActivity {

    private final String TAG = "DiaryDayActivity";

    private DiaryDay diaryDay;
    private FloatingActionButton FABAddDiaryTime;
    private ImageView imgDiaryDaySad, imgDiaryDayMeh, imgDiaryDayHappy;
    private Button cmdDeleteDiaryDay;
    private TextView lblDateOfEntry;
    private RecyclerView RVDiaryTimes;
    private DiaryTimesRecyclerAdapter diaryTimesRecyclerAdapter;
    private LinearLayoutManager diaryTimesLayoutManager;

    private FirebaseFirestore db;
    private CollectionReference diaryRef;
    private FirebaseDiaryDayMapper mapper;

    private String selectedRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_day);

        Intent intent = getIntent();
        diaryDay = intent.getParcelableExtra("DIARYDAY");
        selectedRating = diaryDay.getRating();

        db = FirebaseFirestore.getInstance();
        diaryRef = db.collection(DIARYCOLLECTION);
        mapper = new FirebaseDiaryDayMapper();

        initalise();
        setDiaryDays();
    }

    private void initalise()
    {
        FABAddDiaryTime = findViewById(R.id.FABAddDiaryTime);
        lblDateOfEntry = findViewById(R.id.lblDateOfEntry);
        imgDiaryDaySad = findViewById(R.id.imgDiaryDaySad);
        imgDiaryDayMeh = findViewById(R.id.imgDiaryDayMeh);
        imgDiaryDayHappy = findViewById(R.id.imgDiaryDayHappy);
        cmdDeleteDiaryDay = findViewById(R.id.cmdDeleteDiaryDay);
        diaryTimesLayoutManager = new LinearLayoutManager(getApplicationContext());
        RVDiaryTimes = findViewById(R.id.RVDiaryTimes);
        RVDiaryTimes.setLayoutManager(diaryTimesLayoutManager);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        lblDateOfEntry.setText(diaryDay.getDate().format(dateTimeFormatter));

        FABAddDiaryTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DiaryDayActivity.this, AddDiaryTimeActivity.class);
                intent.putExtra("DIARYDAY", diaryDay);
                startActivity(intent);
            }
        });

        imgDiaryDaySad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgDiaryDaySad.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                imgDiaryDayMeh.setBackgroundColor(Color.TRANSPARENT);
                imgDiaryDayHappy.setBackgroundColor(Color.TRANSPARENT);
                selectedRating = "Sad";
                changeDiaryDateRating();
            }
        });

        imgDiaryDayMeh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgDiaryDayMeh.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                imgDiaryDaySad.setBackgroundColor(Color.TRANSPARENT);
                imgDiaryDayHappy.setBackgroundColor(Color.TRANSPARENT);
                selectedRating = "Meh";
                changeDiaryDateRating();
            }
        });

        imgDiaryDayHappy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgDiaryDayHappy.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                imgDiaryDaySad.setBackgroundColor(Color.TRANSPARENT);
                imgDiaryDayMeh.setBackgroundColor(Color.TRANSPARENT);
                selectedRating = "Happy";
                changeDiaryDateRating();
            }
        });

        if(diaryDay.getRating() != null && diaryDay.getRating().equals("Sad"))
        {
            imgDiaryDaySad.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
        else if(diaryDay.getRating() != null && diaryDay.getRating().equals("Meh"))
        {
            imgDiaryDayMeh.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
        else if(diaryDay.getRating() != null && diaryDay.getRating().equals("Happy"))
        {
            imgDiaryDayHappy.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }

        cmdDeleteDiaryDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDiaryDay();
            }
        });
    }

    private void deleteDiaryDay() {
        db.collection(DIARYCOLLECTION).document(diaryDay.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(), "Diary entry deleted", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Diary Day deleted with id: " + diaryDay.getId());
                    onBackPressed();
                }
            }
        });
    }

    private void setDiaryDays()
    {
        if (diaryDay.getTimes() == null || diaryDay.getTimes().size() == 0)
        {
            Toast.makeText(getApplicationContext(), "No diary times found", Toast.LENGTH_SHORT).show();
        }
        diaryTimesRecyclerAdapter = new DiaryTimesRecyclerAdapter(DiaryDayActivity.this, diaryDay);
        RVDiaryTimes.setAdapter(diaryTimesRecyclerAdapter);
    }

    private void changeDiaryDateRating()
    {
        diaryDay.setRating(selectedRating);
        Map<String, Object> diaryDayMap = mapper.DiaryDayToHashMap(diaryDay);

        CollectionReference collectionReference =  db.collection(DIARYCOLLECTION);
        final DocumentReference documentReference = collectionReference.document(diaryDay.getId());
        documentReference.set(diaryDayMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(), "Rating updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "DiaryDay updated with ID: " + documentReference.getId());
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "DiaryDay update failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "DiaryDay update failed. " + task.getException().getMessage());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        db.collection(DIARYCOLLECTION).document(diaryDay.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    diaryDay = mapper.DocumentToDiaryDay(task.getResult());
                    setDiaryDays();
                }
            }
        });
    }
}
