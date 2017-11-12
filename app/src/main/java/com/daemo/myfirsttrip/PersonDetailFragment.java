package com.daemo.myfirsttrip;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.daemo.myfirsttrip.models.Person;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;


public class PersonDetailFragment extends MySuperFragment implements EventListener<DocumentSnapshot>, OnCompleteListener<Void> {
    private DocumentReference personRef;
    private ListenerRegistration listenerRegistration;
    private Person person;
    private DetailFragmentMode currStatus;


    public PersonDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null || args.isEmpty()) {
            // From peopleIds list, add a person
            currStatus = DetailFragmentMode.NEW;
            return;
        }

        if (args.containsKey(Constants.EXTRA_PERSON_ID) &&
                args.containsKey(Constants.EXTRA_EDIT) && args.getBoolean(Constants.EXTRA_EDIT)) {
            // Click on a person for edit
            currStatus = DetailFragmentMode.EDIT;
            Data.createDraftPersonFromRef(Data.getPerson(args.getString(Constants.EXTRA_PERSON_ID)), task -> {
                if (task.getException() != null) {
                    getMySuperActivity().showToast(task.getException().getMessage());
                }
                personRef = task.getResult();
                if (listenerRegistration == null)
                    listenerRegistration = personRef.addSnapshotListener(PersonDetailFragment.this);
            });
            return;
        }

        if (args.containsKey(Constants.EXTRA_PERSON_ID)) {
            // Click on a person to see details
            currStatus = DetailFragmentMode.VIEW;
            personRef = Data.getPerson(args.getString(Constants.EXTRA_PERSON_ID));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = null;
        switch (currStatus) {
            case EDIT:
            case NEW:
                root = inflater.inflate(R.layout.fragment_person_edit, container, false);
                break;
            case VIEW:
                root = inflater.inflate(R.layout.fragment_person_detail, container, false);
                break;
        }
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
        switch (currStatus) {
            case VIEW:
            case EDIT:
                if (listenerRegistration == null && personRef != null)
                    listenerRegistration = personRef.addSnapshotListener(this);
                break;
            case NEW:
                break;
        }
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
        switch (currStatus) {
            case EDIT:
            case NEW:
                inflater.inflate(R.menu.person_detail_edit, menu);
                break;
            case VIEW:
                inflater.inflate(R.menu.person_detail, menu);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                if (person.isDraft())
                    getMySuperActivity().showOkCancelDialog("Confirm",
                            "Confirm the current modifications before choosing other tripsIds",
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

    private void chooseTrip() {
        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putBoolean(Constants.EXTRA_CHOOSE, true);
        bb.putString(Constants.EXTRA_PERSON_ID, person.getId());
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, TripsListFragment.class.getName());
        mListener.onFragmentInteraction(b);
    }

    @Override
    public boolean allowBackPress() {
        if (person.isDraft()) {
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
        setRefreshing(true);
        Data.commitPersonBatch(person, null, task -> setRefreshing(false));
        // needed, otherwise this fragment isn't correctly removed
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.popBackStack();
            fragmentManager.popBackStack();
        }

        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putString(Constants.EXTRA_PERSON_ID, person.getId());
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PersonDetailFragment.class.getName());
        mListener.onFragmentInteraction(b);
        return true;
    }

    private void editPerson() {
        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putString(Constants.EXTRA_PERSON_ID, person.id);
        bb.putBoolean(Constants.EXTRA_EDIT, true);
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PersonDetailFragment.class.getName());
        mListener.onFragmentInteraction(b);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switch (currStatus) {
            case NEW:
                String newId = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_COLLECTION).document().getId();
                person = new Person(newId, null, null);
                break;
            case EDIT:
            case VIEW:
                setRefreshing(true);
                break;
        }
    }

    private void loadEditLayoutPerson(@Nullable View view) {
        if (view == null) return;
        EditText person_name = view.findViewById(R.id.person_name);
        person_name.setText(person.name);
        EditText person_surname = view.findViewById(R.id.person_surname);
        person_surname.setText(person.surname);
        Bundle b = new Bundle();
        b.putString(Constants.EXTRA_PERSON_ID, person.getId());
        FragmentManager childFragmentManager = getChildFragmentManager();
        childFragmentManager.beginTransaction().replace(
                R.id.fragment_trips_list,
                Fragment.instantiate(getContext(), TripsListFragment.class.getName(), b)
        ).commit();
    }

    private void loadLayoutPerson(@Nullable View view) {
        if (view == null) return;
        TextView person_name = view.findViewById(R.id.person_name);
        person_name.setText(person.name);
        TextView person_surname = view.findViewById(R.id.person_surname);
        person_surname.setText(person.surname);
        Bundle b = new Bundle();
        b.putString(Constants.EXTRA_PERSON_ID, person.getId());
        FragmentManager childFragmentManager = getChildFragmentManager();
        childFragmentManager.beginTransaction().replace(
                R.id.fragment_trips_list,
                Fragment.instantiate(getContext(), TripsListFragment.class.getName(), b)
        ).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanData();
    }

    private void cleanData() {
        if (person.isDraft())
            Data.deletePersonBatch(person.getId(), null, this);
    }

    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            getMySuperActivity().showToast(e.getMessage());
            return;
        }

        if (!documentSnapshot.exists()) {
            setRefreshing(false);
            getMySuperActivity().showToast("Snapshot doesn't exist yet");
            return;
        }
        person = documentSnapshot.toObject(Person.class);
        switch (currStatus) {
            case EDIT:
                if (!person.isDraft())
                    getMySuperActivity().showToast("This should be a draft");
                loadEditLayoutPerson(getView());
                break;
            case NEW:
                getMySuperActivity().showToast("We shouldn't be here");
                break;
            case VIEW:
                loadLayoutPerson(getView());
                break;
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
