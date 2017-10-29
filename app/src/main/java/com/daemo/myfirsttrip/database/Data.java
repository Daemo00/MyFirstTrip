package com.daemo.myfirsttrip.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.daemo.myfirsttrip.models.Person;
import com.daemo.myfirsttrip.models.Trip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Data {

    @SuppressWarnings("SpellCheckingInspection")
    public static final List<Person> people = new ArrayList<>(Arrays.asList(
            new Person(0, "Roberta", "Cresta"),
            new Person(1, "Simone", "Rubino"),
            new Person(2, "Stefano", "Cresta"),
            new Person(3, "Valentina", "Rubino")));
    public static final List<Trip> trips = new ArrayList<>(Arrays.asList(
            new Trip(0, "Cracovia", "It was nice"),
            new Trip(1, "Palermo", "It was very nice"),
            new Trip(2, "Londra", "It was very nice"),
            new Trip(3, "Madrid", "It was nice")));

    private static final List<Pair<Integer, Integer>> personTripLinks = new ArrayList<>(Arrays.asList(
            new Pair<>(0, 0),
            new Pair<>(0, 1),
            new Pair<>(0, 2),
            new Pair<>(0, 3),
            new Pair<>(1, 0),
            new Pair<>(1, 1),
//            new Pair<>(1, 2),
            new Pair<>(1, 3),
            new Pair<>(2, 0),
            new Pair<>(2, 1),
            new Pair<>(2, 2),
//            new Pair<>(2, 3),
            new Pair<>(3, 0),
//            new Pair<>(3, 1),
            new Pair<>(3, 2),
            new Pair<>(3, 3)
    ));

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
        List<Trip> trips = new ArrayList<>();
        for (Pair<Integer, Integer> personTripLink : personTripLinks)
            if (personTripLink.first == person.id)
                trips.add(getTrip(personTripLink.second));
        return trips;
    }

    public static List<Person> getPeople(Trip trip) {
        List<Person> people = new ArrayList<>();
        for (Pair<Integer, Integer> personTripLink : personTripLinks)
            if (personTripLink.second == trip.id)
                people.add(getPerson(personTripLink.first));
        return people;
    }

    public static Trip getTripDraft() {
        Trip trip = new Trip(trips.size(), null, null);
        trip.isDraft = true;
        return trip;
    }

    public static Person getPersonDraft() {
        Person person = new Person(people.size(), null, null);
        person.isDraft = true;
        return person;
    }

    public static void addPersonTripLink(@NonNull Person person, @NonNull Trip trip) {
        personTripLinks.add(new Pair<>(person.id, trip.id));
    }

    public static Person commitPersonDraft(@NonNull Person person) {
        person.isDraft = false;
        if (people.add(person))
            return getPerson(person.id);
        return null;
    }

    public static Trip commitTripDraft(@NonNull Trip trip) {
        trip.isDraft = false;
        if (trips.add(trip))
            return getTrip(trip.id);
        return null;
    }
}
