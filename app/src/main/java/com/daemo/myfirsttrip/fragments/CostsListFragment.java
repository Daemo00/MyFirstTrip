package com.daemo.myfirsttrip.fragments;

import android.support.annotation.NonNull;

import com.daemo.myfirsttrip.R;
import com.daemo.myfirsttrip.adapter.CostsAdapter;
import com.daemo.myfirsttrip.adapter.FirestoreAdapter;
import com.daemo.myfirsttrip.common.Constants;
import com.google.firebase.firestore.Query;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class CostsListFragment extends ListFragment {


    public CostsListFragment() {
        // Required empty public constructor
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_costs_list;
    }

    @Override
    public int getList_id() {
        return R.id.list_costs;
    }

    @Override
    public String getCollection() {
        return Constants.COSTS_COLLECTION;
    }

    @Override
    @NonNull
    protected FirestoreAdapter generateAdapter(Query query, Set<String> selected_ids) {
        if (mAdapter == null)
            mAdapter = new CostsAdapter(this, query, selected_ids);
        return mAdapter;
    }

    @Override
    protected Set<String> getItemRelatedIds() {
        if (person != null)
            return person.getCostsIds().keySet();
        else if (trip != null)
            return trip.getCostsIds().keySet();
        return new HashSet<>();
    }

    @Override
    protected void setItemRelatedIds(Map<String, Integer> selectedIds) {
        if (person != null)
            person.setCostsIds(selectedIds);
        else if (trip != null)
            trip.setCostsIds(selectedIds);
    }

    @Override
    public int getMenu_choose() {
        return R.menu.costs_list_choose;
    }

    @Override
    int getMenuAll() {
        return R.menu.costs_list;
    }

    @Override
    public int getMenu_item_add() {
        return R.id.add_cost;
    }

    @Override
    protected String getDetailFragmentName() {
        return CostDetailFragment.class.getName();
    }
}
