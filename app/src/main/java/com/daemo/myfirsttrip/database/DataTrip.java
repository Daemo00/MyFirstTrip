package com.daemo.myfirsttrip.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.daemo.myfirsttrip.common.Constants;
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


public class DataTrip {

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

        List<DocumentReference> docReferences = new ArrayList<>();
        // Delete all its links in other people it references!
        for (Map.Entry<String, Integer> costEntry : trip.getCostsIds().entrySet())
            docReferences.add(tripDocReference.getFirestore()
                    .collection(Constants.COSTS_COLLECTION)
                    .document(costEntry.getKey()));

        // Delete all its links in other trips it references!
        for (Map.Entry<String, Integer> personEntry : trip.getPeopleIds().entrySet())
            docReferences.add(tripDocReference.getFirestore()
                    .collection(Constants.PEOPLE_COLLECTION)
                    .document(personEntry.getKey()));

        // Update to be applied to costs and trips
        Map<String, Object> updates = new HashMap<>();
        updates.put(String.format(Locale.getDefault(), "tripsIds.%s", trip.getId()), FieldValue.delete());

        for (DocumentReference docReference : docReferences)
            docReference.update(updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void updateTripBatch(Trip trip, List<DocumentReference> unselected_ids, OnCompleteListener<Void> onCompleteListener) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        DocumentReference tripDocReference = FirebaseFirestore.getInstance()
                .collection(Constants.TRIPS_COLLECTION).document(trip.getId());
        batch.set(tripDocReference, trip);

        // Remove selection from other deselected items
        for (DocumentReference docReference : unselected_ids) {
            String field = String.format(Locale.getDefault(), "tripsIds.%s", trip.getId());
            batch.update(docReference, field, FieldValue.delete());
        }

        List<DocumentReference> docReferences = new ArrayList<>();
        // Update all its links in other costs it references!
        for (Map.Entry<String, Integer> costEntry : trip.getCostsIds().entrySet())
            docReferences.add(tripDocReference.getFirestore()
                    .collection(Constants.COSTS_COLLECTION)
                    .document(costEntry.getKey()));
        // Update all its links in other trips it references!
        for (Map.Entry<String, Integer> personEntry : trip.getPeopleIds().entrySet())
            docReferences.add(tripDocReference.getFirestore()
                    .collection(Constants.PEOPLE_COLLECTION)
                    .document(personEntry.getKey()));

        // Updates to apply to every item
        Map<String, Object> updates = new HashMap<>();
        String field = String.format(Locale.getDefault(), "tripsIds.%s", trip.getId());
        updates.put(field, 1);

        for (DocumentReference docReference : docReferences)
            batch.update(docReference, updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
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

            List<DocumentReference> docReferences = new ArrayList<>();
            // Update all its links in other items it references!
            for (Map.Entry<String, Integer> costEntry : trip.getCostsIds().entrySet())
                docReferences.add(FirebaseFirestore.getInstance()
                        .collection(Constants.COSTS_COLLECTION)
                        .document(costEntry.getKey()));
            for (Map.Entry<String, Integer> personEntry : trip.getPeopleIds().entrySet())
                docReferences.add(FirebaseFirestore.getInstance()
                        .collection(Constants.PEOPLE_COLLECTION)
                        .document(personEntry.getKey()));

            // Updates to apply to every item
            Map<String, Object> updates = new HashMap<>();
            String field = String.format(Locale.getDefault(), "tripsIds.%s", trip.getId());
            updates.put(field, 1);

            for (DocumentReference docReference : docReferences)
                transaction.update(docReference, updates);

            return draftTripDocReference;
        }).addOnCompleteListener(onCompleteListener);
    }
}
