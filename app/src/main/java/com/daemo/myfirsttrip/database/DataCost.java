package com.daemo.myfirsttrip.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.models.Cost;
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


public class DataCost {

    @NonNull
    public static DocumentReference getCostRef(@Nullable String id) {
        if (id == null || id.isEmpty())
            return FirebaseFirestore.getInstance()
                    .collection(Constants.COSTS_COLLECTION)
                    .document();
        else
            return FirebaseFirestore.getInstance()
                    .collection(Constants.COSTS_COLLECTION)
                    .document(id);
    }

    public static void commitCostBatch(@NonNull Cost cost, OnCompleteListener<Void> onCompleteListener) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        // Remove the draft flag
        DocumentReference costDocReference = FirebaseFirestore.getInstance().collection(Constants.COSTS_COLLECTION)
                .document(cost.getId());
        // Keep the old id for deleting it afterwards
        String oldId = cost.getOldId();
        cost.setOldId(null);
        cost.setDraft(false);
        batch.set(costDocReference, cost);

        // Delete the old item, if any
        if (!(oldId == null || oldId.isEmpty())) {
            costDocReference = FirebaseFirestore.getInstance().collection(Constants.COSTS_COLLECTION)
                    .document(oldId);
            deleteCostBatch(costDocReference, batch, onCompleteListener);
        } else
            batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void deleteCostBatch(String id, OnCompleteListener<Void> onCompleteListener) {
        deleteCostBatch(FirebaseFirestore.getInstance().collection(Constants.COSTS_COLLECTION).document(id), null, onCompleteListener);
    }

    private static void deleteCostBatch(DocumentReference costDocReference, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        costDocReference.get().addOnCompleteListener(runnable -> deleteCostBatch(runnable.getResult(), batch, onCompleteListener));
    }

    public static void deleteCostBatch(DocumentSnapshot costDocSnapshot, WriteBatch batch, OnCompleteListener<Void> onCompleteListener) {
        if (!costDocSnapshot.exists()) return;
        DocumentReference costDocReference = costDocSnapshot.getReference();
        Cost cost = costDocSnapshot.toObject(Cost.class);
        if (batch == null)
            batch = costDocReference.getFirestore().batch();
        // Delete the cost
        batch.delete(costDocReference);

        List<DocumentReference> docReferences = new ArrayList<>();
        // Delete all its links in other trips it references!
        for (Map.Entry<String, Float> personEntry : cost.getPeopleIds().entrySet())
            docReferences.add(costDocReference.getFirestore()
                    .collection(Constants.PEOPLE_COLLECTION)
                    .document(personEntry.getKey()));

        // Delete all its links in other trips it references!
        for (Map.Entry<String, Float> tripEntry : cost.getTripsIds().entrySet())
            docReferences.add(costDocReference.getFirestore()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document(tripEntry.getKey()));

        // Update to be applied to costs and trips
        Map<String, Object> updates = new HashMap<>();
        updates.put(String.format(Locale.getDefault(), "costsIds.%s", cost.getId()), FieldValue.delete());

        for (DocumentReference docReference : docReferences)
            docReference.update(updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void updateCostBatch(Cost cost, List<DocumentReference> unselected_ids, OnCompleteListener<Void> onCompleteListener) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        DocumentReference costDocReference = FirebaseFirestore.getInstance()
                .collection(Constants.COSTS_COLLECTION).document(cost.getId());
        batch.set(costDocReference, cost);

        // Remove selection from other deselected items
        for (DocumentReference docReference : unselected_ids) {
            String field = String.format(Locale.getDefault(), "costsIds.%s", cost.getId());
            batch.update(docReference, field, FieldValue.delete());
        }

        List<DocumentReference> docReferences = new ArrayList<>();
        // Update all its links in other people it references!
        for (Map.Entry<String, Float> personEntry : cost.getPeopleIds().entrySet())
            docReferences.add(costDocReference.getFirestore()
                    .collection(Constants.PEOPLE_COLLECTION)
                    .document(personEntry.getKey()));
        // Update all its links in other trips it references!
        for (Map.Entry<String, Float> tripEntry : cost.getTripsIds().entrySet())
            docReferences.add(costDocReference.getFirestore()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document(tripEntry.getKey()));

        // Updates to apply to every item
        Map<String, Object> updates = new HashMap<>();
        String field = String.format(Locale.getDefault(), "costsIds.%s", cost.getId());
        updates.put(field, 0f);

        for (DocumentReference docReference : docReferences)
            batch.update(docReference, updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void createDraftCostFromRef(@Nullable DocumentReference costDocReference, OnCompleteListener<DocumentReference> onCompleteListener) {
        DocumentReference draftCostDocReference = FirebaseFirestore.getInstance().collection(Constants.COSTS_COLLECTION).document();
        if (costDocReference == null) {
            WriteBatch batch = FirebaseFirestore.getInstance().batch();
            Cost cost = new Cost(draftCostDocReference.getId());
            cost.setDraft(true);
            batch.set(draftCostDocReference, cost);
            batch.commit().addOnCompleteListener(task ->
                    Tasks.forResult(draftCostDocReference).addOnCompleteListener(onCompleteListener));
            return;
        }
        FirebaseFirestore.getInstance().runTransaction(transaction -> {
            DocumentSnapshot documentSnapshot = transaction.get(costDocReference);
            transaction.update(costDocReference, "newId", draftCostDocReference.getId());
            Cost cost = documentSnapshot.toObject(Cost.class);
            cost.setDraft(true);
            cost.setOldId(cost.getId());
            cost.setId(draftCostDocReference.getId());
            transaction.set(draftCostDocReference, cost);

            List<DocumentReference> docReferences = new ArrayList<>();
            // Update all its links in other items it references!
            for (Map.Entry<String, Float> personEntry : cost.getPeopleIds().entrySet())
                docReferences.add(FirebaseFirestore.getInstance()
                        .collection(Constants.PEOPLE_COLLECTION)
                        .document(personEntry.getKey()));
            for (Map.Entry<String, Float> tripEntry : cost.getTripsIds().entrySet())
                docReferences.add(FirebaseFirestore.getInstance()
                        .collection(Constants.TRIPS_COLLECTION)
                        .document(tripEntry.getKey()));

            // Updates to apply to every item
            Map<String, Object> updates = new HashMap<>();
            String field = String.format(Locale.getDefault(), "costsIds.%s", cost.getId());
            updates.put(field, 0f);

            for (DocumentReference docReference : docReferences)
                transaction.update(docReference, updates);

            return draftCostDocReference;
        }).addOnCompleteListener(onCompleteListener);
    }
}
