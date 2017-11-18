package com.daemo.myfirsttrip;

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

import com.daemo.myfirsttrip.adapter.PeopleAdapter;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.common.SimpleItemTouchHelperCallback;
import com.daemo.myfirsttrip.database.Data;
import com.daemo.myfirsttrip.models.Trip;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class PeopleListFragment extends MySuperFragment implements EventListener<DocumentSnapshot> {

    private RecyclerView mRecyclerView;
    /**
     * When this fragment is summoned to add peopleIds to a trip, this is that trip
     */
    private Trip orig_trip;
    private ListenerRegistration listenerRegistration;
    private DocumentReference tripDocReference;
    private ListFragmentMode currStatus;

    public PeopleListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null || args.isEmpty()) {
            currStatus = ListFragmentMode.ALL;
            needsRefreshLayout = true;
        } else if (args.containsKey(Constants.EXTRA_TRIP_ID)) {
            tripDocReference = Data.getTripRef(args.getString(Constants.EXTRA_TRIP_ID));
            currStatus = ListFragmentMode.NESTED;
            needsRefreshLayout = false;
            if (args.containsKey(Constants.EXTRA_EDIT) && args.getBoolean(Constants.EXTRA_EDIT)) {
                currStatus = ListFragmentMode.NESTED_EDIT;
                needsRefreshLayout = false;
            } else if (args.containsKey(Constants.EXTRA_CHOOSE) && args.getBoolean(Constants.EXTRA_CHOOSE)) {
                currStatus = ListFragmentMode.CHOOSE;
                needsRefreshLayout = true;
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_people_list, container, false);
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
        mRecyclerView = view.findViewById(R.id.list_people);
        switch (currStatus) {
            case CHOOSE:
            case NESTED:
            case NESTED_EDIT:
                // List will be updated when personDocReference is resolved
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
                if (listenerRegistration == null)
                    listenerRegistration = tripDocReference.addSnapshotListener(this);
                break;
            case ALL:
                break;
        }
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
        FirebaseFirestore mFirestore = getMySuperActivity().mFirestore;
        Query query;
        Set<String> selected_ids = new HashSet<>();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        switch (currStatus) {
            case CHOOSE:
                if (orig_trip != null) {
                    // Get all trips and populate selected_ids
                    query = mFirestore.collection(Constants.PEOPLE_COLLECTION)
                            .limit(Constants.QUERY_LIMIT);
                    selected_ids.addAll(orig_trip.getPeopleIds().keySet());
                    mAdapter = new PeopleAdapter(this, query, selected_ids);
                }
                break;
            case NESTED:
            case NESTED_EDIT:
                if (orig_trip != null) {
                    // Get only trips in orig_person
                    query = mFirestore.collection(Constants.PEOPLE_COLLECTION)
                            .whereEqualTo(String.format(Locale.getDefault(), "tripsIds.%s", orig_trip.getId()), 1)
                            .limit(Constants.QUERY_LIMIT);
                    mAdapter = new PeopleAdapter(this, query, selected_ids);
                    mAdapter.setMyRefreshing((MyRefreshing) getParentFragment());
                }
                break;
            case ALL:
                // Get all the trips!
                query = mFirestore.collection(Constants.PEOPLE_COLLECTION)
                        .limit(Constants.QUERY_LIMIT);
                mAdapter = new PeopleAdapter(this, query, selected_ids);
                // Allow edit on the list only if we are in the main list
                ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
                ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                touchHelper.attachToRecyclerView(mRecyclerView);
                break;
        }
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
                inflater.inflate(R.menu.people_list_choose, menu);
                break;
            case NESTED:
            case NESTED_EDIT:
                break;
            case ALL:
                inflater.inflate(R.menu.people_list, menu);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_person:
                Bundle b = new Bundle();
                b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
                b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PersonDetailFragment.class.getName());
                mListener.onFragmentInteraction(b);
                return true;
            case R.id.confirm_selection:
                confirmSelection();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmSelection() {
        // Update the orig_trip with the selected trips
        if (!orig_trip.isDraft())
            getMySuperActivity().showToast("Something went wrong, this should be a draft");
        Map<String, Integer> selected_peopleIds = new HashMap<>();
        for (Object selected_id : mAdapter.selected_ids)
            if (selected_id instanceof String) {
                String selectedId = (String) selected_id;
                selected_peopleIds.put(selectedId, 1);
            }
        orig_trip.setPeopleIds(selected_peopleIds);

        setRefreshing(true);
        Data.updateTripBatch(orig_trip, mAdapter.unselected_ids, task -> {
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
        orig_trip = documentSnapshot.toObject(Trip.class);
        fillListView();
    }
}
