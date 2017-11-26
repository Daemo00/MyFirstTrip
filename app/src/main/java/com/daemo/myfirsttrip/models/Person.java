package com.daemo.myfirsttrip.models;


import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Person {
    private String id;
    private String oldId = null;
    private boolean isDraft = false;
    private String name;
    private String surname;
    private Map<String, Integer> tripsIds = new HashMap<>();
    private Map<String, Integer> costsIds = new HashMap<>();

    public Person() {
    }

    public Person(String id) {
        this.id = id;
        this.name = null;
        this.surname = null;
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

    public Map<String, Integer> getTripsIds() {
        return tripsIds;
    }

    public void setTripsIds(Map<String, Integer> tripsIds) {
        this.tripsIds = tripsIds;
    }

    public String getOldId() {
        return oldId;
    }

    public void setOldId(String oldId) {
        this.oldId = oldId;
    }

    public Map<String, Integer> getCostsIds() {
        return costsIds;
    }

    public void setCostsIds(Map<String, Integer> costsIds) {
        this.costsIds = costsIds;
    }
}
