package com.example.fermicalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Calendar extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mAuth = FirebaseAuth.getInstance();

        getEvents(ZonedDateTime.of(2025, 5, 10, 0, 0, 0, 0, ZoneId.systemDefault()),
                ZonedDateTime.of(2025, 5, 11, 0, 0, 0, 0, ZoneId.systemDefault()));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void getEvents(ZonedDateTime startDate, ZonedDateTime endDate) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("www.googleapis.com")
                .addPathSegment("calendar")
                .addPathSegment("v3")
                .addPathSegment("calendars")
                .addPathSegment(getString(R.string.fermiCalendarID))
                .addPathSegment("events")
                .addQueryParameter("timeMin", startDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("timeMax", endDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("key", getString(R.string.googleCalendarApiKey))
                .addQueryParameter("orderBy", "startTime")
                .addQueryParameter("singleEvents", "true")
                .build();

        Log.d("HTTP_URL", url.toString());
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("HTTP_RESPONSE", response.body().string());
                } else {
                    Log.e("HTTP_ERROR", "Code: " + response.code() + ", Message: " + response.message());
                }
            }
        });
    }
}