package com.daemo.myfirsttrip.models;


import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Cost {
    private String id;
    private String oldId = null;
    private boolean isDraft = false;
    private Float quantity;
    private String motivation;
    private Map<String, Integer> peopleIds = new HashMap<>();
    private Map<String, Integer> tripsIds = new HashMap<>();

    public Cost() {

    }

    public Cost(String id) {
        this.id = id;
        this.quantity = 1.0f;
        this.motivation = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Float getQuantity() {
        return quantity;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public void setDraft(boolean draft) {
        isDraft = draft;
    }

    public String getOldId() {
        return oldId;
    }

    public void setOldId(String oldId) {
        this.oldId = oldId;
    }

    public Map<String, Integer> getPeopleIds() {
        return peopleIds;
    }

    public void setPeopleIds(Map<String, Integer> peopleIds) {
        this.peopleIds = peopleIds;
    }

    public Map<String, Integer> getTripsIds() {
        return tripsIds;
    }

    public void setTripsIds(Map<String, Integer> tripsIds) {
        this.tripsIds = tripsIds;
    }
}
