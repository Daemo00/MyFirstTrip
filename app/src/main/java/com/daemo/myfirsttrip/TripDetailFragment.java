package com.daemo.myfirsttrip;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
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


public class TripDetailFragment extends MySuperFragment {
    private Trip trip;
    private boolean isEditMode;
    private boolean isNewMode;


    public TripDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            // From trips list, add a trip
            trip = Data.getTripDraft();
            isNewMode = true;
        } else if (args.containsKey(Constants.EXTRA_TRIP_ID)) {
            // Click on a trip to see details
            trip = Data.getTrip(args.getInt(Constants.EXTRA_TRIP_ID));
            if (args.containsKey(Constants.EXTRA_EDIT)) {
                // Click on a trip for edit
                // Make a copy and work on it, in case user doesn't confirm
                trip = Data.getTripDraft(trip);
                isEditMode = true;
            }
        }
//        else if (args.containsKey(Constants.EXTRA_PERSON_ID)) {
//            // From the person, add a trip
//            trip = Data.getTripDraft();
//            Person person = Data.getPerson(args.getInt(Constants.EXTRA_PERSON_ID));
//            if (person != null) Data.addPersonTripLink(person.id, trip.id);
//        }
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
//        if (PeopleListFragment.peopleMenuItemSelected(item, mListener))
//            return true;
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
                if (trip.isDraft)
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
        bb.putInt(Constants.EXTRA_TRIP_ID, trip.id);
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PeopleListFragment.class.getName());
        mListener.onFragmentInteraction(b);
    }

    @Override
    public boolean allowBackPress() {
        if (trip.isDraft) {
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
            trip.setTitle(((EditText) root.findViewById(R.id.trip_title)).getText().toString());
            trip.setSubtitle(((EditText) root.findViewById(R.id.trip_subtitle)).getText().toString());
        }
        Trip committedTrip = Data.commitTripDraft(trip);
        if (committedTrip == null) {
            getMySuperActivity().showToast("Trip not committed");
            return false;
        }
        // needed, otherwise this fragment isn't correctly removed
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.popBackStack();
            fragmentManager.popBackStack();
        }

        Bundle bb = new Bundle();
        bb.putInt(Constants.EXTRA_TRIP_ID, committedTrip.id);
        Bundle b = new Bundle();
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, TripDetailFragment.class.getName());
        mListener.onFragmentInteraction(b);
        return true;
    }

    private void editTrip() {
        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putInt(Constants.EXTRA_TRIP_ID, trip.id);
        bb.putBoolean(Constants.EXTRA_EDIT, true);
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, TripDetailFragment.class.getName());
        mListener.onFragmentInteraction(b);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isNewMode || isEditMode) editTrip(view);
        else loadTrip(view);
    }

    private void editTrip(@NonNull View view) {
        EditText trip_title = view.findViewById(R.id.trip_title);
        trip_title.setText(trip.title);
        EditText trip_subtitle = view.findViewById(R.id.trip_subtitle);
        trip_subtitle.setText(trip.subtitle);
        RecyclerView people_joined = view.findViewById(R.id.list_people);
        PeopleListFragment.fillListView(this, people_joined, trip, null, false);
    }

    private void loadTrip(@NonNull View view) {
        TextView trip_title = view.findViewById(R.id.trip_title);
        trip_title.setText(trip.title);
        TextView trip_subtitle = view.findViewById(R.id.trip_subtitle);
        trip_subtitle.setText(trip.subtitle);
        RecyclerView people_joined = view.findViewById(R.id.list_people);
        PeopleListFragment.fillListView(this, people_joined, trip, null, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanData();
    }

    void cleanData() {
        if (trip.isDraft)
            Data.removeTrip(trip, null);
    }
}
