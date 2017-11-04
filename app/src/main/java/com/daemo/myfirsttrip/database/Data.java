package com.daemo.myfirsttrip.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.daemo.myfirsttrip.models.Person;
import com.daemo.myfirsttrip.models.Trip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Data {

    @SuppressWarnings("SpellCheckingInspection")
    private static final List<Person> people = new ArrayList<>(Arrays.asList(
            new Person(0, "Roberta", "Cresta"),
            new Person(1, "Simone", "Rubino"),
            new Person(2, "Stefano", "Cresta"),
            new Person(3, "Valentina", "Rubino")));
    private static final List<Trip> trips = new ArrayList<>(Arrays.asList(
            new Trip(0, "Cracovia", "It was nice"),
            new Trip(1, "Palermo", "It was very nice"),
            new Trip(2, "Londra", "It was very nice"),
            new Trip(3, "Madrid", "It was nice")));

    private static final Set<Pair<Integer, Integer>> personTripLinks = new HashSet<>(Arrays.asList(
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

    @NonNull
    public static List<Trip> getTrips(@Nullable Person person) {
        if (person == null) return trips;
        List<Trip> trips = new ArrayList<>();
        for (Pair<Integer, Integer> personTripLink : personTripLinks)
            if (personTripLink.first == person.id)
                trips.add(getTrip(personTripLink.second));
        return trips;
    }

    @NonNull
    public static List<Person> getPeople(@Nullable Trip trip) {
        if (trip == null) return people;
        List<Person> people = new ArrayList<>();
        for (Pair<Integer, Integer> personTripLink : personTripLinks)
            if (personTripLink.second == trip.id)
                people.add(getPerson(personTripLink.first));
        return people;
    }

    @NonNull
    public static Trip getTripDraft() {
        Trip trip = new Trip(
                Collections.max(trips, (t1, t2) -> t1.id - t2.id).id + 1,
                null,
                null);
        trip.isDraft = true;
        trips.add(trip);
        return trip;
    }

    @NonNull
    public static Person getPersonDraft() {
        Person person = new Person(
                Collections.max(people, (p1, p2) -> p1.id - p2.id).id + 1,
                null,
                null);
        person.isDraft = true;
        people.add(person);
        return person;
    }

//    public static void addPersonTripLink(int personId, int tripId) {
//        personTripLinks.add(new Pair<>(personId, tripId));
//    }

    @Nullable
    public static Person commitPersonDraft(@NonNull Person person) {
        person.isDraft = false;
        Data.removeTrip(Data.getTrip(person.old_id), null);
        if (people.contains(person) || people.add(person))
            return getPerson(person.id);
        return null;
    }

    @Nullable
    public static Trip commitTripDraft(@NonNull Trip trip) {
        trip.isDraft = false;
        Data.removeTrip(Data.getTrip(trip.old_id), null);
        if (trips.contains(trip) || trips.add(trip))
            return getTrip(trip.id);
        return null;
    }

    /**
     * If person is null remove the trip and all its links
     */
    public static void removeTrip(Trip trip, @Nullable Person person) {
        if (person == null) trips.remove(trip);
        Iterator<Pair<Integer, Integer>> iterator = personTripLinks.iterator();
        while (iterator.hasNext()) {
            Pair<Integer, Integer> next = iterator.next();
            if (next.second == trip.id && (person == null || next.first == person.id))
                iterator.remove();
        }
    }

    /**
     * If trip is null remove the person and all its links
     */
    public static void removePerson(Person person, @Nullable Trip trip) {
        if (trip == null) people.remove(person);
        Iterator<Pair<Integer, Integer>> iterator = personTripLinks.iterator();
        while (iterator.hasNext()) {
            Pair<Integer, Integer> next = iterator.next();
            if (next.first == person.id && (trip == null || next.second == trip.id))
                iterator.remove();
        }
    }

    public static boolean addTrip(Trip trip, Set<Integer> selected_person_ids) {
        boolean res = trips.add(trip);
        if (!res) return false;
        for (Integer selected_person_id : selected_person_ids)
            res &= personTripLinks.add(new Pair<>(selected_person_id, trip.id));
        return res;
    }

    public static boolean addPerson(Person person, Set<Integer> selected_trip_ids) {
        boolean res = people.add(person);
        if (!res) return false;
        for (Integer selected_trip_id : selected_trip_ids)
            res &= personTripLinks.add(new Pair<>(person.id, selected_trip_id));
        return res;
    }

    public static Trip getTripDraft(Trip trip) {
        Trip draft = getTripDraft();
        draft.title = trip.title;
        draft.subtitle = trip.subtitle;
        draft.old_id = trip.id;
        List<Person> people = getPeople(trip);
        for (Person person : people)
            personTripLinks.add(new Pair<>(person.id, draft.id));
        return draft;
    }

    public static Person getPersonDraft(Person person) {
        Person draft = getPersonDraft();
        draft.name = person.name;
        draft.surname = person.surname;
        draft.old_id = person.id;
        List<Trip> trips = getTrips(person);
        for (Trip trip : trips)
            personTripLinks.add(new Pair<>(draft.id, trip.id));

        return draft;
    }
}
