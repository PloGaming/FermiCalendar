package com.example.fermicalendar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private Gson gson;
    private OkHttpClient client;
    private View rootView;
    private ZonedDateTime selectedDay;
    private TextInputEditText dateEditText;

    public HomeFragment() { super(R.layout.fragment_home); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)  {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the event list
        recyclerView = view.findViewById(R.id.eventList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        // Initialize the library for interpreting JSON
        gson = new Gson();

        // Initialize the OkHttp Library
        client = new OkHttpClient();

        // Set the rootView
        rootView = view.findViewById(R.id.rootHome);

        // Create the DatePicker
        dateEditText  = view.findViewById(R.id.dateEditText);
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build();

        // Set the onclick method for the datepicker
        dateEditText.setOnClickListener(v -> datePicker.show(requireActivity().getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));

        // Set the callback for when the user presses ok in the calendar dialog
        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Checks if selection is not null
            if(datePicker.getSelection() != null) {
                Instant instant = Instant.ofEpochMilli(datePicker.getSelection());

                // Convert to ZonedDateTime using system's default time zone
                ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
                changeDay(zonedDateTime);
            }
        });

        // Set the onclick method for the 2 arrows (back and forward)
        Button backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            changeDay(selectedDay.minusDays(1));
        });

        Button forwardButton = view.findViewById(R.id.forwardButton);
        forwardButton.setOnClickListener(v -> {
            changeDay(selectedDay.plusDays(1));
        });

        // Set up the swipe-to-refresh listener
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.md_theme_background);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.md_theme_primaryContainer,
                R.color.md_theme_primaryContainer,
                R.color.md_theme_primaryContainer
        );
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // This method is called when the user pulls down to refresh
            changeDay(selectedDay);

            // After fetching is complete, turn off the loading indicator
            swipeRefreshLayout.setRefreshing(false);
        });

        // Show today's events
        changeDay(ZonedDateTime.now());
    }

    protected void changeDay(ZonedDateTime newDay) {
        if(newDay == null)
            return;

        // Sets it as the current selected day
        selectedDay = newDay.withHour(0).withMinute(0).withSecond(0);

        // formats selected date
        dateEditText.setText(selectedDay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Finally shows it
        showEvents(selectedDay, selectedDay.plusDays(1).withHour(0).withMinute(0));
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
                if (!isAdded()) return; // Check if fragment is still attached

                // If the response code is between 200 and 300
                if (response.isSuccessful()) {
                    // Get the list of events
                    List<Event> events  = gson.fromJson(response.body().string(), CalendarResponse.class).items;

                    // Because onResponse runs on a different thread than the UI one
                    // and the recyclerView can only be modified on the main thread
                    requireActivity().runOnUiThread(() -> {
                        recyclerView.setAdapter(new EventAdapter(getContext(), rootView, events));
                    });
                } else {
                    Snackbar.make(rootView, getString(R.string.calendarError), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}
        });
    }
}