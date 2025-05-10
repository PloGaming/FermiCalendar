package com.example.fermicalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        // Set the rootView for the snack-bars
        rootView = findViewById(R.id.rootView);

        // Obtain the 2 objects for interacting with Firebase auth and database services
        mAuth = FirebaseAuth.getInstance();

        // Localize the email language
        mAuth.useAppLanguage();

        // Set the onclick event to the register button
        Button registerButton = findViewById(R.id.loginButton);
        registerButton.setOnClickListener(v -> {

            Utility.closeKeyboard(this);

            // Get the user data from the form
            String email, password, name, schoolClass;
            email = ((EditText)findViewById(R.id.emailEditText)).getText().toString();
            password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();
            name = ((EditText)findViewById(R.id.nameEditText)).getText().toString();
            schoolClass = ((EditText)findViewById(R.id.classEditText)).getText().toString();

            // TODO check the domain email

            // Checking if they're null
            if(email.isEmpty() || password.isEmpty() || name.isEmpty() || schoolClass.isEmpty()) {
                Snackbar.make(rootView, getString(R.string.formError), Snackbar.LENGTH_LONG).show();
            } else {
                createUser(email, password, name, schoolClass);
            }
        });

        // Set the onclick event for the link to the login activity
        TextView loginLink = findViewById(R.id.signinLink);
        loginLink.setOnClickListener(v -> Utility.changeActivity(this, LoginUser.class));

    }

    // Create a new user with email and password
    private void createUser(String email, String password, String name, String schoolClass) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    // The user creation was successful
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        User userInfo = new User(name, schoolClass);

                        addUserInfoDB(user, userInfo);

                        Utility.sendVerificationMail(this, user, rootView);
                    } else {
                        // If sign up fails, display a message to the user.
                        Snackbar.make(rootView, getString(R.string.authError), Snackbar.LENGTH_LONG).show();
                        // TODO validate the password
                    }
                });
    }

    private void addUserInfoDB(FirebaseUser user, User userInfo) {
        // Get the user token
        user.getIdToken(true).addOnCompleteListener(task -> {
            if(task.isSuccessful())  {
                OkHttpClient client = new OkHttpClient();
                Request req = new Request.Builder()
                        .url(getString(R.string.serverURL) + "/users")
                        .addHeader("Authorization", "Bearer " + task.getResult().getToken())
                        .post(RequestBody.create(new Gson().toJson(userInfo), MediaType.parse("application/json; charset=utf-8")))
                        .build();

                client.newCall(req).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Snackbar.make(rootView, getString(R.string.authError), Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {}
                });
            } else {
                Snackbar.make(rootView, getString(R.string.authError), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}