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

import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.database.Data;
import com.daemo.myfirsttrip.models.Person;
import com.daemo.myfirsttrip.models.Trip;


public class PersonDetailFragment extends MySuperFragment {


    private Person person;
    private boolean isEditMode;

    public PersonDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null)
            person = Data.getPersonDraft();
        else if (args.containsKey(Constants.EXTRA_PERSON_ID))
            person = Data.getPerson(args.getInt(Constants.EXTRA_PERSON_ID));
        else if (args.containsKey(Constants.EXTRA_TRIP_ID)) {
            person = Data.getPersonDraft();
            Trip trip = Data.getTrip(args.getInt(Constants.EXTRA_TRIP_ID));
            if (trip != null) Data.addPersonTripLink(person, trip);
        }
        isEditMode = person.isDraft;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = isEditMode ?
                inflater.inflate(R.layout.fragment_person_edit, container, false) :
                inflater.inflate(R.layout.fragment_person_detail, container, false);

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
        inflater.inflate(R.menu.trips_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_trip:
                Bundle b = new Bundle();
                Bundle bb = new Bundle();
                bb.putInt(Constants.EXTRA_PERSON_ID, person.id);
                b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
                b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
                b.putString(Constants.EXTRA_REPLACE_FRAGMENT, TripDetailFragment.class.getName());
                mListener.onFragmentInteraction(b);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (person.isDraft) editPerson(view);
        else loadPerson(view);
    }

    private void editPerson(@NonNull View view) {
        EditText person_title = view.findViewById(R.id.person_title);
        person_title.setText(person.name);
        EditText person_subtitle = view.findViewById(R.id.person_subtitle);
        person_subtitle.setText(person.surname);
        RecyclerView people_joined = view.findViewById(R.id.list_trips);
        TripsListFragment.fillListView(this, people_joined, Data.getTrips(person));
    }

    private void loadPerson(@NonNull View view) {
        TextView person_title = view.findViewById(R.id.person_title);
        person_title.setText(person.name);
        TextView person_subtitle = view.findViewById(R.id.person_subtitle);
        person_subtitle.setText(person.surname);
        RecyclerView people_joined = view.findViewById(R.id.list_trips);
        TripsListFragment.fillListView(this, people_joined, Data.getTrips(person));
    }

}
