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

import com.daemo.myfirsttrip.adapter.TripsAdapter;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.common.SimpleItemTouchHelperCallback;
import com.daemo.myfirsttrip.database.Data;
import com.daemo.myfirsttrip.models.Person;
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


public class TripsListFragment extends MySuperFragment implements EventListener<DocumentSnapshot> {

    private RecyclerView mRecyclerView;
    /**
     * When this fragment is summoned to add tripsIds to a person, this is that person
     */
    private Person orig_person;
    private ListenerRegistration listenerRegistration;
    private DocumentReference personDocReference;
    private ListFragmentMode currStatus;

    public TripsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null || args.isEmpty()) {
            currStatus = ListFragmentMode.ALL;
        } else if (args.containsKey(Constants.EXTRA_PERSON_ID)) {
            personDocReference = Data.getPersonRef(args.getString(Constants.EXTRA_PERSON_ID));
            currStatus = ListFragmentMode.NESTED;
            if (args.containsKey(Constants.EXTRA_CHOOSE) && args.getBoolean(Constants.EXTRA_CHOOSE))
                currStatus = ListFragmentMode.CHOOSE;
            if (args.containsKey(Constants.EXTRA_EDIT) && args.getBoolean(Constants.EXTRA_EDIT))
                currStatus = ListFragmentMode.NESTED_EDIT;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_trips_list, container, false);
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
        mRecyclerView = view.findViewById(R.id.list_trips);
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
                    listenerRegistration = personDocReference.addSnapshotListener(this);
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
        Query query = null;
        Set<String> selected_ids = new HashSet<>();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        switch (currStatus) {
            case CHOOSE:
                if (orig_person != null) {
                    // Get all trips and populate selected_ids
                    query = mFirestore.collection(Constants.TRIPS_COLLECTION)
                            .limit(Constants.QUERY_LIMIT);
                    selected_ids.addAll(orig_person.getTripsIds().keySet());
                    mAdapter = new TripsAdapter(this, query, selected_ids);
                }
                break;
            case NESTED:
            case NESTED_EDIT:
                if (orig_person != null) {
                    // Get only trips in orig_person
                    query = mFirestore.collection(Constants.TRIPS_COLLECTION)
                            .whereEqualTo(String.format(Locale.getDefault(), "peopleIds.%s", orig_person.getId()), 1)
                            .limit(Constants.QUERY_LIMIT);
                    mAdapter = new TripsAdapter(this, query, selected_ids);
                }
                break;
            case ALL:
                // Get all the trips!
                query = mFirestore.collection(Constants.TRIPS_COLLECTION)
                        .limit(Constants.QUERY_LIMIT);
                mAdapter = new TripsAdapter(this, query, selected_ids);
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
                inflater.inflate(R.menu.trips_list_choose, menu);
                break;
            case NESTED:
            case NESTED_EDIT:
                break;
            case ALL:
                inflater.inflate(R.menu.trips_list, menu);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_trip:
                Bundle b = new Bundle();
                b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
                b.putString(Constants.EXTRA_REPLACE_FRAGMENT, TripDetailFragment.class.getName());
                mListener.onFragmentInteraction(b);
                return true;
            case R.id.confirm_selection:
                return confirmSelection();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean confirmSelection() {
        // Update the orig_person with the selected trips
        if (!orig_person.isDraft())
            getMySuperActivity().showToast("Something went wrong, this should be a draft");
        Map<String, Integer> selected_tripsIds = new HashMap<>();
        for (Object selected_id : mAdapter.selected_ids)
            if (selected_id instanceof String) {
                String selectedId = (String) selected_id;
                selected_tripsIds.put(selectedId, 1);
            }
        orig_person.setTripsIds(selected_tripsIds);

        setRefreshing(true);
        Data.updatePersonBatch(orig_person, mAdapter.unselected_ids, null, task -> {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null)
                // Go back to whoever called this
                fragmentManager.popBackStack();
            else
                getMySuperActivity().showToast("Fragment manager not found");
            setRefreshing(false);
        });
        return true;
    }

    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            getMySuperActivity().showToast(e.getMessage());
            return;
        }
        orig_person = documentSnapshot.toObject(Person.class);
        fillListView();
    }
}
