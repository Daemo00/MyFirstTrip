package com.daemo.myfirsttrip.fragments;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.daemo.myfirsttrip.R;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.database.DataPerson;
import com.daemo.myfirsttrip.models.Person;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;


public class PersonDetailFragment extends DetailFragment {

    private Person person;


    public PersonDetailFragment() {
        // Required empty public constructor
    }

    @Override
    protected DocumentReference getItemRef(String itemId) {
        return DataPerson.getPersonRef(itemId);
    }

    @Override
    protected void createDraftItemFromRef(DocumentReference itemDocReference, OnCompleteListener<DocumentReference> listener) {
        DataPerson.createDraftPersonFromRef(itemDocReference, listener);
    }

    @Override
    protected String getExtraItemId()  {
        return Constants.EXTRA_PERSON_ID;
    }

    @Override
    protected boolean isItemDraft() {
        return person.isDraft();
    }

    @Override
    protected String getItemId() {
        return person.getId();
    }

    @Override
    protected boolean isItemSet() {
        return person != null;
    }

    @Override
    protected void setEditViewDetails(View view) {
        EditText personName = view.findViewById(R.id.person_name);
        EditText personSurname = view.findViewById(R.id.person_surname);
        personName.setText(person.getName());
        personSurname.setText(person.getSurname());
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_person_detail;
    }

    @Override
    protected int getEditLayout() {
        return R.layout.fragment_person_edit;
    }

    @Override
    protected int getMenuEdit() {
        return R.menu.person_detail_edit;
    }

    @Override
    protected int getMenu() {
        return R.menu.person_detail;
    }

    @Override
    protected int getChooseMenuItem() {
        return R.id.choose_trip;
    }

    @Override
    protected int getEditMenuItem() {
        return R.id.edit_person;
    }

    @Override
    protected int getConfirmMenuItem() {
        return R.id.confirm_person;
    }

    @Override
    protected int getClearMenuItem() {
        return R.id.clear_person;
    }

    @Override
    protected String getListFragmentName() {
        return TripsListFragment.class.getName();
    }

    @Override
    protected void setItemDetails(View view) {
        TextView personName = view.findViewById(R.id.person_name);
        TextView personSurname = view.findViewById(R.id.person_surname);
        person.setName(personName.getText().toString());
        person.setSurname(personSurname.getText().toString());
    }

    @Override
    protected void commitItem(OnCompleteListener<Void> listener) {
        DataPerson.commitPersonBatch(person, listener);
    }

    @Override
    protected String getDetailFragmentName() {
        return PersonDetailFragment.class.getName();
    }

    @Override
    protected void setViewDetails(@NonNull View view) {
        TextView personName = view.findViewById(R.id.person_name);
        TextView personSurname = view.findViewById(R.id.person_surname);
        personName.setText(person.getName());
        personSurname.setText(person.getSurname());
    }

    @Override
    protected int getListFragmentId() {
        return R.id.fragment_trips_list;
    }

    @Override
    protected void deleteItem(OnCompleteListener<Void> listener) {
        DataPerson.deletePersonBatch(getItemId(), listener);
    }

    @Override
    protected void setItem(DocumentSnapshot documentSnapshot) {
        person = documentSnapshot.toObject(Person.class);
    }

}