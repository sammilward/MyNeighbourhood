package com.sammilward.neighbourhood.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sammilward.neighbourhood.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

import static com.sammilward.neighbourhood.ui.login.Constants.DIARYAUDIODIRECTORY;
import static com.sammilward.neighbourhood.ui.login.Constants.DIARYCOLLECTION;
import static com.sammilward.neighbourhood.ui.login.Constants.DIARYPICTUREDIRECTORY;

public class AddDiaryTimeActivity extends AppCompatActivity {

    private final String TAG = "AddDiaryTimeActivity";
    private final int GET_FROM_GALLERY = 3;

    private DiaryDay diaryDay;

    private TextView lblDiaryEntryTime, lblAudioMessage;
    private Button cmdChooseTime, cmdAddDiaryTimePhoto, cmdAddDiaryTimeSave;
    private ImageView imgDiaryTimePhoto;
    private ImageButton cmdAudioRecord, cmdAudioPlay, cmdAudioStop;
    private EditText txtDiaryTimeNotes;
    private ProgressBar pbAddDiaryTime;

    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private FirebaseDiaryDayMapper firebaseDiaryDayMapper;
    private Uri selectedImage;

    private String fileName;
    private MediaRecorder mediaRecorder;
    private boolean recording = false;

    private MediaPlayer mediaPlayer;

    private int selectedHour, selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary_time);

        Intent intent = getIntent();
        diaryDay = intent.getParcelableExtra("DIARYDAY");

        initialise();
    }

    private void initialise()
    {
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        firebaseDiaryDayMapper = new FirebaseDiaryDayMapper();

        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/temporaryaudio.3gp";

        removeLocalAudioFile();

        lblDiaryEntryTime = findViewById(R.id.lblDiaryEntryTime);
        lblAudioMessage = findViewById(R.id.lblAudioMessage);
        cmdChooseTime = findViewById(R.id.cmdChooseTime);
        cmdAddDiaryTimePhoto = findViewById(R.id.cmdAddDiaryTimePhoto);
        cmdAddDiaryTimeSave = findViewById(R.id.cmdAddDiaryTimeSave);
        imgDiaryTimePhoto = findViewById(R.id.imgDiaryTimePhoto);
        cmdAudioRecord = findViewById(R.id.cmdAudioRecord);
        cmdAudioPlay = findViewById(R.id.cmdAudioPlay);
        cmdAudioStop = findViewById(R.id.cmdAudioStop);
        txtDiaryTimeNotes = findViewById(R.id.txtDiaryTimeNotes);
        pbAddDiaryTime = findViewById(R.id.pbAddDiaryTime);

        cmdAddDiaryTimePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        cmdChooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mCurrentTime = Calendar.getInstance();
                int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mCurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddDiaryTimeActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHourInt, int selectedMinuteInt) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                        LocalTime time = LocalTime.of(selectedHourInt, selectedMinuteInt);
                        lblDiaryEntryTime.setText(time.format(dateTimeFormatter));
                        selectedHour = selectedHourInt;
                        selectedMinute = selectedMinuteInt;
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        cmdAudioRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!recording) startRecord();
                else stopRecord();
            }
        });

        cmdAudioPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(fileName);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
            }
        });

        cmdAudioStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        });

        cmdAddDiaryTimeSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(valid())
                {
                    cmdAddDiaryTimeSave.setEnabled(false);
                    saveDairyTime();
                }
            }
        });
    }

    private void removeLocalAudioFile() {
        File file = new File(fileName);
        file.delete();
        if(file.exists()){
            try {
                file.getCanonicalFile().delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(file.exists()){
                getApplicationContext().deleteFile(file.getName());
            }
        }
    }

    private void startRecord()
    {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(fileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
        cmdAudioRecord.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorRed), android.graphics.PorterDuff.Mode.SRC_IN);
        lblAudioMessage.setTextColor(getResources().getColor(R.color.colorRed));
        lblAudioMessage.setText("Recording");
        Toast.makeText(getApplicationContext(), "Recording using microphone", Toast.LENGTH_SHORT).show();

        recording = true;
    }

    private void stopRecord()
    {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        cmdAudioRecord.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
        lblAudioMessage.setTextColor(getResources().getColor(R.color.colorAccent));
        lblAudioMessage.setText("Audio Recorded");
        Toast.makeText(getApplicationContext(), "Recording stopped", Toast.LENGTH_SHORT).show();

        recording = false;
    }

    private boolean valid()
    {
        boolean valid = true;
        boolean mediaExist = false;

        if (lblDiaryEntryTime.getText().toString().isEmpty())
        {
            valid = false;
            cmdChooseTime.setBackgroundColor(Color.RED);
        }
        else
        {
            cmdChooseTime.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        File file = new File(fileName);
        if (file.exists()) mediaExist = true;
        if (selectedImage != null) mediaExist = true;
        if (!txtDiaryTimeNotes.getText().toString().isEmpty()) mediaExist = true;

        if(!mediaExist) return false;

        return valid;
    }

    private void saveDairyTime() {
        pbAddDiaryTime.setVisibility(View.VISIBLE);

        File file = new File(fileName);
        if (file.exists() && selectedImage != null) uploadImageAndAudioThenAddDiaryTime();
        else if (file.exists()) uploadAudioThenAddDiaryTime();
        else if (selectedImage != null) uploadImageThenAddDiaryTime();
        else onlyAddDiaryTime();
    }

    private void uploadImageAndAudioThenAddDiaryTime() {
        final String diaryTimeMediaId = UUID.randomUUID().toString();
        final String audioLocation = DIARYAUDIODIRECTORY+diaryTimeMediaId;
        final StorageReference audioRef = mStorageRef.child(audioLocation);
        Uri audioUri = Uri.fromFile(new File(fileName));

        audioRef.putFile(audioUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    final String photoLocation = DIARYPICTUREDIRECTORY+diaryTimeMediaId;
                    final StorageReference photoRef = mStorageRef.child(photoLocation);

                    photoRef.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful())
                            {
                                DiaryTime newDiaryTime = new DiaryTime();
                                newDiaryTime.setAudioURL(audioLocation);
                                newDiaryTime.setTime(LocalDateTime.of(diaryDay.getDate(), LocalTime.of(selectedHour, selectedMinute)));
                                newDiaryTime.setImageURL(photoLocation);
                                newDiaryTime.setImageUpdatedEpochTime(Instant.now().getEpochSecond());
                                if (!txtDiaryTimeNotes.getText().toString().isEmpty()) newDiaryTime.setDescription(txtDiaryTimeNotes.getText().toString());

                                diaryDay.getTimes().add(newDiaryTime);
                                Map<String, Object> diaryDayMap = firebaseDiaryDayMapper.DiaryDayToHashMap(diaryDay);

                                CollectionReference collectionReference =  db.collection(DIARYCOLLECTION);
                                final DocumentReference documentReference = collectionReference.document(diaryDay.getId());
                                documentReference.set(diaryDayMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            pbAddDiaryTime.setVisibility(View.INVISIBLE);
                                            Toast.makeText(getApplicationContext(), "DiaryDay updated", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "DiaryDay updated with ID: " + documentReference.getId());
                                            onBackPressed();
                                        }
                                        else
                                        {
                                            Toast.makeText(getApplicationContext(), "DiaryDay update failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "DiaryDay update failed. " + task.getException().getMessage());
                                        }
                                    }
                                });

                            }
                            else
                            {
                                Toast.makeText(getApplicationContext() , task.getException().getMessage() , Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(getApplicationContext() , task.getException().getMessage() , Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void uploadAudioThenAddDiaryTime() {

        final String diaryTimeAudioId = UUID.randomUUID().toString();
        final String storageLocation = DIARYAUDIODIRECTORY+diaryTimeAudioId;
        final StorageReference audioRef = mStorageRef.child(storageLocation);
        Uri audioUri = Uri.fromFile(new File(fileName));

        audioRef.putFile(audioUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    DiaryTime newDiaryTime = new DiaryTime();
                    newDiaryTime.setTime(LocalDateTime.of(diaryDay.getDate(), LocalTime.of(selectedHour, selectedMinute)));
                    newDiaryTime.setAudioURL(storageLocation);
                    if (!txtDiaryTimeNotes.getText().toString().isEmpty()) newDiaryTime.setDescription(txtDiaryTimeNotes.getText().toString());

                    diaryDay.getTimes().add(newDiaryTime);
                    Map<String, Object> diaryDayMap = firebaseDiaryDayMapper.DiaryDayToHashMap(diaryDay);

                    CollectionReference collectionReference =  db.collection(DIARYCOLLECTION);
                    final DocumentReference documentReference = collectionReference.document(diaryDay.getId());
                    documentReference.set(diaryDayMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                pbAddDiaryTime.setVisibility(View.INVISIBLE);
                                Toast.makeText(getApplicationContext(), "DiaryDay updated", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "DiaryDay updated with ID: " + documentReference.getId());
                                onBackPressed();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "DiaryDay update failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "DiaryDay update failed. " + task.getException().getMessage());
                            }
                        }
                    });

                }
                else
                {
                    Toast.makeText(getApplicationContext() , task.getException().getMessage() , Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void uploadImageThenAddDiaryTime() {
        final String diaryTimePhotoId = UUID.randomUUID().toString();
        final String storageLocation = DIARYPICTUREDIRECTORY+diaryTimePhotoId;
        final StorageReference photoRef = mStorageRef.child(storageLocation);

        photoRef.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    DiaryTime newDiaryTime = new DiaryTime();
                    newDiaryTime.setTime(LocalDateTime.of(diaryDay.getDate(), LocalTime.of(selectedHour, selectedMinute)));
                    newDiaryTime.setImageURL(storageLocation);
                    newDiaryTime.setImageUpdatedEpochTime(Instant.now().getEpochSecond());
                    if (!txtDiaryTimeNotes.getText().toString().isEmpty()) newDiaryTime.setDescription(txtDiaryTimeNotes.getText().toString());

                    diaryDay.getTimes().add(newDiaryTime);
                    Map<String, Object> diaryDayMap = firebaseDiaryDayMapper.DiaryDayToHashMap(diaryDay);


                    CollectionReference collectionReference =  db.collection(DIARYCOLLECTION);
                    final DocumentReference documentReference = collectionReference.document(diaryDay.getId());
                    documentReference.set(diaryDayMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                pbAddDiaryTime.setVisibility(View.INVISIBLE);
                                Toast.makeText(getApplicationContext(), "DiaryDay updated", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "DiaryDay updated with ID: " + documentReference.getId());
                                onBackPressed();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "DiaryDay update failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "DiaryDay update failed. " + task.getException().getMessage());
                            }
                        }
                    });

                }
                else
                {
                    Toast.makeText(getApplicationContext() , task.getException().getMessage() , Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void onlyAddDiaryTime() {
        pbAddDiaryTime.setVisibility(View.VISIBLE);
        DiaryTime newDiaryTime = new DiaryTime();
        newDiaryTime.setTime(LocalDateTime.of(diaryDay.getDate(), LocalTime.of(selectedHour, selectedMinute)));
        newDiaryTime.setDescription(txtDiaryTimeNotes.getText().toString());

        diaryDay.getTimes().add(newDiaryTime);
        Map<String, Object> diaryDayMap = firebaseDiaryDayMapper.DiaryDayToHashMap(diaryDay);

        CollectionReference collectionReference =  db.collection(DIARYCOLLECTION);
        final DocumentReference documentReference = collectionReference.document(diaryDay.getId());
        documentReference.set(diaryDayMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    pbAddDiaryTime.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "DiaryDay updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "DiaryDay updated with ID: " + documentReference.getId());
                    onBackPressed();
                }
                else
                {
                    pbAddDiaryTime.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "DiaryDay update failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "DiaryDay update failed. " + task.getException().getMessage());
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
                imgDiaryTimePhoto.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
