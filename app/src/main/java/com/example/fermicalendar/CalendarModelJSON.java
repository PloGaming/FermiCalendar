package com.example.fermicalendar;

import java.util.List;

class TimeInfo {
    String dateTime;
    String date; // For all time events
}

class Event {
    String id;
    String summary;
    TimeInfo start;
    TimeInfo end;
}

class CalendarResponse {
    List<Event> items;
}
