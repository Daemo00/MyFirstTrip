package com.daemo.myfirsttrip.fragments;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.daemo.myfirsttrip.R;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.database.DataTrip;
import com.daemo.myfirsttrip.models.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;


public class TripDetailFragment extends DetailFragment{

    private Trip trip;


    public TripDetailFragment() {
        // Required empty public constructor
    }

    @Override
    DocumentReference getItemRef(String itemId) {
        return DataTrip.getTripRef(itemId);
    }

    @Override
    void createDraftItemFromRef(DocumentReference itemDocReference, OnCompleteListener<DocumentReference> listener) {
        DataTrip.createDraftTripFromRef(itemDocReference, listener);
    }

    @Override
    protected String getExtraItemId() {
        return Constants.EXTRA_TRIP_ID;
    }

    @Override
    protected boolean isItemDraft() {
        return trip.isDraft();
    }

    @Override
    protected String getItemId() {
        return trip.getId();
    }

    @Override
    protected boolean isItemSet() {
        return trip != null;
    }

    @Override
    protected void setEditViewDetails(View view) {
        EditText trip_title = view.findViewById(R.id.trip_title);
        EditText trip_subtitle = view.findViewById(R.id.trip_subtitle);
        trip_title.setText(trip.getTitle());
        trip_subtitle.setText(trip.getSubtitle());
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_trip_detail;
    }

    @Override
    protected int getEditLayout() {
        return R.layout.fragment_trip_edit;
    }

    @Override
    protected int getMenuEdit() {
        return R.menu.trip_detail_edit;
    }

    @Override
    protected int getMenu() {
        return R.menu.trip_detail;
    }

    @Override
    protected int getChooseMenuItem() {
        return R.id.choose_person;
    }

    @Override
    protected int getEditMenuItem() {
        return R.id.edit_trip;
    }

    @Override
    protected int getConfirmMenuItem() {
        return R.id.confirm_trip;
    }

    @Override
    protected int getClearMenuItem() {
        return R.id.clear_trip;
    }

    @Override
    protected String getListFragmentName() {
        return PeopleListFragment.class.getName();
    }

    @Override
    protected void setItemDetails(View view) {
        EditText tripTitle = view.findViewById(R.id.trip_title);
        String title = tripTitle.getText().toString();
        EditText tripSubtitle = view.findViewById(R.id.trip_subtitle);
        String subtitle = tripSubtitle.getText().toString();
        trip.setTitle(title);
        trip.setSubtitle(subtitle);
    }

    @Override
    protected void commitItem(OnCompleteListener<Void> listener) {
        DataTrip.commitTripBatch(trip, listener);
    }

    @Override
    protected String getDetailFragmentName() {
        return TripDetailFragment.class.getName();
    }

    @Override
    protected void setViewDetails(@NonNull View view) {
        TextView trip_title = view.findViewById(R.id.trip_title);
        TextView trip_subtitle = view.findViewById(R.id.trip_subtitle);
        trip_title.setText(trip.getTitle());
        trip_subtitle.setText(trip.getSubtitle());
    }

    @Override
    protected int getListFragmentId() {
        return R.id.fragment_people_list;
    }

    @Override
    protected void deleteItem(OnCompleteListener<Void> listener) {
        DataTrip.deleteTripBatch(getItemId(), listener);
    }

    @Override
    protected void setItem(DocumentSnapshot documentSnapshot) {
        trip = documentSnapshot.toObject(Trip.class);
    }
}
