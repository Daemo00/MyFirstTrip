package com.daemo.myfirsttrip.fragments;

import com.daemo.myfirsttrip.R;
import com.daemo.myfirsttrip.adapter.PeopleAdapter;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.models.Cost;
import com.daemo.myfirsttrip.models.Trip;
import com.google.firebase.firestore.Query;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class PeopleListFragment extends ListFragment {

    /**
     * When this fragment is summoned to add relatedIds to a trip, this is that trip
     */
    Cost cost;
    Trip trip;

    public PeopleListFragment() {
        // Required empty public constructor
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
    protected void generateAdapter(Query query, Set<String> selected_ids) {
        if (mAdapter == null)
            mAdapter = new PeopleAdapter(this, query, selected_ids);
    }

    @Override
    protected Set<String> getItemRelatedIds() {
        if (cost != null)
            return cost.getPeopleIds().keySet();
        else if (trip != null)
            return trip.getPeopleIds().keySet();
        return new HashSet<>();
    }

    @Override
    protected void setItemRelatedIds(Map<String, Integer> selectedIds) {
        if (cost != null)
            cost.setPeopleIds(selectedIds);
        else if (trip != null)
            trip.setPeopleIds(selectedIds);
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
