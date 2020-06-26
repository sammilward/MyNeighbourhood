package com.sammilward.neighbourhood.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
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

import com.bumptech.glide.signature.ObjectKey;
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

import static com.sammilward.neighbourhood.ui.login.Constants.EVENTSCOLLECTION;
import static com.sammilward.neighbourhood.ui.login.Constants.EVENTSPICTUERESDIRECTORY;
import static com.sammilward.neighbourhood.ui.login.Constants.USERSCOLLECTION;

public class ViewEditEventActivity extends AppCompatActivity {

    private final String TAG = "ViewEditEventActivity";
    public static final int GET_FROM_GALLERY = 3;

    private EditText txtName, txtDesciption;
    private TextView lblStartDate, lblEndDate, lblStartDateLabel, lblEndDateLabel;
    private ImageView imgPhoto;
    private Button cmdSave, cmdChangePhoto, cmdChooseStartDate, cmdChooseEndDate, cmdRemove;
    private ProgressBar pbViewEditSave;

    private DatePickerDialog datePickerDialog;

    private FirebaseUserMapper firebaseUserMapper;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;
    private Uri selectedImage;

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_edit_event);

        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseUserMapper = new FirebaseUserMapper();

        Intent intent = getIntent();
        event = intent.getParcelableExtra("EVENT");


        initialiseForm();
        displayData();
    }

    private void initialiseForm() {
        txtName = findViewById(R.id.txtViewEditEventName);
        txtDesciption = findViewById(R.id.txtViewEditEventDescription);
        imgPhoto = findViewById(R.id.imgViewEditEventPhoto);
        cmdChangePhoto = findViewById(R.id.cmdViewEditEventChangePhoto);
        cmdChooseStartDate = findViewById(R.id.cmdViewEditEventChooseStartDate);
        cmdChooseEndDate = findViewById(R.id.cmdViewEditEventChooseEndDate);
        cmdSave = findViewById(R.id.cmdViewEditEventSave);
        cmdRemove = findViewById(R.id.cmdViewEditEventRemove);
        lblStartDate = findViewById(R.id.lblViewEditEventStartDate);
        lblEndDate = findViewById(R.id.lblViewEditEventEndDate);
        lblStartDateLabel = findViewById(R.id.lblViewEditEventStartDateLabel);
        lblEndDateLabel = findViewById(R.id.lblViewEditEventEndDateLabel);
        pbViewEditSave = findViewById(R.id.pbViewEditEvent);

        if(!(event.getCreatedByEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())))
        {
            txtName.setEnabled(false);
            txtDesciption.setEnabled(false);
            cmdChangePhoto.setVisibility(View.GONE);
            cmdRemove.setVisibility(View.GONE);
            cmdSave.setVisibility(View.GONE);
            cmdChooseEndDate.setVisibility(View.GONE);
            cmdChooseStartDate.setVisibility(View.GONE);
            lblStartDateLabel.setVisibility(View.VISIBLE);
            lblEndDateLabel.setVisibility(View.VISIBLE);
            lblStartDate.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            lblEndDate.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }
        else
        {
            cmdChangePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
                }
            });

            cmdSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(validate()) updateEvent();
                }
            });

            cmdRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteEvent();
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
        }
    }

    private void displayData() {
        txtName.setText(event.getName());
        txtDesciption.setText(event.getDescription());
        lblStartDate.setText(new SimpleDateFormat("dd-MM-yyyy").format(event.getStartDate()));
        lblEndDate.setText(new SimpleDateFormat("dd-MM-yyyy").format(event.getEndDate()));
        StorageReference storageReference = mStorageRef.child(event.getImageURL());
        GlideApp.with(this).load(storageReference).signature(new ObjectKey(event.getImageUpdatedEpochTime())).into(imgPhoto);
    }

    private void displayDatePickerForStartDate() {

        DatePickerDialog.OnDateSetListener StartDateSelected = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                lblStartDate.setText(getDateFromDatePicker(datePicker));
            }
        };

        LocalDate date = LocalDate.now();
        datePickerDialog = new DatePickerDialog(this, StartDateSelected, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
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
        datePickerDialog = new DatePickerDialog(this, StartDateSelected, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
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
        ValidationHandler validationHandler = new ValidationHandler();

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

    private void updateEvent() {
        pbViewEditSave.setVisibility(View.VISIBLE);
        event.setName(txtName.getText().toString());
        event.setDescription(txtDesciption.getText().toString());
        event.setCreatedByEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        try {
            event.setStartDate(new SimpleDateFormat("dd-MM-yyyy").parse(lblStartDate.getText().toString()));
            event.setEndDate(new SimpleDateFormat("dd-MM-yyyy").parse(lblEndDate.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(!(selectedImage == null))
        {
            final String storageLocation = EVENTSPICTUERESDIRECTORY+event.getId();
            final StorageReference photoRef = mStorageRef.child(storageLocation);

            photoRef.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        event.setImageURL(storageLocation);
                        event.setImageUpdatedEpochTime(Instant.now().getEpochSecond());
                        updateFirebase();
                    }
                    else
                    {
                        Log.e(TAG, "putFile: failure. " + task.getException().getMessage());
                    }
                }
            });
        }
        else
        {
            updateFirebase();
        }

    }

    private void updateFirebase() {
        Map<String, Object> eventMap = new FirebaseEventMapper().EventToHashmap(event);
        CollectionReference collectionReference =  db.collection(EVENTSCOLLECTION);
        final DocumentReference documentReference = collectionReference.document(event.getId());
        documentReference.set(eventMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    pbViewEditSave.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Event updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Event updated with ID: " + documentReference.getId());
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Event update failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Event update failed. " + task.getException().getMessage());
                }
            }
        });
    }

    private void deleteEvent() {
        pbViewEditSave.setVisibility(View.VISIBLE);
        final String storageLocation = EVENTSPICTUERESDIRECTORY+event.getId();
        final StorageReference photoRef = mStorageRef.child(storageLocation);
        photoRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    CollectionReference collectionReference =  db.collection(EVENTSCOLLECTION);
                    final DocumentReference documentReference = collectionReference.document(event.getId());
                    documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                user.setNumberEvents(user.getNumberEvents() - 1);
                                                Map<String, Object> userMap = firebaseUserMapper.UserToHashMap(user);
                                                DocumentReference documentReference = collectionReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                documentReference.set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            pbViewEditSave.setVisibility(View.INVISIBLE);
                                                            Log.d(TAG, "Event removed with ID: " + event.getId());
                                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(intent);
                                                        }
                                                        else
                                                        {
                                                            pbViewEditSave.setVisibility(View.INVISIBLE);
                                                            Toast.makeText(getApplicationContext(), "Profile update failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            Log.e(TAG, "UserDocument update failed. " + task.getException().getMessage());
                                                        }
                                                    }
                                                });

                                            }
                                            else
                                            {
                                                pbViewEditSave.setVisibility(View.INVISIBLE);
                                                Log.e(TAG, "getCurrentUser: Failed. " + task.getException().getMessage());
                                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        else
                                        {
                                            pbViewEditSave.setVisibility(View.INVISIBLE);
                                            Log.e(TAG, "getCurrentUser: Failed. " + task.getException().getMessage());
                                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
                else
                {
                    pbViewEditSave.setVisibility(View.INVISIBLE);
                    Log.e(TAG, "deletingImage: Failed. " + task.getException().getMessage());
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
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
}
