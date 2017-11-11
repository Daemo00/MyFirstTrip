package com.daemo.myfirsttrip;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.database.Data;
import com.daemo.myfirsttrip.models.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;


public class TripDetailFragment extends MySuperFragment implements EventListener<DocumentSnapshot>, OnCompleteListener<Void> {
    private boolean isEditMode;
    private boolean isNewMode;
    private DocumentReference tripRef;
    private ListenerRegistration listenerRegistration;
    private Trip trip;


    public TripDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            // From trips_ids list, add a trip
            isNewMode = true;
        } else if (args.containsKey(Constants.EXTRA_TRIP_ID)) {
            // Click on a trip to see details
            String tripId = args.getString(Constants.EXTRA_TRIP_ID);
            tripRef = Data.getTrip(tripId);
            if (args.containsKey(Constants.EXTRA_EDIT)) {
                // Click on a trip for edit
                // Make a copy and work on it, in case user doesn't confirm
                isEditMode = true;
                tripRef = Data.getTrip(tripId);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = isNewMode || isEditMode ?
                inflater.inflate(R.layout.fragment_trip_edit, container, false) :
                inflater.inflate(R.layout.fragment_trip_detail, container, false);
        if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            viewGroup.addView(root);
            return viewGroup;
        }

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isNewMode)
            listenerRegistration = tripRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Also add the possibility to add an existing person
        if (isNewMode || isEditMode)
            inflater.inflate(R.menu.trip_detail_edit, menu);
        else
            inflater.inflate(R.menu.trip_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_trip:
                cleanData();
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null)
                    fragmentManager.popBackStack();
                break;
            case R.id.confirm_trip:
                return confirmTrip();
            case R.id.edit_trip:
                if (trip.isDraft())
                    getMySuperActivity().showOkCancelDialog("Confirm",
                            "Confirm the current modifications before choosing other people",
                            (dialogInterface, i) -> confirmTrip());
                else
                    editTrip();
                return true;
            case R.id.choose_person:
                choosePerson();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void choosePerson() {
        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putBoolean(Constants.EXTRA_CHOOSE, true);
        bb.putString(Constants.EXTRA_TRIP_ID, trip.getId());
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PeopleListFragment.class.getName());
        mListener.onFragmentInteraction(b);
    }

    @Override
    public boolean allowBackPress() {
        if (trip.isDraft()) {
            getMySuperActivity().showOkCancelDialog("Confirm",
                    "Confirm the current modifications?",
                    (dialogInterface, i) -> confirmTrip());
            return false;
        }
        return super.allowBackPress();
    }

    private boolean confirmTrip() {
        // TODO validation
        View root = getView();
        if (root != null) {
            // TODO this doesn't update the server, so actually in the commit you confirm the trip on the server (so nothing changes)
            trip.setTitle(((EditText) root.findViewById(R.id.trip_title)).getText().toString());
            trip.setSubtitle(((EditText) root.findViewById(R.id.trip_subtitle)).getText().toString());
        }
        setRefreshing(true);
        Data.commitTripBatch(trip, null, task -> setRefreshing(false));
        // needed, otherwise this fragment isn't correctly removed
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.popBackStack();
            fragmentManager.popBackStack();
        }

        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putString(Constants.EXTRA_TRIP_ID, trip.getId());
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, TripDetailFragment.class.getName());
        mListener.onFragmentInteraction(b);
        return true;
    }

    private void editTrip() {
        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putString(Constants.EXTRA_TRIP_ID, trip.getId());
        bb.putBoolean(Constants.EXTRA_EDIT, true);
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, TripDetailFragment.class.getName());
        mListener.onFragmentInteraction(b);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isNewMode) {
            String newId = FirebaseFirestore.getInstance().collection(Constants.TRIPS_COLLECTION).document().getId();
            trip = new Trip(newId, null, null);
        } else {
            setRefreshing(true);
        }
    }

    private void loadEditLayoutTrip(@Nullable View view) {
        if (view == null) return;
        EditText trip_title = view.findViewById(R.id.trip_title);
        trip_title.setText(trip.getTitle());
        EditText trip_subtitle = view.findViewById(R.id.trip_subtitle);
        trip_subtitle.setText(trip.getSubtitle());
        // TODO insert the list fragment
//        RecyclerView people_joined = view.findViewById(R.id.list_people);
//        PeopleListFragment.fillListView(this, people_joined, trip, null, false);
    }

    private void loadLayoutTrip(@Nullable View view) {
        if (view == null) return;
        TextView trip_title = view.findViewById(R.id.trip_title);
        trip_title.setText(trip.getTitle());
        TextView trip_subtitle = view.findViewById(R.id.trip_subtitle);
        trip_subtitle.setText(trip.getSubtitle());
        // TODO insert the list fragment
//        RecyclerView people_joined = view.findViewById(R.id.list_people);
//        PeopleListFragment.fillListView(this, people_joined, trip, null, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanData();
    }

    private void cleanData() {
        if (trip.isDraft()) {
            Data.deleteTripBatch(trip.getId(), null, this);
        }
    }

    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            getMySuperActivity().showToast(e.getMessage());
            return;
        }

        if (!documentSnapshot.exists())
            return;
        trip = documentSnapshot.toObject(Trip.class);
        String newId = FirebaseFirestore.getInstance().collection(Constants.TRIPS_COLLECTION).document().getId();

        if (isNewMode) {
            trip.setId(newId);
        } else if (isEditMode) {
            // Work on a copy, in case user doesn't confirm
            trip = Data.createDraftTripBatch(trip, null, task -> {
            });
            loadEditLayoutTrip(getView());
        } else {
            loadLayoutTrip(getView());
        }
        setRefreshing(false);
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.getException() != null)
            getMySuperActivity().showToast(task.getException().getMessage());
        setRefreshing(false);
    }
}
