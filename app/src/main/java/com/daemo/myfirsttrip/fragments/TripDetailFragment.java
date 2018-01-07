package com.daemo.myfirsttrip.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
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
    protected Object getOrigItem(Bundle origItemBundle) {
        return null;
    }

    @Override
    void createDraftItemFromRef(Object origItem, DocumentReference itemDocReference, OnCompleteListener<DocumentReference> listener) {
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
        EditText trip_total_cost = view.findViewById(R.id.trip_total_cost);

        trip_title.setText(trip.getTitle());
        trip_subtitle.setText(trip.getSubtitle());
        trip_total_cost.setText(String.valueOf(trip.getTotalCost()));
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
    protected void setItemDetailsFromView(View view) {
        EditText tripTitle = view.findViewById(R.id.trip_title);
        String title = tripTitle.getText().toString();
        EditText tripSubtitle = view.findViewById(R.id.trip_subtitle);
        String subtitle = tripSubtitle.getText().toString();
        EditText tripTotalCost = view.findViewById(R.id.trip_total_cost);
        Float totalCost = Float.valueOf(tripTotalCost.getText().toString());

        trip.setTitle(title);
        trip.setSubtitle(subtitle);
        trip.setTotalCost(totalCost);
    }

    @Override
    protected void commitItem(OnCompleteListener<Void> listener) {
        DataTrip.commitTripBatch(trip, listener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_cost) {
            Bundle b = new Bundle();
            b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
            b.putString(Constants.EXTRA_REPLACE_FRAGMENT, CostDetailFragment.class.getName());
            Bundle bb = new Bundle();
            bb.putBoolean(Constants.EXTRA_ITEM_ADD, true);
            bb.putString(Constants.EXTRA_TRIP_ID, trip.getId());
            b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
            mListener.onFragmentInteraction(b);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected String getDetailFragmentName() {
        return TripDetailFragment.class.getName();
    }

    @Override
    protected void setViewDetails(@NonNull View view) {
        TextView trip_title = view.findViewById(R.id.trip_title);
        TextView trip_subtitle = view.findViewById(R.id.trip_subtitle);
        TextView trip_total_cost = view.findViewById(R.id.trip_total_cost);

        trip_title.setText(trip.getTitle());
        trip_subtitle.setText(trip.getSubtitle());
        trip_total_cost.setText(String.valueOf(trip.getTotalCost()));
    }

    @Override
    protected void deleteItem(OnCompleteListener<Void> listener) {
        DataTrip.deleteTripBatch(getItemId(), listener);
    }

    @Override
    protected void setItem(DocumentSnapshot documentSnapshot) {
        trip = documentSnapshot.toObject(Trip.class);
    }

    @Override
    protected int getChooseMenuItem1() {
        return R.id.choose_cost;
    }

    @Override
    protected int getChooseMenuItem2() {
        return R.id.choose_person;
    }

    @Override
    protected String getListFragmentName1() {
        return CostsListFragment.class.getName();
    }

    @Override
    protected String getListFragmentName2() {
        return PeopleListFragment.class.getName();
    }
}
