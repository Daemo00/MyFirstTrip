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

import com.daemo.myfirsttrip.R.id;
import com.daemo.myfirsttrip.R.layout;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.database.Data;
import com.daemo.myfirsttrip.models.Person;
import com.daemo.myfirsttrip.models.Trip;


public class TripDetailFragment extends MySuperFragment {
    private Trip trip;
    private boolean isEditMode;


    public TripDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null)
            trip = Data.getTripDraft();
        else if (args.containsKey(Constants.EXTRA_TRIP_ID))
            trip = Data.getTrip(args.getInt(Constants.EXTRA_TRIP_ID));
        else if (args.containsKey(Constants.EXTRA_PERSON_ID)) {
            trip = Data.getTripDraft();
            Person person = Data.getPerson(args.getInt(Constants.EXTRA_PERSON_ID));
            if (person != null) Data.addPersonTripLink(person, trip);
        }
        isEditMode = trip.isDraft;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = isEditMode ?
                inflater.inflate(layout.fragment_trip_edit, container, false) :
                inflater.inflate(layout.fragment_trip_detail, container, false);
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
//        // Add everything that you can do in the list of trips
//        inflater.inflate(R.menu.people_list, menu);
        // Also add the possibility to add an existing person
        if (isEditMode)
            inflater.inflate(R.menu.trip_detail_edit, menu);
        inflater.inflate(R.menu.trip_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (PeopleListFragment.peopleMenuItemSelected(item, mListener))
//            return true;
        switch (item.getItemId()) {
            case R.id.confirm_trip:
                return confirmTrip();
            case R.id.choose_person:
                if (trip.isDraft)
                    getMySuperActivity().showOkCancelDialog("Confirm",
                            "Confirm the current modifications before choosing other people",
                            (dialogInterface, i) -> confirmTrip());
                else
                    choosePerson();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean confirmTrip() {
        //TODO use inserted data and validation
        Trip committedTrip = Data.commitTripDraft(trip);
        if (committedTrip == null) {
            getMySuperActivity().showToast("Trip not committed");
            return false;
        }
        // needed, otherwise this fragment isn't correctly removed
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null)
            fragmentManager.popBackStack();

        Bundle bb = new Bundle();
        bb.putInt(Constants.EXTRA_TRIP_ID, committedTrip.id);
        Bundle b = new Bundle();
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, TripDetailFragment.class.getName());
        mListener.onFragmentInteraction(b);
        return true;
    }

    private void choosePerson() {
        //TODO choose from people list
        getMySuperActivity().showOkCancelDialog("Sorry!", "Not yet implemented",
                (dialogInterface, i) -> {
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isEditMode) editTrip(view);
        else loadTrip(view);
    }

    private void editTrip(@NonNull View view) {
        EditText trip_title = view.findViewById(id.trip_title);
        trip_title.setText(trip.title);
        EditText trip_subtitle = view.findViewById(id.trip_subtitle);
        trip_subtitle.setText(trip.subtitle);
        RecyclerView people_joined = view.findViewById(id.list_people);
        PeopleListFragment.fillListView(this, people_joined, Data.getPeople(trip));
    }

    private void loadTrip(@NonNull View view) {
        TextView trip_title = view.findViewById(id.trip_title);
        trip_title.setText(trip.title);
        TextView trip_subtitle = view.findViewById(id.trip_subtitle);
        trip_subtitle.setText(trip.subtitle);
        RecyclerView people_joined = view.findViewById(id.list_people);
        PeopleListFragment.fillListView(this, people_joined, Data.getPeople(trip));
    }

}
