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

    private FirebaseAuth mAuth;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mAuth = FirebaseAuth.getInstance();
        gson = new Gson();

        getEvents(ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(2).withHour(0).withMinute(0).withSecond(0));
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
                    List<Event> events = gson.fromJson(response.body().string(), CalendarResponse.class).items;
                    for(int i = 0; i < events.size(); i++) {
                        Log.d("Event", events.get(i).summary);
                    }

                } else {
                    Log.e("HTTP_ERROR", "Code: " + response.code() + ", Message: " + response.message());
                }
            }
        });
    }
}