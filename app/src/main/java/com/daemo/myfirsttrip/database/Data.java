package com.daemo.myfirsttrip.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.models.Person;
import com.daemo.myfirsttrip.models.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
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
import java.util.Set;

public class Data {

    @NonNull
    public static DocumentReference getTripRef(@Nullable String id) {
        if (id == null || id.isEmpty())
            return FirebaseFirestore.getInstance()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document();
        else
            return FirebaseFirestore.getInstance()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document(id);
    }

    @NonNull
    public static DocumentReference getPersonRef(@Nullable String id) {
        if (id == null || id.isEmpty())
            return FirebaseFirestore.getInstance()
                    .collection(Constants.PEOPLE_COLLECTION)
                    .document();
        else
            return FirebaseFirestore.getInstance()
                    .collection(Constants.PEOPLE_COLLECTION)
                    .document(id);
    }

    public static void commitPersonBatch(@NonNull Person person, OnCompleteListener<Void> onCompleteListener) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        // Remove the draft flag
        DocumentReference personDocReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION)
                .document(person.getId());
        // Keep the old id for deleting it afterwards
        String oldId = person.getOldId();
        person.setOldId(null);
        person.setDraft(false);
        batch.set(personDocReference, person);

        // Delete the old item, if any
        if (!(oldId == null || oldId.isEmpty())) {
            personDocReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION)
                    .document(oldId);
            deletePersonBatch(personDocReference, batch, onCompleteListener);
        } else
            batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void commitTripBatch(@NonNull Trip trip, OnCompleteListener<Void> onCompleteListener) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        // Remove the draft flag
        DocumentReference tripDocReference = FirebaseFirestore.getInstance().collection(Constants.TRIPS_COLLECTION)
                .document(trip.getId());
        // Keep the old id for deleting it afterwards
        String oldId = trip.getOldId();
        trip.setOldId(null);
        trip.setDraft(false);
        batch.set(tripDocReference, trip);

        // Delete the old item, if any
        if (!(oldId == null || oldId.isEmpty())) {
            tripDocReference = FirebaseFirestore.getInstance().collection(Constants.TRIPS_COLLECTION)
                    .document(oldId);
            deleteTripBatch(tripDocReference, batch, onCompleteListener);
        } else
            batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void deletePersonBatch(String id, OnCompleteListener<Void> onCompleteListener) {
        deletePersonBatch(FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION).document(id), null, onCompleteListener);
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
        for (Map.Entry<String, Integer> tripEntry : person.getTripsIds().entrySet())
            tripDocReferences.add(personDocReference.getFirestore()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document(tripEntry.getKey()));

        // Update to be applied to trips
        Map<String, Object> updates = new HashMap<>();
        updates.put(String.format(Locale.getDefault(), "peopleIds.%s", person.getId()), FieldValue.delete());

        for (DocumentReference tripDocReference : tripDocReferences)
            batch.update(tripDocReference, updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void deleteTripBatch(String id, OnCompleteListener<Void> onCompleteListener) {
        deleteTripBatch(FirebaseFirestore.getInstance().collection(Constants.TRIPS_COLLECTION).document(id), null, onCompleteListener);
    }

    private static void deleteTripBatch(DocumentReference tripDocReference, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        tripDocReference.get().addOnCompleteListener(runnable -> deleteTripBatch(runnable.getResult(), batch, onCompleteListener));
    }

    public static void deleteTripBatch(DocumentSnapshot tripDocSnapshot, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        if (!tripDocSnapshot.exists()) return;
        DocumentReference tripDocReference = tripDocSnapshot.getReference();
        Trip trip = tripDocSnapshot.toObject(Trip.class);
        if (batch == null)
            batch = tripDocReference.getFirestore().batch();
        // Delete the trip
        batch.delete(tripDocReference);

        // Delete all its links in other people it references!
        List<DocumentReference> personDocReferences = new ArrayList<>();
        for (Map.Entry<String, Integer> personEntry : trip.getPeopleIds().entrySet())
            personDocReferences.add(tripDocReference.getFirestore()
                    .collection(Constants.PEOPLE_COLLECTION)
                    .document(personEntry.getKey()));

        // Update to be applied to people
        Map<String, Object> updates = new HashMap<>();
        updates.put(String.format(Locale.getDefault(), "tripsIds.%s", trip.getId()), FieldValue.delete());

        for (DocumentReference personDocReference : personDocReferences)
            personDocReference.update(updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void updatePersonBatch(Person person, Set<String> unselected_ids, OnCompleteListener<Void> onCompleteListener) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        DocumentReference personDocReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION).document(person.id);
        batch.set(personDocReference, person);

        // Remove selection from deselected people
        for (String unselected_id : unselected_ids) {
            DocumentReference tripDocReference = FirebaseFirestore.getInstance().collection(Constants.TRIPS_COLLECTION).document(unselected_id);
            String field = String.format(Locale.getDefault(), "peopleIds.%s", person.getId());
            batch.update(tripDocReference, field, FieldValue.delete());
        }

        // Update all its links in other trips it references!
        List<DocumentReference> tripDocReferences = new ArrayList<>();
        for (Map.Entry<String, Integer> tripEntry : person.tripsIds.entrySet())
            tripDocReferences.add(personDocReference.getFirestore()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document(tripEntry.getKey()));

        Map<String, Object> updates = new HashMap<>();
        updates.put(String.format(Locale.getDefault(), "peopleIds.%s", person.id), 1);

        for (DocumentReference tripDocReference : tripDocReferences)
            batch.update(tripDocReference, updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void updateTripBatch(Trip trip, Set<String> unselected_ids, OnCompleteListener<Void> onCompleteListener) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        DocumentReference tripDocReference = FirebaseFirestore.getInstance()
                .collection(Constants.TRIPS_COLLECTION).document(trip.getId());
        batch.set(tripDocReference, trip);

        // Remove selection from deselected people
        for (String unselected_id : unselected_ids) {
            DocumentReference personDocReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION).document(unselected_id);
            String field = String.format(Locale.getDefault(), "tripsIds.%s", trip.getId());
            batch.update(personDocReference, field, FieldValue.delete());
        }

        // Update all its links in other people it references!
        List<DocumentReference> personDocReferences = new ArrayList<>();
        for (Map.Entry<String, Integer> personEntry : trip.getPeopleIds().entrySet()) {
            DocumentReference documentReference = FirebaseFirestore.getInstance()
                    .collection(Constants.PEOPLE_COLLECTION)
                    .document(personEntry.getKey());
            personDocReferences.add(documentReference);
        }

        // Updates to apply to every person
        Map<String, Object> updates = new HashMap<>();
        String field = String.format(Locale.getDefault(), "tripsIds.%s", trip.getId());
        updates.put(field, 1);

        for (DocumentReference personDocReference : personDocReferences)
            batch.update(personDocReference, updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void createDraftPersonFromRef(DocumentReference personDocReference, OnCompleteListener<DocumentReference> onCompleteListener) {
        DocumentReference draftPersonDocReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION).document();
        if (personDocReference == null) {
            WriteBatch batch = FirebaseFirestore.getInstance().batch();
            Person person = new Person(draftPersonDocReference.getId());
            person.setDraft(true);
            batch.set(draftPersonDocReference, person);
            batch.commit().addOnCompleteListener(task ->
                    Tasks.forResult(draftPersonDocReference).addOnCompleteListener(onCompleteListener));
            return;
        }
        FirebaseFirestore.getInstance().runTransaction(transaction -> {
            DocumentSnapshot documentSnapshot = transaction.get(personDocReference);
            transaction.update(personDocReference, "newId", draftPersonDocReference.getId());
            Person person = documentSnapshot.toObject(Person.class);
            person.setDraft(true);
            person.setOldId(person.getId());
            person.setId(draftPersonDocReference.getId());
            transaction.set(draftPersonDocReference, person);

            // Update all its links in other people it references!
            List<DocumentReference> tripDocReferences = new ArrayList<>();
            for (Map.Entry<String, Integer> tripEntry : person.getTripsIds().entrySet())
                tripDocReferences.add(FirebaseFirestore.getInstance()
                        .collection(Constants.TRIPS_COLLECTION)
                        .document(tripEntry.getKey()));

            // Updates to apply to every trip
            Map<String, Object> updates = new HashMap<>();
            String field = String.format(Locale.getDefault(), "peopleIds.%s", person.getId());
            updates.put(field, 1);

            for (DocumentReference tripDocReference : tripDocReferences)
                transaction.update(tripDocReference, updates);

            return draftPersonDocReference;
        }).addOnCompleteListener(onCompleteListener);
    }

    public static void createDraftTripFromRef(@Nullable DocumentReference tripDocReference, OnCompleteListener<DocumentReference> onCompleteListener) {
        DocumentReference draftTripDocReference = FirebaseFirestore.getInstance().collection(Constants.TRIPS_COLLECTION).document();
        if (tripDocReference == null) {
            WriteBatch batch = FirebaseFirestore.getInstance().batch();
            Trip trip = new Trip(draftTripDocReference.getId());
            trip.setDraft(true);
            batch.set(draftTripDocReference, trip);
            batch.commit().addOnCompleteListener(task ->
                    Tasks.forResult(draftTripDocReference).addOnCompleteListener(onCompleteListener));
            return;
        }
        FirebaseFirestore.getInstance().runTransaction(transaction -> {
            DocumentSnapshot documentSnapshot = transaction.get(tripDocReference);
            transaction.update(tripDocReference, "newId", draftTripDocReference.getId());
            Trip trip = documentSnapshot.toObject(Trip.class);
            trip.setDraft(true);
            trip.setOldId(trip.getId());
            trip.setId(draftTripDocReference.getId());
            transaction.set(draftTripDocReference, trip);

            // Update all its links in other people it references!
            List<DocumentReference> personDocReferences = new ArrayList<>();
            for (Map.Entry<String, Integer> personEntry : trip.getPeopleIds().entrySet())
                personDocReferences.add(FirebaseFirestore.getInstance()
                        .collection(Constants.PEOPLE_COLLECTION)
                        .document(personEntry.getKey()));

            // Updates to apply to every person
            Map<String, Object> updates = new HashMap<>();
            String field = String.format(Locale.getDefault(), "tripsIds.%s", trip.getId());
            updates.put(field, 1);

            for (DocumentReference personDocReference : personDocReferences)
                transaction.update(personDocReference, updates);

            return draftTripDocReference;
        }).addOnCompleteListener(onCompleteListener);
    }
}
