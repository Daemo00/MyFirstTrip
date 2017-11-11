package com.daemo.myfirsttrip.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.models.Person;
import com.daemo.myfirsttrip.models.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.common.base.Strings;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Data {

    @NonNull
    public static DocumentReference getTrip(String id) {
        if (Strings.isNullOrEmpty(id))
            return FirebaseFirestore.getInstance()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document();
        else
            return FirebaseFirestore.getInstance()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document(id);
    }

    @NonNull
    public static DocumentReference getPerson(String id) {
        if (Strings.isNullOrEmpty(id))
            return FirebaseFirestore.getInstance()
                    .collection(Constants.PEOPLE_COLLECTION)
                    .document();
        else
            return FirebaseFirestore.getInstance()
                    .collection(Constants.PEOPLE_COLLECTION)
                    .document(id);
    }

    public static void commitPersonBatch(@NonNull Person person, @Nullable WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        if (batch == null)
            batch = FirebaseFirestore.getInstance().batch();
        // Remove the draft flag
        DocumentReference personDocReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION)
                .document(person.getId());
        person.setDraft(false);
        batch.set(personDocReference, person);

        // Delete the old item, if any
        if (!Strings.isNullOrEmpty(person.getOldId())) {
            personDocReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION)
                    .document(person.getOldId());
            deletePersonBatch(personDocReference, batch, onCompleteListener);
        } else
            batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void commitTripBatch(@NonNull Trip trip, @Nullable WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        if (batch == null)
            batch = FirebaseFirestore.getInstance().batch();
        // Remove the draft flag
        DocumentReference tripDocReference = FirebaseFirestore.getInstance().collection(Constants.TRIPS_COLLECTION)
                .document(trip.getId());

        trip.setDraft(false);
        batch.set(tripDocReference, trip);

        // Delete the old item, if any
        if (!Strings.isNullOrEmpty(trip.getOldId())) {
            tripDocReference = FirebaseFirestore.getInstance().collection(Constants.TRIPS_COLLECTION)
                    .document(trip.getOldId());
            deleteTripBatch(tripDocReference, batch, onCompleteListener);
        } else
            batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void deletePersonBatch(String id, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        deletePersonBatch(FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION).document(id), batch, onCompleteListener);
    }

    public static void deletePersonBatch(DocumentReference personDocReference, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        personDocReference.get().addOnCompleteListener(runnable -> deletePersonBatch(runnable.getResult(), batch, onCompleteListener));
    }

    private static void deletePersonBatch(DocumentSnapshot personDocSnapshot, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        if (!personDocSnapshot.exists()) return;
        DocumentReference personDocReference = personDocSnapshot.getReference();
        Person person = personDocSnapshot.toObject(Person.class);
        if (batch == null)
            batch = personDocSnapshot.getReference().getFirestore().batch();
        // Delete the person
        batch.delete(personDocReference);

        // Delete all its links in other trips it references!
        List<DocumentReference> tripDocReferences = new ArrayList<>();
        for (Map.Entry<String, Integer> tripEntry : person.trips_ids.entrySet())
            tripDocReferences.add(personDocReference.getFirestore()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document(tripEntry.getKey()));

        Map<String, Object> updates = new HashMap<>();
        updates.put(String.format(Locale.getDefault(), "peopleIds.%s", person.id), FieldValue.delete());

        for (DocumentReference tripDocReference : tripDocReferences)
            batch.update(tripDocReference, updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void deleteTripBatch(String id, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        deleteTripBatch(FirebaseFirestore.getInstance().collection(Constants.TRIPS_COLLECTION).document(id), batch, onCompleteListener);
    }

    private static void deleteTripBatch(DocumentReference tripDocReference, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        tripDocReference.get().addOnCompleteListener(runnable -> deleteTripBatch(runnable.getResult(), batch, onCompleteListener));
    }

    public static void deleteTripBatch(DocumentSnapshot tripDocSnapshot, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        DocumentReference tripDocReference = tripDocSnapshot.getReference();
        if (!tripDocSnapshot.exists()) return;
        Trip trip = tripDocSnapshot.toObject(Trip.class);
        if (batch == null)
            batch = tripDocReference.getFirestore().batch();
        // Delete the trip
        batch.delete(tripDocReference);

        // Delete all its links in other people it references!
        List<DocumentReference> personDocReferences = new ArrayList<>();
        for (Map.Entry<String, Integer> personEntry : trip.getPeopleIds().entrySet())
            personDocReferences.add(tripDocReference.getFirestore()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document(personEntry.getKey()));

        Map<String, Object> updates = new HashMap<>();
        updates.put(String.format(Locale.getDefault(), "peopleIds.%s", trip.getId()), FieldValue.delete());

        for (DocumentReference personDocReference : personDocReferences)
            personDocReference.update(updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static DocumentReference updatePersonBatch(Person person, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        if (batch == null)
            batch = FirebaseFirestore.getInstance().batch();
        DocumentReference personDocReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION).document(person.id);
        batch.set(personDocReference, person);

        // Update all its links in other trips it references!
        List<DocumentReference> tripDocReferences = new ArrayList<>();
        for (Map.Entry<String, Integer> tripEntry : person.trips_ids.entrySet())
            tripDocReferences.add(personDocReference.getFirestore()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document(tripEntry.getKey()));

        Map<String, Object> updates = new HashMap<>();
        updates.put(String.format(Locale.getDefault(), "peopleIds.%s", person.id), 1);

        for (DocumentReference tripDocReference : tripDocReferences)
            batch.update(tripDocReference, updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
        return personDocReference;
    }

    public static DocumentReference updateTripBatch(Trip trip, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        if (batch == null)
            batch = FirebaseFirestore.getInstance().batch();
        DocumentReference tripDocReference = FirebaseFirestore.getInstance()
                .collection(Constants.TRIPS_COLLECTION).document(trip.getId());
        batch.set(tripDocReference, trip);

        // Update all its links in other people it references!
        List<DocumentReference> personDocReferences = new ArrayList<>();
        for (Map.Entry<String, Integer> personEntry : trip.getPeopleIds().entrySet())
            personDocReferences.add(tripDocReference.getFirestore()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document(personEntry.getKey()));

        Map<String, Object> updates = new HashMap<>();
        updates.put(String.format(Locale.getDefault(), "peopleIds.%s", trip.getId()), 1);

        for (DocumentReference personDocReference : personDocReferences)
            personDocReference.update(updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
        return tripDocReference;
    }

    public static Trip createDraftTripBatch(Trip trip, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        if (batch == null)
            batch = FirebaseFirestore.getInstance().batch();
        DocumentReference draftTripDocReference = FirebaseFirestore.getInstance().collection(Constants.TRIPS_COLLECTION).document();
        Trip draft = new Trip(draftTripDocReference.getId(), trip.getTitle(), trip.getSubtitle());
        draft.setOldId(trip.getId());
        draft.setDraft(true);
        batch.set(draftTripDocReference, draft);
        batch.commit().addOnCompleteListener(onCompleteListener);
        return draft;
    }

    public static Person createDraftPersonBatch(Person person, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        if (batch == null)
            batch = FirebaseFirestore.getInstance().batch();
        DocumentReference draftPersonDocReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION).document();
        Person draft = new Person(draftPersonDocReference.getId(), person.getName(), person.getSurname());
        draft.setOldId(person.getId());
        draft.setDraft(true);
        batch.set(draftPersonDocReference, draft);
        batch.commit().addOnCompleteListener(onCompleteListener);
        return draft;
    }
}
