package com.daemo.myfirsttrip;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daemo.myfirsttrip.R.id;
import com.daemo.myfirsttrip.R.layout;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.database.Data;
import com.daemo.myfirsttrip.models.Trip;


public class TripDetailFragment extends MySuperFragment {
    private Trip trip;


    public TripDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            trip = Data.getTrip(getArguments().getInt(Constants.EXTRA_TRIP_ID));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = inflater.inflate(layout.fragment_trip_detail, container, false);
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
        if (trip == null) return;
        TextView trip_title = view.findViewById(id.trip_title);
        trip_title.setText(trip.title);
        TextView trip_subtitle = view.findViewById(id.trip_subtitle);
        trip_subtitle.setText(trip.subtitle);
        RecyclerView people_joined = view.findViewById(id.list_people);
        PeopleListFragment.fillListView(this, people_joined, Data.getPeople(trip));
    }
}
