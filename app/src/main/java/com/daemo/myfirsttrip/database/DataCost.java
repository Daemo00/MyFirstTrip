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
import java.util.Set;


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

        // Delete all its links in other people it references!
        List<DocumentReference> personDocReferences = new ArrayList<>();
        for (Map.Entry<String, Integer> personEntry : cost.getPeopleIds().entrySet())
            personDocReferences.add(costDocReference.getFirestore()
                    .collection(Constants.PEOPLE_COLLECTION)
                    .document(personEntry.getKey()));

        // Update to be applied to people
        Map<String, Object> updates = new HashMap<>();
        updates.put(String.format(Locale.getDefault(), "costsIds.%s", cost.getId()), FieldValue.delete());

        for (DocumentReference personDocReference : personDocReferences)
            personDocReference.update(updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void updateCostBatch(Cost cost, Set<String> unselected_ids, OnCompleteListener<Void> onCompleteListener) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        DocumentReference costDocReference = FirebaseFirestore.getInstance()
                .collection(Constants.COSTS_COLLECTION).document(cost.getId());
        batch.set(costDocReference, cost);

        // Remove selection from deselected people
        for (String unselected_id : unselected_ids) {
            DocumentReference personDocReference = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION).document(unselected_id);
            String field = String.format(Locale.getDefault(), "costsIds.%s", cost.getId());
            batch.update(personDocReference, field, FieldValue.delete());
        }

        // Update all its links in other people it references!
        List<DocumentReference> personDocReferences = new ArrayList<>();
        for (Map.Entry<String, Integer> personEntry : cost.getPeopleIds().entrySet()) {
            DocumentReference documentReference = FirebaseFirestore.getInstance()
                    .collection(Constants.PEOPLE_COLLECTION)
                    .document(personEntry.getKey());
            personDocReferences.add(documentReference);
        }

        // Updates to apply to every person
        Map<String, Object> updates = new HashMap<>();
        String field = String.format(Locale.getDefault(), "costsIds.%s", cost.getId());
        updates.put(field, 1);

        for (DocumentReference personDocReference : personDocReferences)
            batch.update(personDocReference, updates);

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

            // Update all its links in other people it references!
            List<DocumentReference> personDocReferences = new ArrayList<>();
            for (Map.Entry<String, Integer> personEntry : cost.getPeopleIds().entrySet())
                personDocReferences.add(FirebaseFirestore.getInstance()
                        .collection(Constants.PEOPLE_COLLECTION)
                        .document(personEntry.getKey()));

            // Updates to apply to every person
            Map<String, Object> updates = new HashMap<>();
            String field = String.format(Locale.getDefault(), "costsIds.%s", cost.getId());
            updates.put(field, 1);

            for (DocumentReference personDocReference : personDocReferences)
                transaction.update(personDocReference, updates);

            return draftCostDocReference;
        }).addOnCompleteListener(onCompleteListener);
    }
}
