package com.example.fermicalendar;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

// This adapter simply binds the Event objects to the RecyclerView so they can be
// displayed in a scrollable list
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private final List<Event> events;
    private final Context context;
    private final View rootView;

    public EventAdapter(Context context, View rootView, List<Event> events) {
        this.events = events;
        this.context = context;
        this.rootView = rootView;
    }

    // This class represents one element in the list
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, time;

        EventViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        ZonedDateTime start, end;

        // Set the summary of the event
        Event event = events.get(position);
        holder.title.setText(event.summary);

        // Set the dates of the event
        if(event.start.dateTime != null && event.end.dateTime != null) {
            start = ZonedDateTime.parse(event.start.dateTime);
            end = ZonedDateTime.parse(event.end.dateTime);
            holder.time.setText(String.format(Locale.ITALY,
                    "%02d:%02d - %02d:%02d  %02d %s %02d", start.getHour(), start.getMinute(), end.getHour(), end.getMinute(),
                    start.getDayOfMonth(), start.getMonth(), start.getYear()));

            // Set the onclick listener for each event
            holder.itemView.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Aggiungere al calendario?")
                        .setMessage("Vuoi aggiungere alla tua app calendario questo evento?\n" + event.summary)
                        .setPositiveButton("OK", (dialog, which) -> {
                            Intent intent = new Intent(Intent.ACTION_INSERT);
                            intent.setData(CalendarContract.Events.CONTENT_URI);

                            // pre-fill event details
                            intent.putExtra(CalendarContract.Events.TITLE, context.getString(R.string.app_name));
                            intent.putExtra(CalendarContract.Events.DESCRIPTION, event.summary);

                            // Set start and end times in milliseconds (UTC timestamp)
                            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.toInstant().toEpochMilli());
                            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end.toInstant().toEpochMilli());

                            // Start the intent if a calendar app exists
                            context.startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}