package com.daemo.myfirsttrip.models;


public class Trip {
    public final String title;
    public final String subtitle;
    public final int id;
    public boolean isDraft = false;

    public Trip(int id, String title, String subtitle) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
    }
}
