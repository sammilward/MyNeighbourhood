package com.sammilward.neighbourhood.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.UUID;

import static com.sammilward.neighbourhood.ui.login.Constants.PLACESCOLLECTION;
import static com.sammilward.neighbourhood.ui.login.Constants.PLACESPICTURESDIRECTORY;
import static com.sammilward.neighbourhood.ui.login.Constants.USERSCOLLECTION;

public class AddPlaceActivity extends AppCompatActivity {

    private final int GET_FROM_GALLERY = 3;

    private final String TAG = "AddPlaceActivity";

    private EditText txtName, txtDescription;
    private ImageView imgPhoto;
    private Button cmdSave, cmdChangePhoto;
    private ProgressBar pbAddEditPlace;
    private StorageReference mStorageRef;

    private FirebasePlaceMapper firebasePlaceMapper;
    private FirebaseUserMapper firebaseUserMapper;
    private FirebaseFirestore db;
    private ValidationHandler validationHandler;
    private Place place;

    private Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        firebasePlaceMapper = new FirebasePlaceMapper();
        firebaseUserMapper = new FirebaseUserMapper();
        db = FirebaseFirestore.getInstance();
        validationHandler = new ValidationHandler();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        txtName = findViewById(R.id.txtAddPlaceName);
        txtDescription = findViewById(R.id.txtAddPlaceDescription);
        imgPhoto = findViewById(R.id.imgDiaryTimePhoto);
        cmdChangePhoto = findViewById(R.id.cmdAddDiaryTimePhoto);
        cmdSave = findViewById(R.id.cmdAddDiaryTimeSave);
        pbAddEditPlace = findViewById(R.id.pbAddPlace);

        Intent intent = getIntent();
        place = intent.getParcelableExtra("PLACE");

        cmdChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        cmdSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate())
                {
                    pbAddEditPlace.setVisibility(View.VISIBLE);
                    CreateNewPlace();
                }
            }
        });
    }

    public void CreateNewPlace()
    {

        pbAddEditPlace.setVisibility(View.VISIBLE);
        final String placeID = UUID.randomUUID().toString();
        final String storageLocation = PLACESPICTURESDIRECTORY+placeID;
        final StorageReference photoRef = mStorageRef.child(storageLocation);

        String city = "";
        try {
            city = new GetCity().execute(place.getLocation()).get();
        } catch (Exception e) {
            e.printStackTrace();
            pbAddEditPlace.setVisibility(View.INVISIBLE);
        }

        final String confirmedCity = city;

        photoRef.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {

                    addPlaceData();
                    place.setImageURL(storageLocation);
                    place.setImageUpdatedEpochTime(Instant.now().getEpochSecond());
                    place.setCity(confirmedCity);
                    Map<String, Object> placeMap = firebasePlaceMapper.PlaceToHashmap(place);

                    CollectionReference collectionReference =  db.collection(PLACESCOLLECTION);
                    DocumentReference documentReference = collectionReference.document(placeID);
                    documentReference.set(placeMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                                                user.setNumberPlaces(user.getNumberPlaces() + 1);
                                                Map<String, Object> userMap = firebaseUserMapper.UserToHashMap(user);
                                                DocumentReference documentReference = collectionReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                documentReference.set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            pbAddEditPlace.setVisibility(View.INVISIBLE);
                                                            Toast.makeText(getApplicationContext(), "Place created", Toast.LENGTH_SHORT).show();
                                                            Log.d(TAG, "Place added with ID: " + placeID);
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
                                Log.e(TAG, "Place upload failed. " + task.getException().getMessage());
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

    private void addPlaceData()
    {
        place.setName(txtName.getText().toString());
        place.setDescription(txtDescription.getText().toString());
        place.setCreatedByEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
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

        if(txtDescription.getText().toString().isEmpty())
        {
            validationHandler.displayValidationError(txtDescription, "Can't be left blank");
            Log.i(TAG, "validate:failure. Place description left blank.");
            valid = false;
        }
        else validationHandler.removeValidationError(txtDescription);

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

    private class GetCity extends AsyncTask<com.google.android.gms.maps.model.LatLng, Void, String> {

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
