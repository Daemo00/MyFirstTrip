package com.daemo.myfirsttrip.common;


import com.daemo.myfirsttrip.BuildConfig;

public class Constants {
    public static final String ACTION_TRIP_SELECTED = BuildConfig.APPLICATION_ID.concat(".ACTION_TRIP_SELECTED");

    public static final String EXTRA_ADD_TO_BACKSTACK = BuildConfig.APPLICATION_ID.concat(".EXTRA_ADD_TO_BACKSTACK");
    public static final String EXTRA_REPLACE_FRAGMENT = BuildConfig.APPLICATION_ID.concat(".EXTRA_REPLACE_FRAGMENT");
    public static final String EXTRA_BUNDLE_FOR_FRAGMENT = BuildConfig.APPLICATION_ID.concat(".EXTRA_BUNDLE_FOR_FRAGMENT");

    public static final String EXTRA_TRIP_ID = BuildConfig.APPLICATION_ID.concat(".EXTRA_TRIP_ID");
    public static final String EXTRA_PERSON_ID = BuildConfig.APPLICATION_ID.concat(".EXTRA_PERSON_ID");
    public static final String EXTRA_COST_ID = BuildConfig.APPLICATION_ID.concat(".EXTRA_COST_ID");

    public static final String EXTRA_ITEM_ADD = BuildConfig.APPLICATION_ID.concat(".EXTRA_ADD");
    public static final String EXTRA_EDIT = BuildConfig.APPLICATION_ID.concat(".EXTRA_EDIT");
    public static final String EXTRA_CHOOSE = BuildConfig.APPLICATION_ID.concat(".EXTRA_CHOOSE");
    public static final String EXTRA_UNIQUE = BuildConfig.APPLICATION_ID.concat(".EXTRA_UNIQUE");

    public static final String TRIPS_COLLECTION = "trips";
    public static final String PEOPLE_COLLECTION = "people";
    public static final String COSTS_COLLECTION = "costs";
    public static final long QUERY_LIMIT = 100;
}
