package com.daemo.myfirsttrip.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.daemo.myfirsttrip.MyRefreshing;
import com.daemo.myfirsttrip.R;
import com.daemo.myfirsttrip.adapter.FirestoreAdapter;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.common.SimpleItemTouchHelperCallback;
import com.daemo.myfirsttrip.database.DataCost;
import com.daemo.myfirsttrip.database.DataPerson;
import com.daemo.myfirsttrip.database.DataTrip;
import com.daemo.myfirsttrip.models.Cost;
import com.daemo.myfirsttrip.models.Person;
import com.daemo.myfirsttrip.models.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public abstract class ListFragment extends MySuperFragment implements EventListener<DocumentSnapshot> {

    public FirestoreAdapter mAdapter;
    /**
     * When this fragment is summoned to add relatedIds to a cost/person/trip, this is that cost/person/trip
     */
    Cost cost;
    Person person;
    Trip trip;
    private DocumentReference docReference;
    private RecyclerView mRecyclerView;
    private ListenerRegistration listenerRegistration;
    private ListFragmentMode currStatus;
    private boolean unique = false;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null || args.isEmpty()) {
            currStatus = ListFragmentMode.ALL;
            needsRefreshLayout = true;
        } else if (hasExtraItemId(args)) {
            docReference = getDocReference(args);
            currStatus = ListFragmentMode.NESTED;
            needsRefreshLayout = false;
            if (args.containsKey(Constants.EXTRA_EDIT) && args.getBoolean(Constants.EXTRA_EDIT)) {
                currStatus = ListFragmentMode.NESTED_EDIT;
                needsRefreshLayout = false;
            } else if (args.containsKey(Constants.EXTRA_CHOOSE) && args.getBoolean(Constants.EXTRA_CHOOSE)) {
                currStatus = ListFragmentMode.CHOOSE;
                if (args.containsKey(Constants.EXTRA_UNIQUE) && args.getBoolean(Constants.EXTRA_UNIQUE))
                    unique = args.getBoolean(Constants.EXTRA_UNIQUE);
                needsRefreshLayout = true;
            }
        }
    }

    protected boolean hasExtraItemId(Bundle args) {
        return args.containsKey(Constants.EXTRA_COST_ID)
                || args.containsKey(Constants.EXTRA_PERSON_ID)
                || args.containsKey(Constants.EXTRA_TRIP_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = inflater.inflate(getLayout(), container, false);
        if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            viewGroup.addView(root);
            return viewGroup;
        }

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(getList_id());
        switch (currStatus) {
            case CHOOSE:
            case NESTED:
            case NESTED_EDIT:
                // List will be updated when docReference is resolved
                break;
            case ALL:
                fillListView();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        switch (currStatus) {
            case CHOOSE:
            case NESTED:
            case NESTED_EDIT:
                if (listenerRegistration == null && docReference!= null)
                    listenerRegistration = docReference.addSnapshotListener(this);
                break;
            case ALL:
                break;
        }
    }

    @Override
    public void onRefresh() {
        if (mAdapter != null)
            mAdapter.setQuery(mAdapter.mQuery);
    }

    @Override
    public void onStop() {
        super.onStop();
        switch (currStatus) {
            case CHOOSE:
            case NESTED:
            case NESTED_EDIT:
                if (listenerRegistration != null) {
                    listenerRegistration.remove();
                    listenerRegistration = null;
                }
                break;
            case ALL:
                break;
        }
    }

    private void fillListView() {
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        Query query;
        Set<String> selected_ids = new HashSet<>();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        query = mFirestore.collection(getCollection())
                .limit(Constants.QUERY_LIMIT);
        switch (currStatus) {
            case CHOOSE:
                if (isItemSet()) {
                    selected_ids.addAll(getItemRelatedIds());
                    generateAdapter(query, selected_ids);
                }
                break;
            case NESTED:
            case NESTED_EDIT:
                if (isItemSet()) {
                    // Get only items in origItem
                    query = getNestedFilter(query);
                    generateAdapter(query, selected_ids);
                    mAdapter.setMyRefreshing((MyRefreshing) getParentFragment());
                }
                break;
            case ALL:
                generateAdapter(query, selected_ids);
                // Allow edit on the list only if we are in the main list
                ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
                ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                touchHelper.attachToRecyclerView(mRecyclerView);
                break;
        }
        mAdapter.setUnique(unique);
        mAdapter.setChooseMode(currStatus.equals(ListFragmentMode.CHOOSE));
        mAdapter.setClickable(!currStatus.equals(ListFragmentMode.NESTED_EDIT));
        mAdapter.startListening();
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        switch (currStatus) {
            case CHOOSE:
                inflater.inflate(getMenu_choose(), menu);
                break;
            case NESTED:
            case NESTED_EDIT:
                break;
            case ALL:
                inflater.inflate(getMenuAll(), menu);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == getMenu_item_add()) {
            Bundle b = new Bundle();
            b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
            b.putString(Constants.EXTRA_REPLACE_FRAGMENT, getDetailFragmentName());
            mListener.onFragmentInteraction(b);
            return true;
        } else if (itemId == R.id.confirm_selection) {
            confirmSelection();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmSelection() {
        // Update the origItem with the selected items
        if (!getIsDraft())
            getMySuperActivity().showToast("Something went wrong, this should be a draft");
        Map<String, Float> relatedIds = new HashMap<>();
        for (Object selectedId : mAdapter.selectedIds)
            relatedIds.put((String) selectedId, 0f);
        setItemRelatedIds(relatedIds);

        setRefreshing(true);

        updateItem(task -> {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null)
                // Go back to whoever called this
                fragmentManager.popBackStack();
            else
                getMySuperActivity().showToast("Fragment manager not found");
            setRefreshing(false);
        });
    }

    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            getMySuperActivity().showToast(e.getMessage());
            return;
        }
        setItem(documentSnapshot);
        fillListView();
    }

    protected abstract Set<String> getItemRelatedIds();

    protected abstract void setItemRelatedIds(Map<String, Float> selectedIds);

    protected abstract String getDetailFragmentName();

    protected void updateItem(OnCompleteListener<Void> listener) {
        List<DocumentReference> unselectedReferences = new ArrayList<>(mAdapter.unselectedIds.size());
        for (Object unselected_id : mAdapter.unselectedIds)
            unselectedReferences.add(FirebaseFirestore.getInstance().collection(mAdapter.collection)
                    .document((String) unselected_id));

        if (cost != null)
            DataCost.updateCostBatch(cost, unselectedReferences, listener);
        else if (person != null)
            DataPerson.updatePersonBatch(person, unselectedReferences, listener);
        else if (trip != null)
            DataTrip.updateTripBatch(trip, unselectedReferences, listener);
    }

    protected void setItem(DocumentSnapshot documentSnapshot) {
        String collection = documentSnapshot.getReference().getParent().getId();
        switch (collection) {
            case Constants.COSTS_COLLECTION:
                cost = documentSnapshot.toObject(Cost.class);
                break;
            case Constants.PEOPLE_COLLECTION:
                person = documentSnapshot.toObject(Person.class);
                break;
            case Constants.TRIPS_COLLECTION:
                trip = documentSnapshot.toObject(Trip.class);
                break;
        }
    }

    public String getExtraItemId(Bundle args) {
        if (args.containsKey(Constants.EXTRA_COST_ID))
            return args.getString(Constants.EXTRA_COST_ID);
        else if (args.containsKey(Constants.EXTRA_PERSON_ID))
            return args.getString(Constants.EXTRA_PERSON_ID);
        else if (args.containsKey(Constants.EXTRA_TRIP_ID))
            return args.getString(Constants.EXTRA_TRIP_ID);
        return null;
    }

    public DocumentReference getDocReference(Bundle args) {
        if (args.containsKey(Constants.EXTRA_COST_ID))
            return DataCost.getCostRef(getExtraItemId(args));
        else if (args.containsKey(Constants.EXTRA_PERSON_ID))
            return DataPerson.getPersonRef(getExtraItemId(args));
        else if (args.containsKey(Constants.EXTRA_TRIP_ID))
            return DataTrip.getTripRef(getExtraItemId(args));
        return null;
    }

    protected abstract int getLayout();

    protected abstract int getList_id();

    protected abstract String getCollection();

    protected abstract void generateAdapter(Query query, Set<String> selected_ids);

    protected boolean isItemSet() {
        return cost != null
                || person != null
                || trip != null;
    }

    boolean getIsDraft() {
        if (cost != null)
            return cost.isDraft();
        else if (person != null)
            return person.isDraft();
        else if (trip != null)
            return trip.isDraft();
        return false;
    }

    protected Query getNestedFilter(Query query) {
        if (cost != null)
            if (getCollection().equals(Constants.TRIPS_COLLECTION) && !cost.getTripId().isEmpty())
                // In this case, we are in cost details, and this is the list of trips
                return query.whereEqualTo("id", cost.getTripId());
            else
                return query.whereGreaterThanOrEqualTo(String.format(Locale.getDefault(), "costsIds.%s", cost.getId()), 0f);
        else if (person != null)
            return query.whereGreaterThanOrEqualTo(String.format(Locale.getDefault(), "peopleIds.%s", person.getId()), 0f);
        else if (trip != null)
            if (getCollection().equals(Constants.COSTS_COLLECTION))
                // In this case, we are in trip details, and this is the list of costs
                return query.whereEqualTo("tripId", trip.getId());
            else
                return query.whereGreaterThanOrEqualTo(String.format(Locale.getDefault(), "tripsIds.%s", trip.getId()), 0f);
        return query;
    }

    protected abstract int getMenu_choose();

    abstract int getMenuAll();

    protected abstract int getMenu_item_add();
}
