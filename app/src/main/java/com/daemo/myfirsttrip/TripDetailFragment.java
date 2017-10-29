package com.daemo.myfirsttrip;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
            // TODO quando non sono più bozze?
            trip = Data.getTripDraft();
            Person person = Data.getPerson(args.getInt(Constants.EXTRA_PERSON_ID));
            if (person != null) Data.addPersonTripLink(person, trip);
        }
        isEditMode = trip == null || trip.isDraft;
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
        inflater.inflate(R.menu.people_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_person:
                Bundle b = new Bundle();
                // TODO tieni conto quando è bozza
                Bundle bb = new Bundle();
                bb.putInt(Constants.EXTRA_TRIP_ID, trip.id);
                b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
                b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
                b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PersonDetailFragment.class.getName());
                mListener.onFragmentInteraction(b);
        }
        return super.onOptionsItemSelected(item);
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
