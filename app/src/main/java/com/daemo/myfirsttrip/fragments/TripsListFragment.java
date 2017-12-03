package com.daemo.myfirsttrip.fragments;

import com.daemo.myfirsttrip.R;
import com.daemo.myfirsttrip.adapter.TripsAdapter;
import com.daemo.myfirsttrip.common.Constants;
import com.google.firebase.firestore.Query;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class TripsListFragment extends ListFragment {


    public TripsListFragment() {
        // Required empty public constructor
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_trips_list;
    }

    @Override
    public int getList_id() {
        return R.id.list_trips;
    }

    @Override
    public String getCollection() {
        return Constants.TRIPS_COLLECTION;
    }

    @Override
    protected void generateAdapter(Query query, Set<String> selected_ids) {
        if (mAdapter == null)
            mAdapter = new TripsAdapter(this, query, selected_ids);
    }

    @Override
    protected Set<String> getItemRelatedIds() {
        if (cost != null)
            return cost.getTripsIds().keySet();
        else if (person != null)
            return person.getTripsIds().keySet();
        return new HashSet<>();
    }

    @Override
    protected void setItemRelatedIds(Map<String, Float> selectedIds) {
        if (cost != null)
            cost.setTripsIds(selectedIds);
        else if (person != null)
            person.setTripsIds(selectedIds);
    }

    @Override
    public int getMenu_choose() {
        return R.menu.trips_list_choose;
    }

    @Override
    int getMenuAll() {
        return R.menu.trips_list;
    }

    @Override
    public int getMenu_item_add() {
        return R.id.add_trip;
    }

    @Override
    protected String getDetailFragmentName() {
        return TripDetailFragment.class.getName();
    }
}
