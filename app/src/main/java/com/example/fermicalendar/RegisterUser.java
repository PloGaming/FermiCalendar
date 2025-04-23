package com.example.fermicalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        // Set the rootView for the snack-bars
        rootView = findViewById(R.id.rootView);

        // Obtain the 2 objects for interacting with Firebase auth and database services
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_db_url)).getReference();

        // Localize the email language
        mAuth.useAppLanguage();

        // Set the onclick event to the register button
        Button registerButton = findViewById(R.id.loginButton);
        registerButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Get the user data from the form
                String email, password, name, schoolClass;
                email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();
                password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();
                name = ((EditText)findViewById(R.id.nameEditText)).getText().toString();
                schoolClass = ((EditText)findViewById(R.id.classEditText)).getText().toString();

                // Checking if they're null
                if(email.isEmpty() || password.isEmpty() || name.isEmpty() || schoolClass.isEmpty()) {
                    Snackbar.make(rootView, getString(R.string.formError), Snackbar.LENGTH_LONG).show();
                } else {
                    createUser(email, password, name, schoolClass);
                }
            }
        });

        // Set the onclick event for the link to the login activity
        TextView loginLink = findViewById(R.id.signupLink);
        loginLink.setOnClickListener(new TextView.OnClickListener() {

            @Override
            public void onClick(View v) { startActivity(new Intent(RegisterUser.this, LoginUser.class));}
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and go to calendar activity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            // Reload the user cached info
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (currentUser.isEmailVerified()) {
                        startActivity(new Intent(this, Calendar.class));
                    }
                } else {
                    Snackbar.make(rootView, getString(R.string.authError), Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    // Create a new user with email and password
    private void createUser(String email, String password, String name, String schoolClass) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // The user creation was successful
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            User newUser = new User(name, email, schoolClass);

                            // Add the remaining data to the db
                            mDatabase.child("users").child(user.getUid()).setValue(newUser);

                            sendVerificationMail(user);
                        } else {
                            // If sign up fails, display a message to the user.
                            Snackbar.make(rootView, getString(R.string.authError), Snackbar.LENGTH_LONG).show();
                            // TODO validate the password
                        }
                    }
                });
    }

    // Send the verification email
    private void sendVerificationMail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(activity -> {
                    Snackbar.make(rootView, activity.isSuccessful() ? getString(R.string.emailInfo) + user.getEmail() :
                                    getString(R.string.emailError), Snackbar.LENGTH_LONG).show();
                });
        // TODO add a resend button
    }
}