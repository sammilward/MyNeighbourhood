package com.sammilward.neighbourhood.ui.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.sammilward.neighbourhood.R;

public class LoginActivity extends AppCompatActivity {

    final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private ValidationHandler validationHandler = new ValidationHandler();

    EditText txtSignInEmail, txtSignInPassword;
    Button cmdSignIn;
    TextView lblRegister;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        txtSignInEmail = findViewById(R.id.txtSignInEmail);
        txtSignInPassword = findViewById(R.id.txtSignInPassword);
        cmdSignIn = findViewById(R.id.cmdSignIn);
        lblRegister = findViewById(R.id.txtRegister);

        lblRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        cmdSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignIn();
            }
        });
    }

    public void SignIn()
    {
        String email = txtSignInEmail.getText().toString();
        String password = txtSignInPassword.getText().toString();
        if(Validate(email, password))
        {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Username and password don't match.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        }
    }

    public boolean Validate(String email, String password)
    {
        boolean valid = true;
        if(email.isEmpty())
        {
            validationHandler.displayValidationError(txtSignInEmail, "Can't be left blank");
            Log.i(TAG, "validate:failure. Email left blank.");
            valid = false;
        }
        if(password.isEmpty())
        {
            validationHandler.displayValidationError(txtSignInPassword, "Can't be left blank");
            Log.i(TAG, "validate:failure. Password left blank");
            valid = false;
        }
        if(!validationHandler.isEmailValid(email))
        {
            validationHandler.displayValidationError(txtSignInEmail, "Invalid email address.");
            Log.i(TAG, "validate:failure. Invalid email.");
            valid = false;
        }
        else validationHandler.removeValidationError(txtSignInEmail);

        if(!validationHandler.isPasswordValid(password))
        {
            validationHandler.displayValidationError(txtSignInPassword, "Passwords are 6 characters long and need to include a number.");
            Log.i(TAG, "validate:failure. Invalid password.");
            valid = false;
        }
        else
        {
            validationHandler.removeValidationError(txtSignInPassword);
        }
        return valid;
    }
}
