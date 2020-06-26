package com.sammilward.neighbourhood.ui.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sammilward.neighbourhood.R;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

import static com.sammilward.neighbourhood.ui.login.Constants.DIARYCOLLECTION;
import static com.sammilward.neighbourhood.ui.login.Constants.FRIENDSLIST;
import static com.sammilward.neighbourhood.ui.login.Constants.PLACESCOLLECTION;
import static com.sammilward.neighbourhood.ui.login.Constants.USERSCOLLECTION;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final String MAPFRAGMENTTAG = "MapFragment";
    private static final String MYPROFILEFRAGMENTTAG = "MyProfileFragment";
    private static final String FRIENDSFRAGMENTTAG = "FriendsFragment";
    private static final String PEOPLEFRAGMENTTAG = "PeopleFragment";
    private static final String EVENTSFRAGMENTTAG = "EventsFragment";
    private static final String DIARYFRAGMENTTAG = "DiaryFragment";
    private static final String PLACESFRAGMENTTAG = "PlacesFragment";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView txtHeaderEmail, txtHeaderName;
    private ImageView imgHeaderPhoto;
    private NavigationView navigationView;

    private int CurrentNavOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                loadUserHeader();
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        initialise();
    }

    private void initialise()
    {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dynamicallyAddMapFragment();
        loadUserHeader();
        selectNavigationMenuItem(R.id.nav_map);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(CurrentNavOption != R.id.nav_map)
            {
                addFragment(new MapsFragment(), false, MAPFRAGMENTTAG);
                selectNavigationMenuItem(R.id.nav_map);
            }
            else
            {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id != CurrentNavOption)
        {
            if (id == R.id.nav_map) {
                addFragment(new MapsFragment(), false, MAPFRAGMENTTAG);
                selectNavigationMenuItem(R.id.nav_map);
            } else if (id == R.id.nav_friends) {
                addFragment(new FriendsFragment(), false, FRIENDSFRAGMENTTAG  );
                selectNavigationMenuItem(R.id.nav_friends);
            } else if (id == R.id.nav_people) {
                addFragment(new PeopleFragment(), false, PEOPLEFRAGMENTTAG );
                selectNavigationMenuItem(R.id.nav_people);
            } else if (id == R.id.nav_places) {
                addFragment(new PlacesFragment(), false, PLACESFRAGMENTTAG);
                selectNavigationMenuItem(R.id.nav_places);
            } else if (id == R.id.nav_events) {
                addFragment(new EventsFragment(), false, EVENTSFRAGMENTTAG );
                selectNavigationMenuItem(R.id.nav_events);
            } else if (id == R.id.nav_diary) {
                addFragment(new DiaryFragment(), false, DIARYFRAGMENTTAG);
                selectNavigationMenuItem(R.id.nav_diary);
            } else if (id == R.id.nav_my_profile){
                addFragment(new MyProfileFragment(), false, MYPROFILEFRAGMENTTAG);
                selectNavigationMenuItem(R.id.nav_my_profile);
            } else if (id == R.id.nav_sign_out)
            {
                signOut();
            }
            else if (id == R.id.nav_remove_account){
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Permanently remove your account?")
                    .setMessage("Are you sure you want to permanently delete your account? This is not recoverable.")
                    .setPositiveButton("Yes remove account", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteAccount();
                        }
                    })
                    .setNegativeButton("Do not delete", null)
                    .show();
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void dynamicallyAddMapFragment() {
        Fragment fg = new MapsFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.contentMainConstraint, fg).commit();
    }

    public void addFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.replace(R.id.contentMainConstraint, fragment, tag);
        ft.commitAllowingStateLoss();
    }

    public void loadUserHeader()
    {
        CollectionReference collectionReference =  db.collection(USERSCOLLECTION);
        DocumentReference documentReference = collectionReference.document(mAuth.getCurrentUser().getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        User user = new FirebaseUserMapper().DocumentToUser(task.getResult());
                        loadNavigationHeader(user);
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

    private void loadNavigationHeader(User user)
    {
        View header = navigationView.getHeaderView(0);
        txtHeaderEmail = header.findViewById(R.id.txtHeaderEmail);
        txtHeaderName = header.findViewById(R.id.txtHeaderName);
        imgHeaderPhoto = header.findViewById(R.id.imgUsersProfilePicture);

        txtHeaderEmail.setText(user.getEmail());
        txtHeaderName.setText(user.getDisplayName());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(user.getImageURL());
        GlideApp.with(this).load(storageReference).signature(new ObjectKey(user.getImageUpdatedEpochTime())).into(imgHeaderPhoto);
    }

    private void selectNavigationMenuItem(int id)
    {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
        CurrentNavOption = id;
    }

    private void signOut() {
        mAuth.signOut();
        returnToLoginActivity();
    }

    private void deleteAccount() {
        reauthenticateAccount(getString(R.string.dialog_confirm_account_delete_title), getString(R.string.dialog_confirm_account_delete_description));
    }

    private void reauthenticateAccount(String title, String description)
    {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_SWIPE_TO_DISMISS);
        dialog.setContentView(R.layout.dialog_reauthenticate_account);
        dialog.show();

        TextView lblReauthenticateUserTitle = dialog.findViewById(R.id.lblreauthenticateusertitle);
        TextView lblReauthenticateUserDesciption = dialog.findViewById(R.id.lblreauthenticateuserdescription);
        lblReauthenticateUserTitle.setText(title);
        lblReauthenticateUserDesciption.setText(description);

        final EditText passwordTextBox = dialog.findViewById(R.id.txtreauthenticatepassword);
        Button cmdConfirm = dialog.findViewById(R.id.cmdReauthenticateConfirm);
        cmdConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            String password = passwordTextBox.getText().toString();
            AuthCredential credential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(),password);
                mAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Log.d(TAG, "reauthenticateAccount:success.");
                        firebaseDeleteAccount();
                    }
                    else
                    {
                        Log.d(TAG, "reauthenticateAccount:failure. " + task.getException().getMessage());
                        Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            }
        });
    }

    private void firebaseDeleteAccount() {
        final CollectionReference collectionReference =  db.collection(USERSCOLLECTION);
        String id = mAuth.getCurrentUser().getUid();
        final DocumentReference documentReference = collectionReference.document(id);
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Log.d(TAG, "deleteAccount:success.");
                                returnToLoginActivity();
                            }
                            else
                            {
                                Log.d(TAG, "deleteAccount:failure. " + task.getException().getMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    private void returnToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}