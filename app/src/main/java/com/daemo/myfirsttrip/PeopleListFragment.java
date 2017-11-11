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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


public class PeopleListFragment extends MySuperFragment implements EventListener<DocumentSnapshot> {

    private boolean isChooseMode;
    private RecyclerView mRecyclerView;
    /**
     * When this fragment is summoned to add people_ids to a trip, this is that trip
     */
    private Trip orig_trip;
    private ListenerRegistration listenerRegistration;
    private DocumentReference tripDocReference;

    public PeopleListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null) return;
        if (args.containsKey(Constants.EXTRA_CHOOSE))
            isChooseMode = args.getBoolean(Constants.EXTRA_CHOOSE);
        if (args.containsKey(Constants.EXTRA_TRIP_ID)) {
            tripDocReference = Data.getTrip(args.getString(Constants.EXTRA_TRIP_ID));
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
        fillListView();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (tripDocReference != null && listenerRegistration == null)
            listenerRegistration = tripDocReference.addSnapshotListener(this);
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
        if (isChooseMode && orig_trip != null) {
            // Get all people and populate selected_ids
            query = mFirestore.collection(Constants.PEOPLE_COLLECTION);
            selected_ids.addAll(orig_trip.getPeopleIds().keySet());
        } else if (orig_trip != null) {
            // Get only people in orig_trip
            query = mFirestore.collection(Constants.PEOPLE_COLLECTION)
                    .whereEqualTo(String.format(Locale.getDefault(), "trips_ids.%s", orig_trip.getId()), 1)
                    .limit(Constants.QUERY_LIMIT);
        } else {
            // Get all the people!
            query = mFirestore.collection(Constants.PEOPLE_COLLECTION)
                    .limit(Constants.QUERY_LIMIT);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new PeopleAdapter(this, query, selected_ids);
        mAdapter.isChooseMode(isChooseMode);
        mRecyclerView.setAdapter(mAdapter);
        if (orig_trip == null) {
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
            inflater.inflate(R.menu.people_list_choose, menu);
        else
            inflater.inflate(R.menu.people_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle b = new Bundle();
        switch (item.getItemId()) {
            case R.id.add_person:
                b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
                b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PersonDetailFragment.class.getName());
                mListener.onFragmentInteraction(b);
                return true;
            case R.id.confirm_person:
                return confirmSelection();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean confirmSelection() {
        // Update the orig_trip
        orig_trip.getPeopleIds().clear();
        for (Object selected_id : mAdapter.selected_ids)
            if (selected_id instanceof String) {
                String selectedId = (String) selected_id;
                orig_trip.getPeopleIds().put(selectedId, 1);
            }

        setRefreshing(true);
        Data.updateTripBatch(orig_trip, null, task -> {
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
        if (orig_trip != null && orig_trip.isDraft()) {
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
            return;
        }
        orig_trip = documentSnapshot.toObject(Trip.class);
        fillListView();
    }
}

