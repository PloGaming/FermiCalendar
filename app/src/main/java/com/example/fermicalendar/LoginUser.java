package com.example.fermicalendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        // Obtain the 2 objects for interacting with Firebase auth and database services
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_db_url)).getReference();

        // Set the onclick event to the login button
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Get the user data from the form
                String email, password, name, schoolClass;
                email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();
                password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();

                // Checking if they're null
                if(email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginUser.this, getString(R.string.formError),
                            Toast.LENGTH_LONG).show();
                } else {
                    signInUser(email, password);
                }
            }
        });

        // Set the onclick event for the link to the login activity
        TextView signupLink = findViewById(R.id.signupLink);
        signupLink.setOnClickListener(new TextView.OnClickListener() {

            @Override
            public void onClick(View v) { startActivity(new Intent(LoginUser.this, RegisterUser.class));}
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(this, Calendar.class));
        }
    }

    private void signInUser(String email, String password) {

    }

}