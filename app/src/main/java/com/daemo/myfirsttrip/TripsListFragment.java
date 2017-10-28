package com.daemo.myfirsttrip;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
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
    private MySuperFragment.OnFragmentInteractionListener mListener;

    public TripsListFragment() {
        // Required empty public constructor
    }

// --Commented out by Inspection START (28-Oct-17 17:26):
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment TripsListFragment.
//     */
//    public static TripsListFragment newInstance(String param1, String param2) {
//        TripsListFragment fragment = new TripsListFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
// --Commented out by Inspection STOP (28-Oct-17 17:26)

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            String mParam1 = getArguments().getString(ARG_PARAM1);
//            String mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = inflater.inflate(layout.fragment_trips_list, container, false);
        fillListView(root.findViewById(id.list_trips));
        if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            viewGroup.addView(root);
            return viewGroup;
        }

        return root;
    }

    private void fillListView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TripsAdapter(this));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MySuperFragment.OnFragmentInteractionListener) {
            mListener = (MySuperFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    static class TripsAdapter extends Adapter<TripsListFragment.TripsAdapter.ViewHolder> {
        private final List<Trip> dataset;
        private final TripsListFragment fragment;

        // Provide a suitable constructor (depends on the kind of dataset)
        TripsAdapter(TripsListFragment fragment) {
            this.fragment = fragment;
            dataset = Data.trips;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public TripsListFragment.TripsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                            int viewType) {
            // create a new view
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(layout.trip_card_view, parent, false);
            // set the view's size, margins, paddings and layout parameters
            return new TripsListFragment.TripsAdapter.ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(TripsListFragment.TripsAdapter.ViewHolder holder, int position) {
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
        static class ViewHolder extends RecyclerView.ViewHolder {
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

            private void setTrip(TripsListFragment fragment, Trip trip) {
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
}
