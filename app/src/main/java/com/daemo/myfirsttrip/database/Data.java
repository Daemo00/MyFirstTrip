package com.daemo.myfirsttrip.database;

import android.support.annotation.Nullable;

import com.daemo.myfirsttrip.models.Trip;

import java.util.Arrays;
import java.util.List;

public class Data {

    public static final List<Trip> trips = Arrays.asList(
            new Trip(1, "Cracovia", "It was nice"),
            new Trip(2, "Palermo", "It was very nice"),
            new Trip(3, "Londra", "It was very nice"),
            new Trip(4, "Madrid", "It was nice"));

    @Nullable
    public static Trip getTrip(int id) {
        for (Trip trip : trips)
            if (trip.id == id)
                return trip;
        return null;
    }
}
