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


public class PersonDetailFragment extends MySuperFragment {
    private Person person;
    private boolean isNewMode;
    private boolean isEditMode;


    public PersonDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            // From people list, add a person
            person = Data.getPersonDraft();
            isNewMode = true;
        } else if (args.containsKey(Constants.EXTRA_PERSON_ID)) {
            // Click on a person to see details
            person = Data.getPerson(args.getInt(Constants.EXTRA_PERSON_ID));
            if (args.containsKey(Constants.EXTRA_EDIT)) {
                // Click on a person for edit
                // Make a copy and work on it, in case user doesn't confirm
                person = Data.getPersonDraft(person);
                isEditMode = true;
            }
        }
//        else if (args.containsKey(Constants.EXTRA_TRIP_ID)) {
//            // From the trip, add a person
//            person = Data.getPersonDraft();
//            Trip trip = Data.getTrip(args.getInt(Constants.EXTRA_TRIP_ID));
//            if (trip != null) Data.addPersonTripLink(person.id, trip.id);
//        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = isNewMode || isEditMode ?
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
        // Also add the possibility to add an existing trip
        if (isNewMode || isEditMode)
            inflater.inflate(R.menu.person_detail_edit, menu);
        else
            inflater.inflate(R.menu.person_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (TripsListFragment.tripsMenuItemSelected(item, mListener))
//            return true;
        switch (item.getItemId()) {
            case R.id.clear_person:
                cleanData();
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null)
                    fragmentManager.popBackStack();
                break;
            case R.id.confirm_person:
                return confirmPerson();
            case R.id.edit_person:
                if (person.isDraft)
                    getMySuperActivity().showOkCancelDialog("Confirm",
                            "Confirm the current modifications before choosing other trips",
                            (dialogInterface, i) -> confirmPerson());
                else
                    editPerson();
                return true;
            case R.id.choose_trip:
                chooseTrip();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void chooseTrip() {
        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putBoolean(Constants.EXTRA_CHOOSE, true);
        bb.putInt(Constants.EXTRA_PERSON_ID, person.id);
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, TripsListFragment.class.getName());
        mListener.onFragmentInteraction(b);
    }

    @Override
    public boolean allowBackPress() {
        if (person.isDraft) {
            getMySuperActivity().showOkCancelDialog("Confirm",
                    "Confirm the current modifications?",
                    (dialogInterface, i) -> confirmPerson());
            return false;
        }
        return super.allowBackPress();
    }

    private boolean confirmPerson() {
        //TODO validation
        View root = getView();
        if (root != null) {
            person.setName(((EditText) root.findViewById(R.id.person_name)).getText().toString());
            person.setSurname(((EditText) root.findViewById(R.id.person_surname)).getText().toString());
        }
        Person committedPerson = Data.commitPersonDraft(person);
        if (committedPerson == null) {
            getMySuperActivity().showToast("Person not committed");
            return false;
        }
        // needed, otherwise this fragment isn't correctly removed
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.popBackStack();
            fragmentManager.popBackStack();
        }

        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putInt(Constants.EXTRA_PERSON_ID, committedPerson.id);
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PersonDetailFragment.class.getName());
        mListener.onFragmentInteraction(b);
        return true;
    }

    private void editPerson() {
        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putInt(Constants.EXTRA_PERSON_ID, person.id);
        bb.putBoolean(Constants.EXTRA_EDIT, true);
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PersonDetailFragment.class.getName());
        mListener.onFragmentInteraction(b);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (person.isDraft) loadEditLayoutPerson(view);
        else loadLayoutPerson(view);
    }

    private void loadEditLayoutPerson(@NonNull View view) {
        EditText person_name = view.findViewById(R.id.person_name);
        person_name.setText(person.name);
        EditText person_surname = view.findViewById(R.id.person_surname);
        person_surname.setText(person.surname);
        RecyclerView people_joined = view.findViewById(R.id.list_trips);
        TripsListFragment.fillListView(this, people_joined, person, null, false);
    }

    private void loadLayoutPerson(@NonNull View view) {
        TextView person_name = view.findViewById(R.id.person_name);
        person_name.setText(person.name);
        TextView person_surname = view.findViewById(R.id.person_surname);
        person_surname.setText(person.surname);
        RecyclerView people_joined = view.findViewById(R.id.list_trips);
        TripsListFragment.fillListView(this, people_joined, person, null, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanData();
    }

    void cleanData() {
        if (person.isDraft)
            Data.removePerson(person, null);
    }
}
