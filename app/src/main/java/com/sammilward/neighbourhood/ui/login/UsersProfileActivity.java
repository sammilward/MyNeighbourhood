package com.sammilward.neighbourhood.ui.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sammilward.neighbourhood.R;

import java.util.Map;

import static com.sammilward.neighbourhood.ui.login.Constants.USERSCOLLECTION;

public class UsersProfileActivity extends AppCompatActivity {

    private final String TAG = "UsersProfileActivity";

    private User displayedUser;
    private User currentUser;

    private FirebaseFirestore db;
    private FirebaseUserMapper mapper;

    ImageView imgProfilePicture, imgFriendsHeart;
    CardView cvAddFriend;
    TextView lblName, lblEthnicity, lblAge, lblNumFriends, lblNumPlaces, lblNumEvents, lblInterests, lblBio, lblUserProfileFriendAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);

        db = FirebaseFirestore.getInstance();
        mapper = new FirebaseUserMapper();

        Intent intent = getIntent();
        displayedUser = intent.getParcelableExtra("SELECTEDUSER");
        currentUser = intent.getParcelableExtra("CURRENTUSER");
        displayUserData();
    }

    private void displayUserData()
    {
        imgProfilePicture = findViewById(R.id.imgUsersProfilePicture);
        imgFriendsHeart = findViewById(R.id.imgFriendsHeart);
        cvAddFriend = findViewById(R.id.cvUserProfileAddFriend);

        lblName = findViewById(R.id.lblUsersProfileName);
        lblEthnicity = findViewById(R.id.lblUsersProfileEthnicity);
        lblAge = findViewById(R.id.lblUsersProfileAge);
        lblNumFriends = findViewById(R.id.lblUsersProfileNumFriends);
        lblNumPlaces = findViewById(R.id.lblUsersProfileNumPlacesVisited);
        lblNumEvents = findViewById(R.id.lblUsersProfileNumEventsVisited);
        lblInterests = findViewById(R.id.lblUsersProfileInterests);
        lblBio = findViewById(R.id.lblUsersProfileBio);
        lblUserProfileFriendAction = findViewById(R.id.lblUserProfileFriendAction);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(displayedUser.getImageURL());
        GlideApp.with(this).load(storageReference).signature(new ObjectKey(displayedUser.getImageUpdatedEpochTime())).into(imgProfilePicture);

        lblName.setText(displayedUser.getDisplayName());
        lblEthnicity.setText(displayedUser.getEthnicity());
        lblAge.setText(Integer.toString(displayedUser.getAge()));
        lblNumFriends.setText(Integer.toString(displayedUser.getFriendsList().size()));
        lblNumPlaces.setText(Integer.toString(displayedUser.getNumberPlaces()));
        lblNumEvents.setText(Integer.toString(displayedUser.getNumberEvents()));
        lblBio.setText(displayedUser.getBio());
        lblInterests.setText(displayedUser.getInterestListAsText());
        displayFriendAction();

        cvAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                friendAction();
            }
        });

    }

    private boolean areFriends(User displayedUser, User currentUser)
    {
        return displayedUser.getFriendsList().contains(currentUser.getEmail());
    }

    private void displayFriendAction()
    {
        if (areFriends(displayedUser, currentUser))
        {
            lblUserProfileFriendAction.setText("Remove Friend");
            cvAddFriend.setCardBackgroundColor(getResources().getColor(R.color.colorRed));
            imgFriendsHeart.setVisibility(View.VISIBLE);
        }
        else
        {
            lblUserProfileFriendAction.setText("Add Friend");
            cvAddFriend.setCardBackgroundColor(getResources().getColor(R.color.colorGreen));
            imgFriendsHeart.setVisibility(View.GONE);
        }

        lblNumFriends.setText(Integer.toString(displayedUser.getFriendsList().size()));
    }

    private void friendAction() {
        if (areFriends(displayedUser, currentUser))
        {
            displayedUser.getFriendsList().remove(currentUser.getEmail());
            currentUser.getFriendsList().remove(displayedUser.getEmail());
        }
        else
        {
            displayedUser.getFriendsList().add(currentUser.getEmail());
            currentUser.getFriendsList().add(displayedUser.getEmail());
        }

        final Map<String, Object> displayUserMap = mapper.UserToHashMap(displayedUser);
        CollectionReference collectionReference =  db.collection(USERSCOLLECTION);
        final DocumentReference documentReference = collectionReference.document(displayedUser.getId());
        documentReference.set(displayUserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Map<String, Object> currentUserMap = mapper.UserToHashMap(currentUser);
                    CollectionReference collectionReference =  db.collection(USERSCOLLECTION);
                    final DocumentReference documentReference = collectionReference.document(currentUser.getId());
                    documentReference.set(currentUserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Log.d(TAG, "User updated with ID: " + documentReference.getId());
                                displayFriendAction();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "Failed to remove friend\n" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "User update failed. " + task.getException().getMessage());
                            }
                        }
                    });
                }
            }
        });
    }
}
