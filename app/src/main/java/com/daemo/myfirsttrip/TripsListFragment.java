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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


public class TripsListFragment extends MySuperFragment implements EventListener<DocumentSnapshot> {

    private boolean isChooseMode;
    private RecyclerView mRecyclerView;
    /**
     * When this fragment is summoned to add trips_ids to a person, this is that person
     */
    private Person orig_person;
    private ListenerRegistration listenerRegistration;
    private DocumentReference personDocReference;

    public TripsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null) return;
        if (args.containsKey(Constants.EXTRA_CHOOSE))
            isChooseMode = args.getBoolean(Constants.EXTRA_CHOOSE);
        if (args.containsKey(Constants.EXTRA_PERSON_ID)) {
            personDocReference = Data.getPerson(args.getString(Constants.EXTRA_PERSON_ID));
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
        fillListView();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (personDocReference != null && listenerRegistration == null)
            listenerRegistration = personDocReference.addSnapshotListener(this);
    }

    @Override
    public void onRefresh() {
        fillListView();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    private void fillListView() {
        FirebaseFirestore mFirestore = getMySuperActivity().mFirestore;
        Query query;
        Set<String> selected_ids = new HashSet<>();
        if (isChooseMode && orig_person != null) {
            // Get all trips and populate selected_ids
            query = mFirestore.collection(Constants.TRIPS_COLLECTION);
            selected_ids.addAll(orig_person.getTrips_ids().keySet());
        } else if (orig_person != null) {
            // Get only trips in orig_person
            query = mFirestore.collection(Constants.TRIPS_COLLECTION)
                    .whereEqualTo(String.format(Locale.getDefault(), "peopleIds.%s", orig_person.id), 1)
                    .limit(Constants.QUERY_LIMIT);
        } else {
            // Get all the trips!
            query = mFirestore.collection(Constants.TRIPS_COLLECTION)
                    .limit(Constants.QUERY_LIMIT);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new TripsAdapter(this, query, selected_ids);
        mAdapter.isChooseMode(isChooseMode);
        mRecyclerView.setAdapter(mAdapter);
        if (orig_person == null) {
            // Allow edit on the list only if we are in the main list
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mRecyclerView);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (isChooseMode)
            inflater.inflate(R.menu.trips_list_choose, menu);
        else
            inflater.inflate(R.menu.trips_list, menu);
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
            case R.id.confirm_trip:
                return confirmSelection();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean confirmSelection() {
        // Update the orig_person
        orig_person.getTrips_ids().clear();
        for (Object selected_id : mAdapter.selected_ids)
            if (selected_id instanceof String) {
                String selectedId = (String) selected_id;
                orig_person.trips_ids.put(selectedId, 1);
            }

        setRefreshing(true);
        Data.updatePersonBatch(orig_person, null, task -> {
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
    public boolean allowBackPress() {
        if (orig_person != null && orig_person.isDraft()) {
            getMySuperActivity().showOkCancelDialog("Confirm",
                    "Confirm the current modifications?",
                    (dialogInterface, i) -> confirmSelection());
            return false;
        }
        return super.allowBackPress();
    }

    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            getMySuperActivity().showToast(e.getMessage());
        }
        orig_person = documentSnapshot.toObject(Person.class);
        fillListView();
    }
}
