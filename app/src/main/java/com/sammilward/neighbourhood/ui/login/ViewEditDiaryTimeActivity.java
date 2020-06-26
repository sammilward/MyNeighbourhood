package com.sammilward.neighbourhood.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.signature.ObjectKey;
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
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import static com.sammilward.neighbourhood.ui.login.Constants.DIARYAUDIODIRECTORY;
import static com.sammilward.neighbourhood.ui.login.Constants.DIARYCOLLECTION;
import static com.sammilward.neighbourhood.ui.login.Constants.DIARYPICTUREDIRECTORY;

public class ViewEditDiaryTimeActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {

    private final String TAG = "ViewEditDiaryTimeActivity";
    private final int GET_FROM_GALLERY = 3;

    private DiaryDay diaryDay;
    private DiaryTime diaryTime;
    private int diaryTimeIndex;

    private TextView lblDiaryEntryTime, lblAudioMessage, lblDiaryTimeNote;
    private Button cmdChangeDiaryTimePhoto, cmdViewEditDiaryTimeSave, cmdDeleteDiaryTime;
    private ImageView imgDiaryTimePhoto;
    private ImageButton cmdAudioRecord, cmdAudioPlay, cmdAudioStop;
    private EditText txtDiaryTimeNotes;
    private ProgressBar pbViewEditDiaryTime, pbAudioLoading;

    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private FirebaseDiaryDayMapper firebaseDiaryDayMapper;
    private Uri selectedImage;

    private String fileName;
    private MediaRecorder mediaRecorder;
    private boolean recording = false;

    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_edit_diary_time);

        Intent intent = getIntent();
        diaryDay = intent.getParcelableExtra("DIARYDAY");
        diaryTimeIndex = intent.getIntExtra("DIARYTIMEINDEX", 0);
        diaryTime = diaryDay.getTimes().get(diaryTimeIndex);
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
        cmdChangeDiaryTimePhoto = findViewById(R.id.cmdChangeDiaryTimePhoto);
        cmdViewEditDiaryTimeSave = findViewById(R.id.cmdViewEditDiaryTimeSave);
        imgDiaryTimePhoto = findViewById(R.id.imgDiaryTimePhoto);
        cmdAudioRecord = findViewById(R.id.cmdAudioRecord);
        cmdAudioPlay = findViewById(R.id.cmdAudioPlay);
        cmdAudioStop = findViewById(R.id.cmdAudioStop);
        cmdDeleteDiaryTime = findViewById(R.id.cmdDeleteDiaryTime);
        txtDiaryTimeNotes = findViewById(R.id.txtDiaryTimeNotes);
        pbViewEditDiaryTime = findViewById(R.id.pbViewEditDiaryTime);
        pbAudioLoading = findViewById(R.id.pbAudioLoading);

        cmdChangeDiaryTimePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
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
                File file = new File(fileName);
                if(file.exists()) {
                    try {
                        mediaPlayer.setDataSource(fileName);
                        mediaPlayer.setOnPreparedListener(ViewEditDiaryTimeActivity.this);
                        mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (diaryTime.getAudioURL() != null)
                {
                    pbAudioLoading.setVisibility(View.VISIBLE);
                    cmdAudioPlay.setVisibility(View.GONE);
                    lblAudioMessage.setText("Streaming voice note...");

                    StorageReference audioFileRef = FirebaseStorage.getInstance().getReference(diaryTime.getAudioURL());
                    audioFileRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful())
                            {
                                final String audioUrl = task.getResult().toString();
                                try {
                                    mediaPlayer.setDataSource(audioUrl);
                                    mediaPlayer.setOnPreparedListener(ViewEditDiaryTimeActivity.this);
                                    mediaPlayer.prepareAsync();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
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

        cmdViewEditDiaryTimeSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cmdViewEditDiaryTimeSave.setEnabled(false);
                saveDairyTime();
            }
        });

        cmdDeleteDiaryTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete();
            }
        });

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        lblDiaryEntryTime.setText(diaryTime.getTime().format(dateTimeFormatter));
        txtDiaryTimeNotes.setText(diaryTime.getDescription());

        if (diaryTime.getImageURL() != null)
        {
            StorageReference storageReference = mStorageRef.child(diaryTime.getImageURL());
            GlideApp.with(this).load(storageReference).signature(new ObjectKey(diaryTime.getImageUpdatedEpochTime())).into(imgDiaryTimePhoto);
        }
        else cmdChangeDiaryTimePhoto.setText("Upload photo");

        if (diaryTime.getAudioURL() != null)
        {
            lblAudioMessage.setText("Audio ready to play");
        }
        else
        {
            lblAudioMessage.setText("Click the microphone to record");
        }
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
        lblAudioMessage.setText("Audio ready to play");
        Toast.makeText(getApplicationContext(), "Recording stopped", Toast.LENGTH_SHORT).show();

        recording = false;
    }

    private void delete() {
        diaryDay.getTimes().remove(diaryTimeIndex);
        Map<String, Object> diaryDayMap = firebaseDiaryDayMapper.DiaryDayToHashMap(diaryDay);

        CollectionReference collectionReference =  db.collection(DIARYCOLLECTION);
        final DocumentReference documentReference = collectionReference.document(diaryDay.getId());
        documentReference.set(diaryDayMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    pbViewEditDiaryTime.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Removed", Toast.LENGTH_SHORT).show();
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

    private void saveDairyTime() {
        pbViewEditDiaryTime.setVisibility(View.VISIBLE);
        File file = new File(fileName);
        if (file.exists() && selectedImage != null) uploadImageAndAudioThenAddDiaryTime();
        else if (file.exists()) uploadAudioThenAddDiaryTime();
        else if (selectedImage != null) uploadImageThenAddDiaryTime();
        else onlyAddDiaryTime();
    }

    private void uploadImageAndAudioThenAddDiaryTime() {
        StorageReference audioRef;
        String audioLocation = null;

        if (diaryTime.getAudioURL() == null)
        {
            final String diaryTimeMediaId = UUID.randomUUID().toString();
            audioLocation = DIARYAUDIODIRECTORY+diaryTimeMediaId;
            audioRef = mStorageRef.child(audioLocation);
        }
        else
        {
            audioRef = mStorageRef.child(diaryTime.getAudioURL());
        }

        final String audioLocationFinal = audioLocation;

        Uri audioUri = Uri.fromFile(new File(fileName));
        audioRef.putFile(audioUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    final StorageReference photoRef;
                    String photoLocation = null;

                    if (diaryTime.getImageURL() == null)
                    {
                        final String diaryTimeMediaId = UUID.randomUUID().toString();
                        photoLocation = DIARYPICTUREDIRECTORY+diaryTimeMediaId;
                        photoRef = mStorageRef.child(photoLocation);
                    }
                    else
                    {
                        photoRef = mStorageRef.child(diaryTime.getImageURL());
                    }

                    final String photoLocationFinal = photoLocation;

                    photoRef.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful())
                            {
                                diaryDay.getTimes().get(diaryTimeIndex).setImageUpdatedEpochTime(Instant.now().getEpochSecond());
                                if (!txtDiaryTimeNotes.getText().toString().equals(diaryTime.getDescription())) diaryDay.getTimes().get(diaryTimeIndex).setDescription(txtDiaryTimeNotes.getText().toString());

                                if (diaryTime.getAudioURL() == null) diaryDay.getTimes().get(diaryTimeIndex).setAudioURL(audioLocationFinal);
                                if (diaryTime.getImageURL() == null) diaryDay.getTimes().get(diaryTimeIndex).setImageURL(photoLocationFinal);

                                Map<String, Object> diaryDayMap = firebaseDiaryDayMapper.DiaryDayToHashMap(diaryDay);

                                CollectionReference collectionReference =  db.collection(DIARYCOLLECTION);
                                final DocumentReference documentReference = collectionReference.document(diaryDay.getId());
                                documentReference.set(diaryDayMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            pbViewEditDiaryTime.setVisibility(View.INVISIBLE);
                                            Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
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
        StorageReference audioRef;
        String audioLocation = null;

        if (diaryTime.getAudioURL() == null)
        {
            final String diaryTimeMediaId = UUID.randomUUID().toString();
            audioLocation = DIARYAUDIODIRECTORY+diaryTimeMediaId;
            audioRef = mStorageRef.child(audioLocation);
        }
        else
        {
            audioRef = mStorageRef.child(diaryTime.getAudioURL());
        }

        final String audioLocationFinal = audioLocation;

        Uri audioUri = Uri.fromFile(new File(fileName));
        audioRef.putFile(audioUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    if (!txtDiaryTimeNotes.getText().toString().isEmpty()) diaryDay.getTimes().get(diaryTimeIndex).setDescription(txtDiaryTimeNotes.getText().toString());
                    if (diaryTime.getAudioURL() == null) diaryDay.getTimes().get(diaryTimeIndex).setAudioURL(audioLocationFinal);

                    Map<String, Object> diaryDayMap = firebaseDiaryDayMapper.DiaryDayToHashMap(diaryDay);

                    CollectionReference collectionReference =  db.collection(DIARYCOLLECTION);
                    final DocumentReference documentReference = collectionReference.document(diaryDay.getId());
                    documentReference.set(diaryDayMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                pbViewEditDiaryTime.setVisibility(View.INVISIBLE);
                                Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
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
        final StorageReference photoRef;
        String photoLocation = null;

        if (diaryTime.getImageURL() == null)
        {
            final String diaryTimeMediaId = UUID.randomUUID().toString();
            photoLocation = DIARYPICTUREDIRECTORY+diaryTimeMediaId;
            photoRef = mStorageRef.child(photoLocation);
        }
        else
        {
            photoRef = mStorageRef.child(diaryTime.getImageURL());
        }

        final String photoLocationFinal = photoLocation;

        photoRef.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    diaryDay.getTimes().get(diaryTimeIndex).setImageUpdatedEpochTime(Instant.now().getEpochSecond());
                    if (!txtDiaryTimeNotes.getText().toString().isEmpty()) diaryDay.getTimes().get(diaryTimeIndex).setDescription(txtDiaryTimeNotes.getText().toString());
                    if (diaryTime.getImageURL() == null) diaryDay.getTimes().get(diaryTimeIndex).setImageURL(photoLocationFinal);

                    Map<String, Object> diaryDayMap = firebaseDiaryDayMapper.DiaryDayToHashMap(diaryDay);

                    CollectionReference collectionReference =  db.collection(DIARYCOLLECTION);
                    final DocumentReference documentReference = collectionReference.document(diaryDay.getId());
                    documentReference.set(diaryDayMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                pbViewEditDiaryTime.setVisibility(View.INVISIBLE);
                                Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
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
        pbViewEditDiaryTime.setVisibility(View.VISIBLE);
        diaryDay.getTimes().get(diaryTimeIndex).setDescription(txtDiaryTimeNotes.getText().toString());
        Map<String, Object> diaryDayMap = firebaseDiaryDayMapper.DiaryDayToHashMap(diaryDay);

        CollectionReference collectionReference =  db.collection(DIARYCOLLECTION);
        final DocumentReference documentReference = collectionReference.document(diaryDay.getId());
        documentReference.set(diaryDayMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    pbViewEditDiaryTime.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "DiaryDay updated with ID: " + documentReference.getId());
                    onBackPressed();
                }
                else
                {
                    pbViewEditDiaryTime.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "DiaryDay update failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "DiaryDay update failed. " + task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        pbAudioLoading.setVisibility(View.GONE);
        cmdAudioPlay.setVisibility(View.VISIBLE);
        lblAudioMessage.setText("Audio ready to play");
        mp.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                imgDiaryTimePhoto.setImageBitmap(bitmap);
                cmdChangeDiaryTimePhoto.setText("Change photo");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
