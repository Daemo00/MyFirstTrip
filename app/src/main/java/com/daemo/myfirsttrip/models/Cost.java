package com.daemo.myfirsttrip.models;


import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Cost {
    private String id;
    private String oldId = null;
    private boolean isDraft = false;
    private Float quantity = 0f;
    private String motivation = "";
    private String payerId = "";
    /**
     * Map from person id to how much it contributes to the cost
     */
    private Map<String, Float> peopleIds = new HashMap<>();
    private String tripId = ""; // TODO get rid of tripsIds and use correctly this, especially in data class

    public Cost() {

    }

    public Cost(String id) {
        this.id = id;
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

    public Map<String, Float> getPeopleIds() {
        return peopleIds;
    }

    public void setPeopleIds(Map<String, Float> peopleIds) {
        this.peopleIds = peopleIds;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }
}
