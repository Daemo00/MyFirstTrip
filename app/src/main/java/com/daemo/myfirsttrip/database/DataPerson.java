package com.daemo.myfirsttrip.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.models.Person;
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


public class DataPerson {

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
            batch = personDocReference.getFirestore().batch();
        // Delete the person
        batch.delete(personDocReference);

        List<DocumentReference> docReferences = new ArrayList<>();
        // Delete all its links in other trips it references!
        for (Map.Entry<String, Float> costEntry : person.getCostsIds().entrySet())
            docReferences.add(personDocReference.getFirestore()
                    .collection(Constants.COSTS_COLLECTION)
                    .document(costEntry.getKey()));

        // Delete all its links in other trips it references!
        for (Map.Entry<String, Float> tripEntry : person.getTripsIds().entrySet())
            docReferences.add(personDocReference.getFirestore()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document(tripEntry.getKey()));

        // Update to be applied to costs and trips
        Map<String, Object> updates = new HashMap<>();
        updates.put(String.format(Locale.getDefault(), "peopleIds.%s", person.getId()), FieldValue.delete());

        for (DocumentReference docReference : docReferences)
            batch.update(docReference, updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void updatePersonBatch(Person person, List<DocumentReference> unselected_ids, OnCompleteListener<Void> onCompleteListener) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        DocumentReference personDocReference = FirebaseFirestore.getInstance()
                .collection(Constants.PEOPLE_COLLECTION).document(person.getId());
        batch.set(personDocReference, person);

        // Remove selection from deselected people
        for (DocumentReference docReference : unselected_ids) {
            String field = String.format(Locale.getDefault(), "peopleIds.%s", person.getId());
            batch.update(docReference, field, FieldValue.delete());
        }

        List<DocumentReference> docReferences = new ArrayList<>();
        // Update all its links in other costs it references!
        for (Map.Entry<String, Float> costEntry : person.getCostsIds().entrySet())
            docReferences.add(personDocReference.getFirestore()
                    .collection(Constants.COSTS_COLLECTION)
                    .document(costEntry.getKey()));
        // Update all its links in other trips it references!
        for (Map.Entry<String, Float> tripEntry : person.getTripsIds().entrySet())
            docReferences.add(personDocReference.getFirestore()
                    .collection(Constants.TRIPS_COLLECTION)
                    .document(tripEntry.getKey()));

        // Updates to apply to every item
        Map<String, Object> updates = new HashMap<>();
        String field = String.format(Locale.getDefault(), "peopleIds.%s", person.getId());
        updates.put(field, 0f);

        for (DocumentReference docReference : docReferences)
            batch.update(docReference, updates);

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    public static void createDraftPersonFromRef(@Nullable DocumentReference personDocReference, OnCompleteListener<DocumentReference> onCompleteListener) {
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

            List<DocumentReference> docReferences = new ArrayList<>();
            // Update all its links in other items it references!
            for (Map.Entry<String, Float> costEntry : person.getCostsIds().entrySet())
                docReferences.add(FirebaseFirestore.getInstance()
                        .collection(Constants.COSTS_COLLECTION)
                        .document(costEntry.getKey()));
            for (Map.Entry<String, Float> tripEntry : person.getTripsIds().entrySet())
                docReferences.add(FirebaseFirestore.getInstance()
                        .collection(Constants.TRIPS_COLLECTION)
                        .document(tripEntry.getKey()));

            // Updates to apply to every item
            Map<String, Object> updates = new HashMap<>();
            String field = String.format(Locale.getDefault(), "peopleIds.%s", person.getId());
            updates.put(field, 0f);

            for (DocumentReference docReference : docReferences)
                transaction.update(docReference, updates);

            return draftPersonDocReference;
        }).addOnCompleteListener(onCompleteListener);
    }
}
