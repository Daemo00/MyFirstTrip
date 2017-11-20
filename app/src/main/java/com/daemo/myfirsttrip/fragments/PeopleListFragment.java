package com.daemo.myfirsttrip.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.daemo.myfirsttrip.R;
import com.daemo.myfirsttrip.adapter.FirestoreAdapter;
import com.daemo.myfirsttrip.adapter.PeopleAdapter;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.database.DataTrip;
import com.daemo.myfirsttrip.models.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class PeopleListFragment extends ListFragment {

    /**
     * When this fragment is summoned to add relatedIds to a trip, this is that trip
     */
    Trip trip;

    public PeopleListFragment() {
        // Required empty public constructor
    }

    @Override
    public String getExtraItemId() {
        return Constants.EXTRA_TRIP_ID;
    }

    @Override
    public DocumentReference getDocReference(Bundle args) {
        return DataTrip.getTripRef(args.getString(getExtraItemId()));
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_people_list;
    }

    @Override
    public int getList_id() {
        return R.id.list_people;
    }

    @Override
    public String getCollection() {
        return Constants.PEOPLE_COLLECTION;
    }

    @Override
    @NonNull
    protected FirestoreAdapter generateAdapter(Query query, Set<String> selected_ids) {
        if (mAdapter == null)
            mAdapter = new PeopleAdapter(this, query, selected_ids);
        return mAdapter;
    }

    @Override
    protected Set<String> getItemRelatedIds() {
        return trip.getPeopleIds().keySet();
    }

    @Override
    protected void setItemRelatedIds(Map<String, Integer> selectedIds) {
        trip.setPeopleIds(selectedIds);
    }

    @Override
    protected void updateItem(OnCompleteListener<Void> listener) {
        DataTrip.updateTripBatch(trip, mAdapter.unselected_ids, listener);
    }

    @Override
    protected void setItem(DocumentSnapshot documentSnapshot) {
        trip = documentSnapshot.toObject(Trip.class);
    }

    @Override
    protected boolean isItemSet() {
        return trip != null;
    }

    @Override
    boolean getIsDraft() {
        return trip.isDraft();
    }

    @Override
    protected String getNestedFilter() {
        return String.format(Locale.getDefault(), "tripsIds.%s", trip.getId());
    }

    @Override
    public int getMenu_choose() {
        return R.menu.people_list_choose;
    }

    @Override
    int getMenuAll() {
        return R.menu.people_list;
    }

    @Override
    public int getMenu_item_add() {
        return R.id.add_person;
    }

    @Override
    protected String getDetailFragmentName() {
        return PersonDetailFragment.class.getName();
    }
}
