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
        if (oldId != null && !oldId.isEmpty())
            deleteTripBatch(
                    FirebaseFirestore.getInstance().collection(Constants.TRIPS_COLLECTION).document(oldId),
                    batch, onCompleteListener);
        else
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

        // Delete all its links in other costs it references!
        for (Map.Entry<String, Float> costEntry : trip.getCostsIds().entrySet())
            batch.update(
                    FirebaseFirestore.getInstance().collection(Constants.COSTS_COLLECTION).document(costEntry.getKey()),
                    "tripId",
                    trip.isDraft() ?
                            FieldValue.delete() :
                            trip.getNewId() != null ?
                                    trip.getNewId() :
                                    FieldValue.delete());

        // Delete all its links in other people it references!
        for (Map.Entry<String, Float> personEntry : trip.getPeopleIds().entrySet())
            batch.update(
                    FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION).document(personEntry.getKey()),
                    String.format(Locale.getDefault(), "tripsIds.%s", trip.getId()),
                    FieldValue.delete());

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void updateTripBatch(Trip trip, List<DocumentReference> unselectedDocReferences, OnCompleteListener<Void> onCompleteListener) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        DocumentReference tripDocReference = FirebaseFirestore.getInstance()
                .collection(Constants.TRIPS_COLLECTION).document(trip.getId());
        batch.set(tripDocReference, trip);

        // Remove trip from costs and people that have been deselected
        for (DocumentReference docReference : unselectedDocReferences)
            if (docReference.getParent().getId().equals(Constants.COSTS_COLLECTION))
                batch.update(docReference,
                        "tripId",
                        FieldValue.delete());
            else if (docReference.getParent().getId().equals(Constants.PEOPLE_COLLECTION))
                batch.update(docReference,
                        String.format(Locale.getDefault(), "tripsIds.%s", trip.getId()),
                        FieldValue.delete());

        // Update all its links in other costs it references!
        for (Map.Entry<String, Float> costEntry : trip.getCostsIds().entrySet())
            batch.update(
                    FirebaseFirestore.getInstance().collection(Constants.COSTS_COLLECTION).document(costEntry.getKey()),
                    "tripId",
                    trip.getId());

        // Update all its links in other people it references!
        for (Map.Entry<String, Float> personEntry : trip.getPeopleIds().entrySet())
            batch.update(
                    FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION).document(personEntry.getKey()),
                    String.format(Locale.getDefault(), "tripsIds.%s", trip.getId()),
                    0f);

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
            // Store the original trip and make it a draft
            Trip trip = documentSnapshot.toObject(Trip.class);
            trip.setDraft(true);
            trip.setOldId(trip.getId());
            trip.setId(draftTripDocReference.getId());
            // Store this into the draftTripDocReference
            transaction.set(draftTripDocReference, trip);

            // Update all its links in other costs it references!
            for (Map.Entry<String, Float> costEntry : trip.getCostsIds().entrySet())
                transaction.update(
                        FirebaseFirestore.getInstance().collection(Constants.COSTS_COLLECTION).document(costEntry.getKey()),
                        "tripId",
                        trip.getId());

            // Update all its links in other people it references!
            for (Map.Entry<String, Float> personEntry : trip.getPeopleIds().entrySet())
                transaction.update(
                        FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION).document(personEntry.getKey()),
                        String.format(Locale.getDefault(), "tripsIds.%s", trip.getId()),
                        0f);

            return draftTripDocReference;
        }).addOnCompleteListener(onCompleteListener);
    }
}
