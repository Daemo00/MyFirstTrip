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
        // From the list, add a person
        if (args == null)
            person = Data.getPersonDraft();
            // Click on a person
        else if (args.containsKey(Constants.EXTRA_PERSON_ID))
            person = Data.getPerson(args.getInt(Constants.EXTRA_PERSON_ID));
            // From the trip, add a person
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
//        // Add everything that you can do in the list of trips
//        inflater.inflate(R.menu.trips_list, menu);
        // Also add the possibility to add an existing trip
        if (isEditMode)
            inflater.inflate(R.menu.person_detail_edit, menu);
        inflater.inflate(R.menu.person_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (TripsListFragment.tripsMenuItemSelected(item, mListener))
//            return true;
        switch (item.getItemId()) {
            case R.id.confirm_person:
                return confirmPerson();
            case R.id.choose_trip:
                if (person.isDraft)
                    getMySuperActivity().showOkCancelDialog("Confirm",
                            "Confirm the current modifications before choosing other trips",
                            (dialogInterface, i) -> chooseTrip());
                else
                    chooseTrip();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean confirmPerson() {
        //TODO use inserted data and validation
        Person committedPerson = Data.commitPersonDraft(person);
        if (committedPerson == null) {
            getMySuperActivity().showToast("Person not committed");
            return false;
        }
        // needed, otherwise this fragment isn't correctly removed
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null)
            fragmentManager.popBackStack();

        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putInt(Constants.EXTRA_PERSON_ID, committedPerson.id);
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PersonDetailFragment.class.getName());
        mListener.onFragmentInteraction(b);
        return true;
    }

    private void chooseTrip() {
        //TODO choose from trips list
        getMySuperActivity().showOkCancelDialog("Sorry!", "Not yet implemented",
                (dialogInterface, i) -> {
                });
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
