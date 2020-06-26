package com.sammilward.neighbourhood.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sammilward.neighbourhood.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.sammilward.neighbourhood.ui.login.Constants.EVENTSCOLLECTION;
import static com.sammilward.neighbourhood.ui.login.Constants.EVENTSPICTUERESDIRECTORY;
import static com.sammilward.neighbourhood.ui.login.Constants.USERSCOLLECTION;

public class AddEventActivity extends AppCompatActivity {

    private final String TAG = "AddEventActivity";

    private TextView lblStartDate, lblEndDate;
    private EditText txtName, txtDesciption;
    private ImageView imgPhoto;
    private Button cmdSave, cmdChangePhoto, cmdChooseStartDate, cmdChooseEndDate;
    private ProgressBar pb;
    private DatePickerDialog datePickerDialog;

    private final int GET_FROM_GALLERY = 3;
    private StorageReference mStorageRef;

    private FirebaseEventMapper firebaseEventMapper;
    private FirebaseUserMapper firebaseUserMapper;
    private FirebaseFirestore db;
    private ValidationHandler validationHandler;
    private Event event;

    private Uri selectedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        Intent intent = getIntent();
        event = intent.getParcelableExtra("EVENT");

        firebaseEventMapper = new FirebaseEventMapper();
        db = FirebaseFirestore.getInstance();
        validationHandler = new ValidationHandler();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseUserMapper = new FirebaseUserMapper();

        txtName = findViewById(R.id.txtAddEventName);
        txtDesciption = findViewById(R.id.txtAddEventDescription);
        imgPhoto = findViewById(R.id.imgAddEventPhoto);
        lblStartDate = findViewById(R.id.lblAddEventStartDate);
        lblEndDate = findViewById(R.id.lblAddEventEndDate);
        cmdChangePhoto = findViewById(R.id.cmdAddEventChangePhoto);
        cmdChooseStartDate = findViewById(R.id.cmdAddEventChooseStartDate);
        cmdChooseEndDate = findViewById(R.id.cmdAddEventChooseEndDate);
        cmdSave = findViewById(R.id.cmdAddEventSave);
        pb = findViewById(R.id.pbAddEvent);


        cmdChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        cmdChooseStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDatePickerForStartDate();
            }
        });

        cmdChooseEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDatePickerForEndDate();
            }
        });

        cmdSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate())
                {
                    pb.setVisibility(View.VISIBLE);
                    createNewEvent();
                }
            }
        });
    }

    private void createNewEvent() {
        pb.setVisibility(View.VISIBLE);
        final String eventID = UUID.randomUUID().toString();
        final String storageLocation = EVENTSPICTUERESDIRECTORY+eventID;
        final StorageReference photoRef = mStorageRef.child(storageLocation);

        String city = "";
        try {
            city = new GetCity().execute(event.getLocation()).get();
        } catch (Exception e) {
            e.printStackTrace();
            pb.setVisibility(View.INVISIBLE);
        }

        final String confirmedCity = city;


        photoRef.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {
                    addEventData();
                    event.setImageURL(storageLocation);
                    event.setImageUpdatedEpochTime(Instant.now().getEpochSecond());
                    event.setCity(confirmedCity);
                    Map<String, Object> eventMap = firebaseEventMapper.EventToHashmap(event);

                    CollectionReference collectionReference =  db.collection(EVENTSCOLLECTION);
                    DocumentReference documentReference = collectionReference.document(eventID);
                    documentReference.set(eventMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {

                                final CollectionReference collectionReference =  db.collection(USERSCOLLECTION);
                                DocumentReference documentReference = collectionReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful())
                                        {
                                            if(task.getResult().exists())
                                            {
                                                User user = firebaseUserMapper.DocumentToUser(task.getResult());
                                                user.setNumberEvents(user.getNumberEvents() + 1);
                                                Map<String, Object> userMap = firebaseUserMapper.UserToHashMap(user);
                                                DocumentReference documentReference = collectionReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                documentReference.set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            pb.setVisibility(View.INVISIBLE);
                                                            Toast.makeText(getApplicationContext(), "Event created", Toast.LENGTH_SHORT).show();
                                                            Log.d(TAG, "Event added with ID: " + eventID);
                                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(intent);
                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(getApplicationContext(), "Profile update failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            Log.e(TAG, "UserDocument update failed. " + task.getException().getMessage());
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
                                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Log.e(TAG, "Event upload failed. " + task.getException().getMessage());
                            }
                        }
                    });
                }
                else
                {
                    Log.e(TAG, "putFile: failure. " + task.getException().getMessage());
                }
            }
        });
    }
    private void addEventData()
    {
        event.setName(txtName.getText().toString());
        event.setDescription(txtDesciption.getText().toString());
        event.setCreatedByEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        try {
            event.setStartDate(new SimpleDateFormat("dd-MM-yyyy").parse(lblStartDate.getText().toString()));
            event.setEndDate(new SimpleDateFormat("dd-MM-yyyy").parse(lblEndDate.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void displayDatePickerForStartDate() {

        DatePickerDialog.OnDateSetListener StartDateSelected = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                lblStartDate.setText(getDateFromDatePicker(datePicker));
            }
        };

        LocalDate date = LocalDate.now();
        datePickerDialog = new DatePickerDialog(this, StartDateSelected, date.getYear(), date.getMonthValue()-1, date.getDayOfMonth());
        datePickerDialog.show();
    }

    private void displayDatePickerForEndDate() {

        DatePickerDialog.OnDateSetListener StartDateSelected = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                lblEndDate.setText(getDateFromDatePicker(datePicker));
            }
        };

        LocalDate date = LocalDate.now();
        datePickerDialog = new DatePickerDialog(this, StartDateSelected, date.getYear(), date.getMonthValue()-1, date.getDayOfMonth());
        datePickerDialog.show();
    }

    private String getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return formatDate(calendar.getTime());
    }

    private String formatDate(Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(date.getTime());
    }

    private boolean validate() {
        boolean valid = true;

        if(selectedImage == null)
        {
            cmdChangePhoto.setTextColor(getResources().getColor(R.color.colorRed));
            valid = false;
        }
        else cmdChangePhoto.setTextColor(getResources().getColor(R.color.colorAccent));

        if(txtName.getText().toString().isEmpty())
        {
            validationHandler.displayValidationError(txtName, "Can't be left blank");
            Log.i(TAG, "validate:failure. Event name left blank.");
            valid = false;
        }
        else validationHandler.removeValidationError(txtName);

        if(!validationHandler.isEventNameValid(txtName.getText().toString()))
        {
            validationHandler.displayValidationError(txtName, "Must only contain letters");
            Log.i(TAG, "validate:failure. Event name name invalid.");
            valid = false;
        }
        else validationHandler.removeValidationError(txtName);

        if(txtDesciption.getText().toString().isEmpty())
        {
            validationHandler.displayValidationError(txtDesciption, "Can't be left blank");
            Log.i(TAG, "validate:failure. Event description left blank.");
            valid = false;
        }
        else validationHandler.removeValidationError(txtDesciption);

        if(lblStartDate.getText().toString().isEmpty())
        {
            Log.i(TAG, "validate:failure. Event Start date left blank.");
            cmdChooseStartDate.setBackgroundColor(Color.RED);
            valid = false;
        } else cmdChooseStartDate.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        if(lblEndDate.getText().toString().isEmpty())
        {
            Log.i(TAG, "validate:failure. Event End date left blank.");
            cmdChooseEndDate.setBackgroundColor(Color.RED);
            valid = false;
        } else cmdChooseEndDate.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        return valid;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), selectedImage);
                imgPhoto.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetCity extends AsyncTask<LatLng, Void, String> {

        @Override
        protected String doInBackground(com.google.android.gms.maps.model.LatLng... latLngs) {
            String city = "";
            try {
                com.google.android.gms.maps.model.LatLng latLng = latLngs[0];
                Geocoder geocoder = new Geocoder();
                city = geocoder.GetCityFromLatLon(latLng);
            }
            catch (Exception e)
            {
                Log.e(TAG, e.getMessage());
            }
            finally {
                return city;
            }
        }
    }
}
