package com.example.fermicalendar;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

class TimeInfo {
    String dateTime;
}

class Event {
    String summary;
    TimeInfo start;
    TimeInfo end;
}

class CalendarResponse {
    List<Event> items;
}

class Classes {
    List<String> classes;
}

@IgnoreExtraProperties
class User {

    public String name, schoolClass;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String schoolClass) {
        this.name = name;
        this.schoolClass = schoolClass;
    }
}