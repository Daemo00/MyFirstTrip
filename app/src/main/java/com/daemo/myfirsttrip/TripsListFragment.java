package com.daemo.myfirsttrip;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daemo.myfirsttrip.R.id;
import com.daemo.myfirsttrip.R.layout;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.database.Data;
import com.daemo.myfirsttrip.models.Trip;

import java.util.List;


public class TripsListFragment extends MySuperFragment {

    public TripsListFragment() {
        // Required empty public constructor
    }

    public static void fillListView(MySuperFragment fragment, RecyclerView recyclerView, List<Trip> trips) {
        recyclerView.setLayoutManager(new LinearLayoutManager(fragment.getContext()));
        recyclerView.setAdapter(new TripsAdapter(fragment, trips));
    }

    private static boolean tripsMenuItemSelected(MenuItem item, OnFragmentInteractionListener mListener) {
        switch (item.getItemId()) {
            case id.add_trip:
                Bundle b = new Bundle();
                b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
                b.putString(Constants.EXTRA_REPLACE_FRAGMENT, TripDetailFragment.class.getName());
                mListener.onFragmentInteraction(b);
                return true;
        }
        return false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = inflater.inflate(layout.fragment_trips_list, container, false);
        TripsListFragment.fillListView(this, root.findViewById(id.list_trips), Data.trips);
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
        return tripsMenuItemSelected(item, mListener) || super.onOptionsItemSelected(item);
    }
}


class TripsAdapter extends Adapter<TripsAdapter.ViewHolder> {
    private final List<Trip> dataset;
    private final MySuperFragment fragment;

    // Provide a suitable constructor (depends on the kind of dataset)
    TripsAdapter(MySuperFragment fragment, List<Trip> dataset) {
        this.fragment = fragment;
        this.dataset = dataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TripsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(layout.trip_card_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new TripsAdapter.ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(TripsAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.setTrip(fragment, dataset.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private final TextView mTripTitle;
        private final TextView mTripSubtitle;
        private final CardView mTripCard;

        ViewHolder(CardView v) {
            super(v);
            mTripCard = v;
            mTripTitle = v.findViewById(id.trip_title);
            mTripSubtitle = v.findViewById(id.trip_subtitle);
        }

        private void setTrip(MySuperFragment fragment, Trip trip) {
            this.mTripTitle.setText(trip.title);
            this.mTripSubtitle.setText(trip.subtitle);
//                Intent i = new Intent(Constants.ACTION_TRIP_SELECTED);
//                i.putExtra(Constants.EXTRA_TRIP_ID, trip.id);
            Bundle b = new Bundle();
            Bundle bb = new Bundle();
            bb.putInt(Constants.EXTRA_TRIP_ID, trip.id);
            b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
            b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
            b.putString(Constants.EXTRA_REPLACE_FRAGMENT, TripDetailFragment.class.getName());
            mTripCard.setOnClickListener(
                    view -> {
                        fragment.mListener.onFragmentInteraction(b);
                        // Suggested way is using interfaces
                        // LocalBroadcastManager.getInstance(mTripCard.getContext()).sendBroadcast(i);

                        // No because it recreates the activity
                        // mTripCard.getContext().startActivity(i);
                    });
        }
    }
}
