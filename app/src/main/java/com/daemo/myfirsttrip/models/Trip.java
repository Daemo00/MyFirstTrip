package com.daemo.myfirsttrip.models;


import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Trip {
    private String id;
    private String title;
    private String subtitle;
    private boolean isDraft = false;
    private String oldId = null;
    private Map<String, Integer> peopleIds;

    public Trip() {

    }

    public Trip(String id) {
        this.id = id;
        this.title = null;
        this.subtitle = null;
        peopleIds = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
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
}
