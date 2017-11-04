package com.daemo.myfirsttrip.models;


public class Trip {
    public final int id;
    public String title;
    public String subtitle;
    public boolean isDraft = false;
    public int old_id;

    public Trip(int id, String title, String subtitle) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
