package com.sammilward.neighbourhood.ui.login;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
import java.util.Map;

import static com.sammilward.neighbourhood.ui.login.Constants.PROFILEPICTURESDIRECTORY;
import static com.sammilward.neighbourhood.ui.login.Constants.USERSCOLLECTION;

public class RegisterActivity extends AppCompatActivity {

    final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;
    private FirebaseUserMapper firebaseUserMapper;
    private FirebaseFirestore db;

    private StorageReference mStorageRef;
    private ValidationHandler validationHandler = new ValidationHandler();
    private Uri selectedImage = null;
    public static final int GET_FROM_GALLERY = 3;

    EditText txtRegisterEmail, txtRegisterPassword, txtReEnterPassword, txtName, txtPostcode;
    Button cmdRegister, cmdChangeProfilePicture;
    ImageView imgRegisterProfilePicture;
    ProgressBar pbRegister;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        firebaseUserMapper = new FirebaseUserMapper();

        txtRegisterEmail = findViewById(R.id.txtRegisterEmail);
        txtRegisterPassword = findViewById(R.id.txtRegisterPassword);
        txtReEnterPassword = findViewById(R.id.txtRegisterReEnterPassword);
        txtName = findViewById(R.id.txtRegisterName);
        txtPostcode = findViewById(R.id.txtRegisterPostcode);
        cmdRegister = findViewById(R.id.cmdRegister);
        cmdChangeProfilePicture =findViewById(R.id.cmdRegisterChangeProfilePicture);
        imgRegisterProfilePicture = findViewById(R.id.imgRegisterProfilePicture);
        pbRegister = findViewById(R.id.pbRegister);

        cmdRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Register();
            }
        });

        cmdChangeProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                imgRegisterProfilePicture.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Register()
    {
        String displayName = txtName.getText().toString();
        String email = txtRegisterEmail.getText().toString();
        String password = txtRegisterPassword.getText().toString();
        String reEnteredPassword = txtReEnterPassword.getText().toString();
        String postcode = txtPostcode.getText().toString();
        if(Validate(displayName, email, password, reEnteredPassword, postcode))
        {
            pbRegister.setVisibility(View.VISIBLE);

            String city = "";
            try {
                city = new GetCity().execute(postcode).get();
            } catch (Exception e) {
                e.printStackTrace();
                pbRegister.setVisibility(View.INVISIBLE);
            }

            final String confirmedCity = city;

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task)
                    {
                    if (task.isSuccessful())
                    {
                        Log.d(TAG, "createUserWithEmail:success");
                        final String storageLocation = PROFILEPICTURESDIRECTORY+mAuth.getCurrentUser().getUid();
                        final StorageReference photoRef = mStorageRef.child(storageLocation);

                        photoRef.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful())
                                {

                                    User user = getActivityUser();
                                    try {
                                        user.setDOB(new SimpleDateFormat("dd-MM-yyyy").parse("01-01-2000"));
                                        user.setCity(confirmedCity);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    user.setImageURL(storageLocation);
                                    user.setImageUpdatedEpochTime(Instant.now().getEpochSecond());
                                    Map<String, Object> userMap = firebaseUserMapper.UserToHashMap(user);

                                    CollectionReference collectionReference =  db.collection(USERSCOLLECTION);
                                    DocumentReference documentReference = collectionReference.document(mAuth.getCurrentUser().getUid());
                                    documentReference.set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                pbRegister.setVisibility(View.INVISIBLE);
                                                Toast.makeText(RegisterActivity.this, "User created", Toast.LENGTH_SHORT).show();
                                                Log.d(TAG, "UserDocument added with ID: " + mAuth.getCurrentUser().getUid());
                                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                            }
                                            else
                                            {
                                                Log.e(TAG, "UserDocument upload failed. " + task.getException().getMessage());
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
                    } else
                    {
                        Log.e(TAG, "createUserWithEmail:failure. " + task.getException().getMessage());
                        Toast.makeText(RegisterActivity.this, "Register failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        pbRegister.setVisibility(View.INVISIBLE);
                    }
                    }
                });
        }
    }

    public boolean Validate(String displayName, String email, String password, String reEnteredPassword, String postcode)
    {
        boolean valid = true;

        if(selectedImage == null)
        {
            cmdChangeProfilePicture.setTextColor(getResources().getColor(R.color.colorRed));
            valid = false;
        }
        else cmdChangeProfilePicture.setTextColor(getResources().getColor(R.color.colorAccent));

        if(displayName.isEmpty())
        {
            validationHandler.displayValidationError(txtName, "Can't be left blank");
            Log.i(TAG, "validate:failure. Display name left blank.");
            valid = false;
        }
        else validationHandler.removeValidationError(txtName);

        if(!validationHandler.isNameValid(displayName))
        {
            validationHandler.displayValidationError(txtName, "Must only contain letters");
            Log.i(TAG, "validate:failure. Display name invalid.");
            valid = false;
        }
        else validationHandler.removeValidationError(txtName);

        if(email.isEmpty())
        {
            validationHandler.displayValidationError(txtRegisterEmail, "Can't be left blank");
            Log.i(TAG, "validate:failure. Email left blank.");
            valid = false;
        }
        if(password.isEmpty())
        {
            validationHandler.displayValidationError(txtRegisterPassword, "Can't be left blank");
            Log.i(TAG, "validate:failure. Password left blank");
            valid = false;
        }
        if(!validationHandler.isEmailValid(email))
        {
            validationHandler.displayValidationError(txtRegisterEmail, "Invalid email address.");
            Log.i(TAG, "validate:failure. Invalid email.");
            valid = false;
        }
        else validationHandler.removeValidationError(txtRegisterEmail);

        if(!validationHandler.doPasswordsMatch(password, reEnteredPassword))
        {
            validationHandler.displayValidationError(txtRegisterPassword, "Passwords do not match");
            validationHandler.displayValidationError(txtReEnterPassword, "Passwords do not match");
            Log.i(TAG, "validate:failure. Passwords do not match");
            return false;
        }
        else {
            validationHandler.removeValidationError(txtRegisterPassword);
            validationHandler.removeValidationError(txtReEnterPassword);
        }

        if(!validationHandler.isPasswordValid(password))
        {
            validationHandler.displayValidationError(txtRegisterPassword,"Passwords need to be 6 characters long and have to include a number.");
            Log.i(TAG, "validate:failure. Password not valid.");
            return false;
        }
        else validationHandler.removeValidationError(txtRegisterPassword);

        if(postcode.isEmpty())
        {
            validationHandler.displayValidationError(txtPostcode, "Can't be left blank");
            valid = false;
        } else validationHandler.removeValidationError(txtPostcode);

        if(!validationHandler.isPostcodeValid(postcode)) {
            validationHandler.displayValidationError(txtPostcode, "Must be UK postcode in uppercase");
            valid = false;
        } else validationHandler.removeValidationError(txtPostcode);

        return valid;
    }

    private User getActivityUser() {
        LocationHandler locationHandler = new LocationHandler(getApplicationContext());
        User user = new User();
        user.setDisplayName(txtName.getText().toString());
        user.setEmail(txtRegisterEmail.getText().toString());
        user.setPostcode(txtPostcode.getText().toString());
        user.setLocation(locationHandler.getLatLngFromLocationName(txtPostcode.getText().toString()));
        user.setBio("");
        user.setEthnicity("British");
        return user;
    }

    private class GetCity extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String city = "";
            try {
                    String postcode = strings[0];
                    Geocoder geocoder = new Geocoder();
                    city = geocoder.GetCityFromPostcode(postcode);
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