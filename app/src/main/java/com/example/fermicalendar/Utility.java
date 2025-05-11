package com.example.fermicalendar;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Locale;

public class Utility {

    // Closes the keyboard
    public static void closeKeyboard(AppCompatActivity actvity) {
        View view = actvity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) actvity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Changes the activity
    public static <T> void changeActivity(AppCompatActivity startingActivity, Class<T> targetActivity) {
        startingActivity.startActivity(new Intent(startingActivity, targetActivity));
        startingActivity.finish(); // Makes impossible to go back for the user
    }

    // Send the verification email
    public static void sendVerificationMail(Context context, FirebaseUser user, View rootView) {
        user.sendEmailVerification()
                .addOnCompleteListener(activity -> Snackbar.make(rootView, activity.isSuccessful() ? context.getString(R.string.emailInfo) + user.getEmail() :
                        context.getString(R.string.emailError), Snackbar.LENGTH_LONG).show());
    }
}
