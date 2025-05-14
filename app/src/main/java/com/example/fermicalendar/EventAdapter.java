package com.example.fermicalendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

// This adapter simply binds the Event objects to the RecuclerView so they can be
// displayed in a scrollable list
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private final List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
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
        // Set the summary of the event
        Event event = events.get(position);
        holder.title.setText(event.summary);

        // Set the dates of the event
        if(event.start.dateTime != null && event.end.dateTime != null)
        {
            ZonedDateTime start = ZonedDateTime.parse(event.start.dateTime);
            ZonedDateTime end = ZonedDateTime.parse(event.end.dateTime);
            holder.time.setText(String.format(Locale.ITALY,
                    "%02d:%02d - %02d:%02d  %02d %s %02d", start.getHour(), start.getMinute(), end.getHour(), end.getMinute(),
                    start.getDayOfMonth(), start.getMonth(), start.getYear()));
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}