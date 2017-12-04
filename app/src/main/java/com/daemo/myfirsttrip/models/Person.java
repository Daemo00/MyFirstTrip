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
    private Map<String, Float> tripsIds = new HashMap<>();
    private Map<String, Float> costsIds = new HashMap<>();
    private Float totalDebt;

    public Person() {
    }

    public Person(String id) {
        this.id = id;
        this.name = null;
        this.surname = null;
        this.totalDebt = 0f;
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

    public Map<String, Float> getTripsIds() {
        return tripsIds;
    }

    public void setTripsIds(Map<String, Float> tripsIds) {
        this.tripsIds = tripsIds;
    }

    public String getOldId() {
        return oldId;
    }

    public void setOldId(String oldId) {
        this.oldId = oldId;
    }

    public Map<String, Float> getCostsIds() {
        return costsIds;
    }

    public void setCostsIds(Map<String, Float> costsIds) {
        this.costsIds = costsIds;
    }

    public Float getTotalDebt() {
        return totalDebt;
    }

    public void setTotalDebt(Float totalDebt) {
        this.totalDebt = totalDebt;
    }
}
