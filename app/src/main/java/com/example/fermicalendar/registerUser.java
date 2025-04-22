package com.example.fermicalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class registerUser extends AppCompatActivity {

    private final static String FIREBASE_DB_URL = "https://fermicalendar-5253f-default-rtdb.europe-west1.firebasedatabase.app/";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registry_activity);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(FIREBASE_DB_URL).getReference();

        // Set the onclick event to the register button
        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Get the user data from the form
                String email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();
                String password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();
                String name = ((EditText)findViewById(R.id.nameEditText)).getText().toString();
                String schoolClass = ((EditText)findViewById(R.id.classEditText)).getText().toString();

                // TODO validate password

                createUser(email, password, name, schoolClass);

                // TODO if successful go to new Activity
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // TODO transfer to new activity
        }
    }

    // Create a new user with email and password
    private void createUser(String email, String password, String name, String schoolClass) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();
                            User newUser = new User(name, email, schoolClass);

                            mDatabase.child("users").child(user.getUid()).setValue(newUser);
                        } else {
                            // If sign up fails, display a message to the user.
                            Log.w("MainActivity", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(registerUser.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                            // TODO handle api errors
                        }
                    }
                });
    }
}