package com.daemo.myfirsttrip.models;


import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Person {
    public String id;
    public String name;
    public String surname;
    public Map<String, Integer> trips_ids = new HashMap<>();
    private String oldId;
    private boolean isDraft = false;

    public Person() {
    }

    public Person(String id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public void setDraft(boolean draft) {
        isDraft = draft;
    }

    public Map<String, Integer> getTrips_ids() {
        return trips_ids;
    }

    public void setTrips_ids(Map<String, Integer> trips_ids) {
        this.trips_ids = trips_ids;
    }

    public String getOldId() {
        return oldId;
    }

    public void setOldId(String oldId) {
        this.oldId = oldId;
    }
}
