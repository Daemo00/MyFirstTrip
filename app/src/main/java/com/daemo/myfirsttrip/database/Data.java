package com.daemo.myfirsttrip.database;

import android.support.annotation.Nullable;

import com.daemo.myfirsttrip.models.Person;
import com.daemo.myfirsttrip.models.Trip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Data {

    public static final List<Person> people = Arrays.asList(
            new Person(1, "Roberta", "Cresta"),
            new Person(2, "Simone", "Rubino"),
            new Person(3, "Stefano", "Cresta"),
            new Person(4, "Valentina", "Rubino"));
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

    @Nullable
    public static Person getPerson(int id) {
        for (Person person : people)
            if (person.id == id)
                return person;
        return null;
    }

    public static List<Trip> getTrips(Person person) {
        switch (person.id) {
            case 1:
                return Arrays.asList(getTrip(1), getTrip(2), getTrip(3));
            case 2:
                return Arrays.asList(getTrip(2), getTrip(3), getTrip(4));
            case 3:
                return Arrays.asList(getTrip(3), getTrip(4), getTrip(1));
            case 4:
                return Arrays.asList(getTrip(4), getTrip(1), getTrip(2));
            default:
                return new ArrayList<>();
        }
    }

    public static List<Person> getPeople(Trip trip) {
        List<Person> people = new ArrayList<>();
        for (Person person : Data.people)
            if (getTrips(person).contains(trip))
                people.add(person);
        return people;
    }
}
