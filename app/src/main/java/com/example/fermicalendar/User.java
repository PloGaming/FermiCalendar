package com.example.fermicalendar;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String name, schoolClass;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String schoolClass) {
        this.name = name;
        this.schoolClass = schoolClass;
    }
}