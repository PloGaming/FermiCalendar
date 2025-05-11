package com.example.fermicalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Calendar extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Gson gson;
    private OkHttpClient client;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Initialize the event list
        recyclerView = findViewById(R.id.eventList);
        recyclerView.setLayoutManager(new LinearLayoutManager(Calendar.this));

        // Initialize the library for interpreting JSON
        gson = new Gson();

        // Initialize the OkHttp Library
        client = new OkHttpClient();

        // Set the rootView
        rootView = findViewById(R.id.rootView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // At the start show the events of today
        showEvents(ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(3).withHour(0).withMinute(0));
    }

    protected void showEvents(ZonedDateTime startDate, ZonedDateTime endDate) {
        // Build the url
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("www.googleapis.com")
                .addPathSegment("calendar")
                .addPathSegment("v3")
                .addPathSegment("calendars")
                .addPathSegment(getString(R.string.fermiCalendarID))
                .addPathSegment("events")
                // Dates should be properly formatted to the standard ISO RFC3339 format
                .addQueryParameter("timeMin", startDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("timeMax", endDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("key", getString(R.string.googleCalendarApiKey))
                .addQueryParameter("orderBy", "startTime")
                .addQueryParameter("singleEvents", "true")
                .build();

        // Build the request
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();

        // Make the call
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // If the response code is between 200 and 300
                if (response.isSuccessful()) {
                    // Get the list of events
                    List<Event> events  = gson.fromJson(response.body().string(), CalendarResponse.class).items;

                    // Because onResponse runs on a different thread than the UI one
                    // and the recyclerView can only be modified on the main thread
                    runOnUiThread(() -> {
                        recyclerView.setAdapter(new EventAdapter(events));
                    });
                } else {
                    Snackbar.make(rootView, getString(R.string.calendarError), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Snackbar.make(rootView, getString(R.string.calendarError), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}