package com.example.fermicalendar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchFragment extends Fragment {

    private EditText searchText;
    private RecyclerView recyclerView;
    private Gson gson;
    private OkHttpClient client;
    private View rootView;
    private MaterialAutoCompleteTextView classDropdown;

    public SearchFragment() { super(R.layout.fragment_search); }

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
        rootView = view.findViewById(R.id.rootSearch);

        // Set the dropdown
        classDropdown = view.findViewById(R.id.classDropDown);

        // Set the editText
        searchText = view.findViewById(R.id.searchEditText);

        // Build the url
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(getString(R.string.serverURL).replace("https://", ""))
                .addPathSegment("classes")
                .build();

        // Request the classes
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!isAdded()) {
                    // Fragment is no longer attached, ignore this callback
                    return;
                }

                // If the response code is between 200 and 300
                if (response.isSuccessful()) {
                    // Get the list of classes
                    List<String> classes = new ArrayList<>();
                    classes.add("None");
                    classes.addAll(gson.fromJson(response.body().string(), Classes.class).classes);

                    // Because onResponse runs on a different thread than the UI one
                    // and the recyclerView can only be modified on the main thread
                    requireActivity().runOnUiThread(() -> classDropdown.setSimpleItems(classes.toArray(new String[0])));
                } else {
                    Snackbar.make(rootView, getString(R.string.calendarError), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Snackbar.make(rootView, getString(R.string.calendarError), Snackbar.LENGTH_LONG).show();
            }
        });

        // Set the onclick method on the button
        Button searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {
            EditText searchText = view.findViewById(R.id.searchEditText);
            searchEvents(searchText.getText().toString());
        });

        // Set up the swipe-to-refresh listener
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.md_theme_background);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.md_theme_primaryContainer,
                R.color.md_theme_primaryContainer,
                R.color.md_theme_primaryContainer
        );
        // This method is called when the user pulls down to refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            searchEvents(searchText.getText().toString());

            // After fetching is complete, turn off the loading indicator
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    protected void searchEvents(String searchTerm) {
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
                .addQueryParameter("timeMin", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("timeMax", ZonedDateTime.now().plusWeeks(2).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
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
                    List<Event> events = gson.fromJson(response.body().string(), CalendarResponse.class).items;

                    // Filter the events out
                    for(int i = 0; i < events.size(); i++) {
                        if(!events.get(i).summary.toLowerCase().contains(searchTerm.toLowerCase()) ||
                                (!classDropdown.getText().toString().equals("None") &&
                                        !events.get(i).summary.toLowerCase().contains((classDropdown.getText().toString().replace(" ", "") + " ").toLowerCase()))) {
                            events.remove(i);
                            i--;
                        }
                    }

                    // Because onResponse runs on a different thread than the UI one
                    // and the recyclerView can only be modified on the main thread
                    requireActivity().runOnUiThread(() -> {
                        // Check to see if events is empty
                        if(events.size() == 0) {
                            // Clear the events list
                            recyclerView.setAdapter(new EventAdapter(new ArrayList<>()));
                            Snackbar.make(rootView, getString(R.string.no_events), Snackbar.LENGTH_LONG).show();
                        } else {
                            recyclerView.setAdapter(new EventAdapter(events));
                        }
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
