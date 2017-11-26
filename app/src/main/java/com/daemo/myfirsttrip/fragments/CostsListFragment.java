package com.daemo.myfirsttrip.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.daemo.myfirsttrip.R;
import com.daemo.myfirsttrip.adapter.CostsAdapter;
import com.daemo.myfirsttrip.adapter.FirestoreAdapter;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.database.DataPerson;
import com.daemo.myfirsttrip.models.Person;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class CostsListFragment extends ListFragment {

    /**
     * When this fragment is summoned to add relatedIds to a person, this is that person
     */
    Person person;

    public CostsListFragment() {
        // Required empty public constructor
    }

    @Override
    public String getExtraItemId() {
        return Constants.EXTRA_PERSON_ID;
    }

    @Override
    public DocumentReference getDocReference(Bundle args) {
        return DataPerson.getPersonRef(args.getString(getExtraItemId()));
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
    protected boolean isItemSet() {
        return person != null;
    }

    @Override
    protected Set<String> getItemRelatedIds() {
        return person.getCostsIds().keySet();
    }

    @Override
    protected void setItemRelatedIds(Map<String, Integer> selectedIds) {
        person.setCostsIds(selectedIds);
    }

    @Override
    protected void updateItem(OnCompleteListener<Void> listener) {
        DataPerson.updatePersonBatch(person, mAdapter.unselected_ids, listener);
    }

    @Override
    protected void setItem(DocumentSnapshot documentSnapshot) {
        person = documentSnapshot.toObject(Person.class);
    }

    @Override
    boolean getIsDraft() {
        return person.isDraft();
    }

    @Override
    protected String getNestedFilter() {
        return String.format(Locale.getDefault(), "peopleIds.%s", person.getId());
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
