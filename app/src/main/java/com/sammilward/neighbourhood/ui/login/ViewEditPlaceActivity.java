package com.sammilward.neighbourhood.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import java.time.Instant;
import java.util.Map;

import static com.sammilward.neighbourhood.ui.login.Constants.PLACESCOLLECTION;
import static com.sammilward.neighbourhood.ui.login.Constants.PLACESPICTURESDIRECTORY;
import static com.sammilward.neighbourhood.ui.login.Constants.USERSCOLLECTION;

public class ViewEditPlaceActivity extends AppCompatActivity {

    private final String TAG = "ViewEditPlaceActivity";
    public static final int GET_FROM_GALLERY = 3;

    private EditText txtName, txtDesciption;
    private ImageView imgPhoto;
    private Button cmdSave, cmdChangePhoto, cmdRemove;
    private ProgressBar pbViewEditSave;

    private FirebaseUserMapper firebaseUserMapper;

    private FirebaseFirestore db;
    private StorageReference mStorageRef;
    private Uri selectedImage;

    private Place place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_edit_place);
        firebaseUserMapper = new FirebaseUserMapper();
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        place = intent.getParcelableExtra("PLACE");


        initialiseForm();
        displayData();
    }

    private void initialiseForm() {
        txtName = findViewById(R.id.txtViewEditPlaceName);
        txtDesciption = findViewById(R.id.txtViewEditPlaceDescription);
        imgPhoto = findViewById(R.id.imgViewEditPlacePhoto);
        cmdChangePhoto = findViewById(R.id.cmdViewEditPlaceChangePhoto);
        cmdSave = findViewById(R.id.cmdViewEditPlaceSave);
        cmdRemove = findViewById(R.id.cmdViewEditPlaceRemove);
        pbViewEditSave = findViewById(R.id.pbViewEditPlace);

        if(!(place.getCreatedByEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())))
        {
            txtName.setEnabled(false);
            txtDesciption.setEnabled(false);
            cmdChangePhoto.setVisibility(View.GONE);
            cmdRemove.setVisibility(View.GONE);
            cmdSave.setVisibility(View.GONE);
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
                    if(validate()) updatePlace();
                }
            });

            cmdRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deletePlace();
                }
            });
        }
    }

    private void displayData() {
        txtName.setText(place.getName());
        txtDesciption.setText(place.getDescription());
        StorageReference storageReference = mStorageRef.child(place.getImageURL());
        GlideApp.with(this).load(storageReference).signature(new ObjectKey(place.getImageUpdatedEpochTime())).into(imgPhoto);
    }

    private void updatePlace() {
        pbViewEditSave.setVisibility(View.VISIBLE);
        place.setName(txtName.getText().toString());
        place.setDescription(txtDesciption.getText().toString());
        place.setCreatedByEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        if(!(selectedImage == null))
        {
            final String storageLocation = PLACESPICTURESDIRECTORY+place.getId();
            final StorageReference photoRef = mStorageRef.child(storageLocation);

            photoRef.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        place.setImageURL(storageLocation);
                        place.setImageUpdatedEpochTime(Instant.now().getEpochSecond());
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
        Map<String, Object> placeMap = new FirebasePlaceMapper().PlaceToHashmap(place);
        CollectionReference collectionReference =  db.collection(PLACESCOLLECTION);
        final DocumentReference documentReference = collectionReference.document(place.getId());
        documentReference.set(placeMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    pbViewEditSave.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Place updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Place updated with ID: " + documentReference.getId());
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Place update failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Place update failed. " + task.getException().getMessage());
                }
            }
        });
    }

    private void deletePlace()
    {
        pbViewEditSave.setVisibility(View.VISIBLE);
        final String storageLocation = PLACESPICTURESDIRECTORY+place.getId();
        final StorageReference photoRef = mStorageRef.child(storageLocation);
        photoRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    CollectionReference collectionReference =  db.collection(PLACESCOLLECTION);
                    final DocumentReference documentReference = collectionReference.document(place.getId());
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
                                                user.setNumberPlaces(user.getNumberPlaces() - 1);
                                                Map<String, Object> userMap = firebaseUserMapper.UserToHashMap(user);
                                                DocumentReference documentReference = collectionReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                documentReference.set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            pbViewEditSave.setVisibility(View.INVISIBLE);
                                                            Log.d(TAG, "Place removed with ID: " + place.getId());
                                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(intent);
                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(getApplicationContext(), "Place update failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                        }
                    });
                }
            }
        });
    }

    private boolean validate() {
        boolean valid = true;
        ValidationHandler validationHandler = new ValidationHandler();

        if(txtName.getText().toString().isEmpty())
        {
            validationHandler.displayValidationError(txtName, "Can't be left blank");
            Log.i(TAG, "validate:failure. Place name left blank.");
            valid = false;
        }
        else validationHandler.removeValidationError(txtName);

        if(!validationHandler.isNameValid(txtName.getText().toString()))
        {
            validationHandler.displayValidationError(txtName, "Must only contain letters");
            Log.i(TAG, "validate:failure. Place name name invalid.");
            valid = false;
        }
        else validationHandler.removeValidationError(txtName);

        if(txtDesciption.getText().toString().isEmpty())
        {
            validationHandler.displayValidationError(txtDesciption, "Can't be left blank");
            Log.i(TAG, "validate:failure. Place description left blank.");
            valid = false;
        }
        else validationHandler.removeValidationError(txtDesciption);

        return valid;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                imgPhoto.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
