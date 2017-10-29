package com.daemo.myfirsttrip;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.database.Data;
import com.daemo.myfirsttrip.models.Person;


public class PersonDetailFragment extends MySuperFragment {


    private Person person;

    public PersonDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            person = Data.getPerson(getArguments().getInt(Constants.EXTRA_PERSON_ID));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_person_detail, container, false);
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
        if (person == null) return;
        TextView person_title = view.findViewById(R.id.person_title);
        person_title.setText(person.name);
        TextView person_subtitle = view.findViewById(R.id.person_subtitle);
        person_subtitle.setText(person.surname);
        RecyclerView people_joined = view.findViewById(R.id.list_trips);
        TripsListFragment.fillListView(this, people_joined, Data.getTrips(person));
    }

}
