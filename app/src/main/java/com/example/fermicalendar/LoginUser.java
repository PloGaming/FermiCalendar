package com.example.fermicalendar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        // Set the rootView for the snack-bars
        rootView = findViewById(R.id.rootHome);

        // Obtain the object for interacting with Firebase auth
        mAuth = FirebaseAuth.getInstance();

        // Localize the email language
        mAuth.useAppLanguage();

        // Set the onclick event to the login button
        Button loginButton = findViewById(R.id.registerButton);
        loginButton.setOnClickListener(v -> {
            Utility.closeKeyboard(this);

            // Get the user data from the form
            String email, password;
            email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();
            password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();

            // Checking if they're null
            if(email.isEmpty() || password.isEmpty()) {
                Snackbar.make(rootView, getString(R.string.formError), Snackbar.LENGTH_LONG).show();
            } else {
                signInUser(email, password);
            }
        });

        // Set the onclick event for the link to the login activity
        TextView signupLink = findViewById(R.id.signinLink);
        signupLink.setOnClickListener(v -> Utility.changeActivity(this, RegisterUser.class));
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and go to calendar activity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            // Reload the user cached info
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (currentUser.isEmailVerified()) {
                        Utility.changeActivity(this, CalendarActivity.class);
                    }
                } else {
                    Snackbar.make(rootView, getString(R.string.authError), Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Check for email verification
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user != null) {
                            user.reload().addOnCompleteListener(activity -> {
                                if (user.isEmailVerified()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Utility.changeActivity(this, CalendarActivity.class);
                                } else {
                                    // Resend verification email
                                    Utility.sendVerificationMail(this, user, rootView);
                                }
                            });
                        }
                    } else {
                        // If sign in fails, displ  ay a message to the user.
                        Snackbar.make(rootView, getString(R.string.authError), Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}