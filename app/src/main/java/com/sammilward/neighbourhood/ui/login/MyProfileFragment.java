package com.sammilward.neighbourhood.ui.login;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.sammilward.neighbourhood.ui.login.Constants.PROFILEPICTURESDIRECTORY;
import static com.sammilward.neighbourhood.ui.login.Constants.USERSCOLLECTION;

public class MyProfileFragment extends Fragment implements DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference mStorageRef;
    private FirebaseUserMapper firebaseUserMapper;
    private User currentUser, updatedUser;
    private ValidationHandler validationHandler;

    private CardView cvEditSaveProfile;
    private ImageView imgPicture;
    private TextView lblEditSave, lblDOB, lblChangePicture, lblNumberFriends, lblNumberPlaces, lblNumberEvents;
    private Button cmdChooseDOB;
    private EditText txtName, txtInterests, txtBio, txtPostcode;
    private Spinner spnEthnicity;
    private DatePickerDialog datePickerDialog;
    private ProgressBar pbMyProfile;

    private Uri selectedImage;

    private String TAG = "MyProfileFragment";


    public static final int GET_FROM_GALLERY = 3;
    Boolean Edit = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_profile, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseUserMapper = new FirebaseUserMapper();
        validationHandler = new ValidationHandler();
        loadProfile();
    }

    private void initialiseComponents() {
        cvEditSaveProfile = getView().findViewById(R.id.cvUserProfileAddFriend);
        cmdChooseDOB = getView().findViewById(R.id.cmdMyProfileChooseDate);
        txtName = getView().findViewById(R.id.lblUsersProfileName);
        txtPostcode = getView().findViewById(R.id.txtMyProfilePostcode);
        txtInterests = getView().findViewById(R.id.lblUsersProfileInterests);
        txtBio = getView().findViewById(R.id.lblUsersProfileBio);
        lblDOB = getView().findViewById(R.id.lblUsersProfileAge);
        lblEditSave = getView().findViewById(R.id.lblUserProfileFriendAction);
        lblChangePicture = getView().findViewById(R.id.lblMyProfileChangePicture);
        lblNumberFriends = getView().findViewById(R.id.lblUsersProfileNumFriends);
        lblNumberPlaces = getView().findViewById(R.id.lblUsersProfileNumPlacesVisited);
        lblNumberEvents = getView().findViewById(R.id.lblUsersProfileNumEventsVisited);
        pbMyProfile = getView().findViewById(R.id.pbMyProfile);
        imgPicture = getView().findViewById(R.id.imgUsersProfilePicture);
        setupEthnicitySpinner();
        enableElements(false);

        cvEditSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    handleEditSave();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        cmdChooseDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDatePicker();
            }

        });

        lblChangePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });
    }

    private void setupEthnicitySpinner() {
        spnEthnicity = getView().findViewById(R.id.spnMyProfileEthnicity);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                imgPicture.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayDatePicker() {
        if(!(currentUser.getDOB() == null)) {
            Calendar calander = Calendar.getInstance();
            calander.setTime(currentUser.getDOB());
            int day = calander.get(calander.DAY_OF_MONTH);
            int month = calander.get(calander.MONTH);
            int year = calander.get(calander.YEAR);
            datePickerDialog = new DatePickerDialog(getContext(), MyProfileFragment.this, year, month, day);
        }
        else datePickerDialog = new DatePickerDialog(getContext(), MyProfileFragment.this, 2000, 01, 01);
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

    private void loadProfile() {
        CollectionReference collectionReference =  db.collection(USERSCOLLECTION);
        DocumentReference documentReference = collectionReference.document(mAuth.getCurrentUser().getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        currentUser = firebaseUserMapper.DocumentToUser(task.getResult());
                        initialiseComponents();
                        displayUserData();
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

    private void displayUserData()
    {
        StorageReference storageReference = mStorageRef.child(currentUser.getImageURL());
        GlideApp.with(this).load(storageReference).signature(new ObjectKey(currentUser.getImageUpdatedEpochTime())).into(imgPicture);
        txtName.setText(currentUser.getDisplayName());
        txtBio.setText(currentUser.getBio());
        txtPostcode.setText(currentUser.getPostcode());
        //txtInterests.setText(currentUser.getInterests().toString());
        if(currentUser.getDOB() != null) lblDOB.setText(formatDate(currentUser.getDOB()));
        lblNumberFriends.setText(String.valueOf(currentUser.getFriendsList().size()));
        lblNumberPlaces.setText(String.valueOf(currentUser.getNumberPlaces()));
        lblNumberEvents.setText(String.valueOf(currentUser.getNumberEvents()));
        pbMyProfile.setVisibility(View.INVISIBLE);

        List<String> ethnicities = Arrays.asList(getResources().getStringArray(R.array.ethnicities));
        int position = ethnicities.indexOf(currentUser.getEthnicity());
        spnEthnicity.setSelection(position);

        txtInterests.setText(currentUser.getInterestListAsText());
    }

    private void handleEditSave() throws ParseException {
        if(!Edit)
        {
            Edit = true;
            enableElements(Edit);
        }
        else
        {
            saveProfile();
        }
    }

    private void enableElements(boolean enable) {
        cmdChooseDOB.setEnabled(enable);
        txtName.setEnabled(enable);
        txtInterests.setEnabled(enable);
        txtBio.setEnabled(enable);
        txtPostcode.setEnabled(enable);
        spnEthnicity.setEnabled(enable);

        if(enable == true)
        {
            lblEditSave.setText("Save Profile");
            lblChangePicture.setVisibility(View.VISIBLE);
            cmdChooseDOB.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        else
        {
            lblEditSave.setText("Edit Profile");
            lblChangePicture.setVisibility(View.INVISIBLE);
            cmdChooseDOB.setBackgroundColor(Color.GRAY);
        }
    }

    private void saveProfile() throws ParseException {
        if(formValid())
        {
            Edit = false;
            enableElements(Edit);
            updatedUser = createUpdatedUser();
            if(!currentUser.equals(updatedUser) || selectedImage != null)
            {
                uploadUserToFirebase();
            }
        }
    }

    private boolean formValid() {
        boolean valid = true;
        if(spnEthnicity.getSelectedItemPosition() == 0)
        {
            spnEthnicity.setBackgroundColor(Color.RED);
            valid = false;
        }
        else spnEthnicity.setBackgroundColor(Color.TRANSPARENT);

        if(txtName.getText().toString().isEmpty())
        {
            validationHandler.displayValidationError(txtName, "Can't be left blank");
            valid = false;
        }
        if(!validationHandler.isNameValid(txtName.getText().toString()))
        {
            validationHandler.displayValidationError(txtName, "Must only contain letters");
            valid = false;
        }
        else validationHandler.removeValidationError(txtName);

        if(lblDOB.getText().toString().isEmpty())
        {
            cmdChooseDOB.setBackgroundColor(Color.RED);
            valid = false;
        }
        else cmdChooseDOB.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        if(txtPostcode.getText().toString().isEmpty())
        {
            validationHandler.displayValidationError(txtPostcode, "Can't be left blank");
            valid = false;
        } else validationHandler.removeValidationError(txtPostcode);

        if(!validationHandler.isPostcodeValid(txtPostcode.getText().toString())) {
            validationHandler.displayValidationError(txtPostcode, "Must be UK postcode in uppercase");
            valid = false;
        } else validationHandler.removeValidationError(txtPostcode);

        return valid;
    }

    private User createUpdatedUser() throws ParseException {
        pbMyProfile.setVisibility(View.VISIBLE);
        LocationHandler locationHandler = new LocationHandler(this.getContext());
        updatedUser = new User();
        updatedUser.setPostcode(txtPostcode.getText().toString());
        updatedUser.setEmail(currentUser.getEmail());
        updatedUser.setImageURL(currentUser.getImageURL());
        updatedUser.setDisplayName(txtName.getText().toString());
        updatedUser.setDOB(new SimpleDateFormat("dd-MM-yyyy").parse(lblDOB.getText().toString()));
        updatedUser.setEthnicity(spnEthnicity.getSelectedItem().toString());
        updatedUser.setBio(txtBio.getText().toString());
        updatedUser.setLocation(locationHandler.getLatLngFromLocationName(txtPostcode.getText().toString()));
        updatedUser.setNumberEvents(currentUser.getNumberEvents());
        updatedUser.setNumberPlaces(currentUser.getNumberPlaces());
        updatedUser.setNumberFriends(currentUser.getNumberFriends());
        updatedUser.setFriendsList(currentUser.getFriendsList());

        String interestsText = txtInterests.getText().toString().replaceAll(",", "");
        String[] interests = interestsText.split(" ");
        List<String> interestList = new ArrayList<>();
        for (String interest : interests) {
            interestList.add(interest);
        }

        updatedUser.setInterests(interestList);

        return updatedUser;
    }

    private void uploadUserToFirebase() {
        pbMyProfile.setVisibility(View.VISIBLE);
        if(!(selectedImage == null))
        {
            final String storageLocation = PROFILEPICTURESDIRECTORY+mAuth.getCurrentUser().getUid();
            final StorageReference photoRef = mStorageRef.child(storageLocation);

            photoRef.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        updatedUser.setImageURL(storageLocation);
                        updatedUser.setImageUpdatedEpochTime(Instant.now().getEpochSecond());
                        UpdateUserDocument();
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
            UpdateUserDocument();
        }
    }

    private void UpdateUserDocument() {

        if (!currentUser.getPostcode().equals(updatedUser.getPostcode()))
        {
            String city = "";
            try {
                city = new GetCity().execute(updatedUser.getPostcode()).get();
            } catch (Exception e) {
                e.printStackTrace();
                pbMyProfile.setVisibility(View.INVISIBLE);
            }
            updatedUser.setCity(city);
        }
        else updatedUser.setCity(currentUser.getCity());

        Map<String, Object> userMap = firebaseUserMapper.UserToHashMap(updatedUser);
        CollectionReference collectionReference =  db.collection(USERSCOLLECTION);
        DocumentReference documentReference = collectionReference.document(mAuth.getCurrentUser().getUid());
        documentReference.set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    pbMyProfile.setVisibility(View.INVISIBLE);
                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "UserDocument updated with ID: " + mAuth.getCurrentUser().getUid());
                }
                else
                {
                    Toast.makeText(getContext(), "Profile update failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "UserDocument update failed. " + task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        lblDOB.setText(getDateFromDatePicker(datePicker));
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        spnEthnicity.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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
