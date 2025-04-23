package com.example.fermicalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

public class LoginUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        // Set the rootView for the snack-bars
        rootView = findViewById(R.id.rootView);

        // Obtain the object for interacting with Firebase auth
        mAuth = FirebaseAuth.getInstance();

        // Localize the email language
        mAuth.useAppLanguage();

        // Set the onclick event to the login button
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Closes the keyboard
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

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
            public void onClick(View v) {
                startActivity(new Intent(LoginUser.this, RegisterUser.class));
                finish();
            }
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
                        finish();
                    }
                } else {
                    Snackbar.make(rootView, getString(R.string.authError), Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Check for email verification
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if(currentUser != null) {
                                currentUser.reload().addOnCompleteListener(activity -> {
                                    if (currentUser.isEmailVerified()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        startActivity(new Intent(LoginUser.this, Calendar.class));
                                        finish();
                                    } else {
                                        sendVerificationMail(currentUser);
                                    }
                                });
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginUser.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
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
    }
}