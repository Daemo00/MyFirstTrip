package com.daemo.myfirsttrip.adapter;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daemo.myfirsttrip.R;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.common.ItemTouchHelperAdapter;
import com.daemo.myfirsttrip.database.DataTrip;
import com.daemo.myfirsttrip.fragments.MySuperFragment;
import com.daemo.myfirsttrip.fragments.TripDetailFragment;
import com.daemo.myfirsttrip.models.Trip;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.Set;


public class TripsAdapter extends FirestoreAdapter<TripsAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    // Provide a suitable constructor (depends on the kind of dataset)
    public TripsAdapter(MySuperFragment fragment, Query mQuery, Set<String> selected_ids) {
        super(fragment, mQuery, selected_ids, Constants.TRIPS_COLLECTION);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TripsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trip_card_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new TripsAdapter.ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(TripsAdapter.ViewHolder holder, int position) {
        Trip trip = getSnapshot(position).toObject(Trip.class);
        holder.setTrip(fragment, trip);
        if (isChooseMode) {
            // Here I am just highlighting the background
            holder.isSelected(selectedIds.contains(trip.getId()) && !unselectedIds.contains(trip.getId()));
            selected_positions.add(position);
        }
    }

    @Override
    void deleteItem(DocumentSnapshot itemSnapshot) {
        myRefreshing.setRefreshing(true);
        DataTrip.deleteTripBatch(itemSnapshot, null, task -> myRefreshing.setRefreshing(false));
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private final TextView mTripTitle;
        private final TextView mTripSubtitle;
        private final CardView mTripCard;
        private final View mSelectedView;
        private final TextView mTripTotalCost;

        ViewHolder(CardView v) {
            super(v);
            mTripCard = v;
            mTripTitle = v.findViewById(R.id.trip_title);
            mTripSubtitle = v.findViewById(R.id.trip_subtitle);
            mTripTotalCost = v.findViewById(R.id.trip_total_cost);
            mSelectedView = v.findViewById(R.id.selected);
        }

        void isSelected(boolean isSelected) {
            mSelectedView.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }

        private void setTrip(MySuperFragment fragment, Trip trip) {
            this.mTripTitle.setText(trip.getTitle());
            this.mTripSubtitle.setText(trip.getSubtitle());
            this.mTripTotalCost.setText(String.valueOf(trip.getTotalCost()));
            if (isClickable) {
                Bundle b = new Bundle();
                Bundle bb = new Bundle();
                bb.putString(Constants.EXTRA_TRIP_ID, trip.getId());
                b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
                b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
                b.putString(Constants.EXTRA_REPLACE_FRAGMENT, TripDetailFragment.class.getName());
                mTripCard.setOnClickListener(
                        view -> {
                            if (isChooseMode) {
                                int position = getAdapterPosition();
                                // Below line is just like a safety check, because sometimes holder could be null,
                                // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
                                if (position == RecyclerView.NO_POSITION) return;

                                // Updating old as well as new positions
                                for (Integer selected_position : selected_positions)
                                    notifyItemChanged(selected_position);

                                if (unique) {
                                    unselectedIds.clear();
                                    unselectedIds.addAll(selectedIds);
                                    selectedIds.clear();
                                    selected_positions.clear();
                                }

                                if (selected_positions.contains(position))
                                    selected_positions.remove(position);
                                else
                                    selected_positions.add(position);

                                if (selectedIds.contains(trip.getId())) {
                                    unselectedIds.add(trip.getId());
                                    selectedIds.remove(trip.getId());
                                } else
                                    selectedIds.add(trip.getId());

                                for (Integer selected_position : selected_positions)
                                    notifyItemChanged(selected_position);

                            } else
                                fragment.mListener.onFragmentInteraction(b);
                        });
            }
        }
    }
}
