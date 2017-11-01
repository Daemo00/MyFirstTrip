package com.daemo.myfirsttrip;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.common.ItemTouchHelperAdapter;
import com.daemo.myfirsttrip.common.SimpleItemTouchHelperCallback;
import com.daemo.myfirsttrip.database.Data;
import com.daemo.myfirsttrip.models.Person;
import com.daemo.myfirsttrip.models.Trip;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TripsListFragment extends MySuperFragment {

    private boolean isChooseMode;
    private RecyclerView recyclerView;
    /**
     * When this fragment is summoned to add trips to a person, this is the id of that person
     */
    private Person orig_person;

    public TripsListFragment() {
        // Required empty public constructor
    }

    /**
     * If selected_trip_ids is not null, we are in choose mode
     */
    public static void fillListView(MySuperFragment fragment, RecyclerView recyclerView, Person person, Set<Integer> selected_trip_ids, boolean isClickable) {
        recyclerView.setLayoutManager(new LinearLayoutManager(fragment.getContext()));
        recyclerView.setHasFixedSize(true);
        RecyclerView.Adapter adapter = new TripsAdapter(fragment, person, selected_trip_ids, isClickable);
        recyclerView.setAdapter(adapter);
        if (person == null && selected_trip_ids == null) {
            // Allow edit on the list only if we are in the main list
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback((ItemTouchHelperAdapter) adapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(recyclerView);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null) return;
        if (args.containsKey(Constants.EXTRA_CHOOSE)
                && args.getBoolean(Constants.EXTRA_CHOOSE)
                && args.containsKey(Constants.EXTRA_PERSON_ID)) {
            isChooseMode = true;
            orig_person = Data.getPerson(args.getInt(Constants.EXTRA_PERSON_ID));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_trips_list, container, false);
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
        recyclerView = view.findViewById(R.id.list_trips);
        Set<Integer> selected_trip_ids = null;
        if (isChooseMode) {
            selected_trip_ids = new HashSet<>();
            for (Trip trip : Data.getTrips(orig_person))
                selected_trip_ids.add(trip.id);
        }
        TripsListFragment.fillListView(this, recyclerView, null, selected_trip_ids, true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (isChooseMode)
            inflater.inflate(R.menu.trips_list_choose, menu);
        else
            inflater.inflate(R.menu.trips_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_trip:
                Bundle b = new Bundle();
                b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
                b.putString(Constants.EXTRA_REPLACE_FRAGMENT, TripDetailFragment.class.getName());
                mListener.onFragmentInteraction(b);
                return true;
            case R.id.confirm_trip:
                return confirmSelection();
        }
        return super.onOptionsItemSelected(item);
    }

    boolean confirmSelection() {
        TripsAdapter adapter = (TripsAdapter) recyclerView.getAdapter();

        // Re-add the trip and all its links
        Data.removePerson(orig_person, null);
        boolean isPersonAdded = Data.addPerson(orig_person, adapter.selected_trip_ids);
        FragmentManager fragmentManager = getFragmentManager();
        if (!isPersonAdded)
            getMySuperActivity().showToast("Person not added");
        else if (fragmentManager != null)
            // Go back to whoever called this
            fragmentManager.popBackStack();
        else
            getMySuperActivity().showToast("Fragment manager not found");
        return isPersonAdded;
    }

    @Override
    public boolean allowBackPress() {
        if (orig_person != null && orig_person.isDraft) {
            getMySuperActivity().showOkCancelDialog("Confirm",
                    "Confirm the current modifications?",
                    (dialogInterface, i) -> confirmSelection());
            return false;
        }
        return super.allowBackPress();
    }
}


class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private final Person person;
    private final MySuperFragment fragment;
    Set<Integer> selected_trip_ids = new HashSet<>();
    private boolean isChooseMode;
    private Set<Integer> selected_positions = new HashSet<>();
    private boolean isClickable;

    // Provide a suitable constructor (depends on the kind of dataset)
    TripsAdapter(MySuperFragment fragment, Person person, Set<Integer> selected_trip_ids, boolean isClickable) {
        this.fragment = fragment;
        this.person = person;
        this.isChooseMode = selected_trip_ids != null;
        this.selected_trip_ids = selected_trip_ids;
        this.isClickable = isClickable;
    }

    private List<Trip> getDataset() {
        return Data.getTrips(person);
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
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Trip trip = getDataset().get(position);
        holder.setTrip(fragment, trip, isClickable);
        if (isChooseMode)
            // Here I am just highlighting the background
            holder.itemView.setBackgroundColor(selected_trip_ids.contains(trip.id) ? Color.GREEN : Color.TRANSPARENT);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return getDataset().size();
    }

    @Override
    public void onItemDismiss(int position) {
        fragment.getMySuperActivity().showUndoSnackbar(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (event == BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION) {
                    // If UNDO is clicked, restore the item
                    notifyItemChanged(position);
                } else {
                    // Otherwise remove the item
                    Trip trip = getDataset().get(position);
                    Data.removeTrip(trip, person);
                    notifyItemRemoved(position);
                }
            }
        });
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition)
            for (int i = fromPosition; i < toPosition; i++)
                Collections.swap(getDataset(), i, i + 1);
        else
            for (int i = fromPosition; i > toPosition; i--)
                Collections.swap(getDataset(), i, i - 1);
        notifyItemMoved(fromPosition, toPosition);
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
            mTripTitle = v.findViewById(R.id.trip_title);
            mTripSubtitle = v.findViewById(R.id.trip_subtitle);
        }

        private void setTrip(MySuperFragment fragment, Trip trip, boolean isClickable) {
            this.mTripTitle.setText(trip.title);
            this.mTripSubtitle.setText(trip.subtitle);
//                Intent i = new Intent(Constants.ACTION_TRIP_SELECTED);
//                i.putExtra(Constants.EXTRA_TRIP_ID, trip.id);
            if (isClickable) {
                Bundle b = new Bundle();
                Bundle bb = new Bundle();
                bb.putInt(Constants.EXTRA_TRIP_ID, trip.id);
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

                                if (selected_positions.contains(position))
                                    selected_positions.remove(position);
                                else
                                    selected_positions.add(position);

                                if (selected_trip_ids.contains(trip.id))
                                    selected_trip_ids.remove(trip.id);
                                else
                                    selected_trip_ids.add(trip.id);

                                for (Integer selected_position : selected_positions)
                                    notifyItemChanged(selected_position);

                            } else
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
