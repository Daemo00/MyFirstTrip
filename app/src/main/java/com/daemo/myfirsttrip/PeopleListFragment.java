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


public class PeopleListFragment extends MySuperFragment {

    private boolean isChooseMode;
    private RecyclerView recyclerView;
    /**
     * When this fragment is summoned to add people to a trip, this is the id of that trip
     */
    private Trip orig_trip;

    public PeopleListFragment() {
        // Required empty public constructor
    }

    /**
     * If selected_person_ids is not null, we are in choose mode
     */
    public static void fillListView(MySuperFragment fragment, RecyclerView recyclerView, Trip trip, Set<Integer> selected_person_ids, boolean isClickable) {
        recyclerView.setLayoutManager(new LinearLayoutManager(fragment.getContext()));
        recyclerView.setHasFixedSize(true);
        RecyclerView.Adapter adapter = new PeopleAdapter(fragment, trip, selected_person_ids, isClickable);
        recyclerView.setAdapter(adapter);
        if (trip == null && selected_person_ids == null) {
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
                && args.containsKey(Constants.EXTRA_TRIP_ID)) {
            isChooseMode = true;
            orig_trip = Data.getTrip(args.getInt(Constants.EXTRA_TRIP_ID));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_people_list, container, false);
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
        recyclerView = view.findViewById(R.id.list_people);
        Set<Integer> selected_person_ids = null;
        if (isChooseMode) {
            selected_person_ids = new HashSet<>();
            for (Person person : Data.getPeople(orig_trip))
                selected_person_ids.add(person.id);
        }
        PeopleListFragment.fillListView(this, recyclerView, null, selected_person_ids, true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (isChooseMode)
            inflater.inflate(R.menu.people_list_choose, menu);
        else
            inflater.inflate(R.menu.people_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle b = new Bundle();
        switch (item.getItemId()) {
            case R.id.add_person:
                b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
                b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PersonDetailFragment.class.getName());
                mListener.onFragmentInteraction(b);
                return true;
            case R.id.confirm_person:
                return confirmSelection();
        }
        return super.onOptionsItemSelected(item);
    }

    boolean confirmSelection() {
        PeopleAdapter adapter = (PeopleAdapter) recyclerView.getAdapter();

        // Re-add the trip and all its links
        Data.removeTrip(orig_trip, null);
        boolean isTripAdded = Data.addTrip(orig_trip, adapter.selected_person_ids);
        FragmentManager fragmentManager = getFragmentManager();
        if (!isTripAdded)
            getMySuperActivity().showToast("Trip not added");
        else if (fragmentManager != null)
            // Go back to whoever called this
            fragmentManager.popBackStack();
        else
            getMySuperActivity().showToast("Fragment manager not found");
        return isTripAdded;
    }

    @Override
    public boolean allowBackPress() {
        if (orig_trip != null && orig_trip.isDraft) {
            getMySuperActivity().showOkCancelDialog("Confirm",
                    "Confirm the current modifications?",
                    (dialogInterface, i) -> confirmSelection());
            return false;
        }
        return super.allowBackPress();
    }
}

class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private final Trip trip;
    private final MySuperFragment fragment;
    Set<Integer> selected_person_ids = new HashSet<>();
    private boolean isChooseMode;
    private Set<Integer> selected_positions = new HashSet<>();
    private boolean isClickable;

    // Provide a suitable constructor (depends on the kind of dataset)
    PeopleAdapter(MySuperFragment fragment, Trip trip, Set<Integer> selected_person_ids, boolean isClickable) {
        this.fragment = fragment;
        this.trip = trip;
        this.isChooseMode = selected_person_ids != null;
        this.selected_person_ids = selected_person_ids;
        this.isClickable = isClickable;
    }

    private List<Person> getDataset() {
        return Data.getPeople(trip);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PeopleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.person_card_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new PeopleAdapter.ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(PeopleAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Person person = getDataset().get(position);
        holder.setPerson(fragment, person, isClickable);
        if (isChooseMode) {
            // Here I am just highlighting the background
            holder.itemView.setBackgroundColor(selected_person_ids.contains(person.id) ? Color.GREEN : Color.TRANSPARENT);
        }
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
                    Person person = getDataset().get(position);
                    Data.removePerson(person, trip);
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
        private final TextView mPersonTitle;
        private final TextView mPersonSubtitle;
        private final CardView mPersonCard;

        ViewHolder(CardView v) {
            super(v);
            mPersonCard = v;
            mPersonTitle = v.findViewById(R.id.person_name);
            mPersonSubtitle = v.findViewById(R.id.person_surname);
        }

        private void setPerson(MySuperFragment fragment, Person person, boolean isClickable) {
            this.mPersonTitle.setText(person.name);
            this.mPersonSubtitle.setText(person.surname);
//                Intent i = new Intent(Constants.ACTION_TRIP_SELECTED);
//                i.putExtra(Constants.EXTRA_TRIP_ID, trip.id);
            if (isClickable) {
                Bundle b = new Bundle();
                Bundle bb = new Bundle();
                bb.putInt(Constants.EXTRA_PERSON_ID, person.id);
                b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
                b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
                b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PersonDetailFragment.class.getName());
                mPersonCard.setOnClickListener(
                        view -> {
                            if (isChooseMode) {
                                int position = getAdapterPosition();
                                // Below line is just like a safety check, because sometimes holder could be null,
                                // in that case, position will return RecyclerView.NO_POSITION
                                if (position == RecyclerView.NO_POSITION) return;

                                // Updating old as well as new positions
                                for (Integer selected_position : selected_positions)
                                    notifyItemChanged(selected_position);

                                if (selected_positions.contains(position))
                                    selected_positions.remove(position);
                                else
                                    selected_positions.add(position);

                                if (selected_person_ids.contains(person.id))
                                    selected_person_ids.remove(person.id);
                                else
                                    selected_person_ids.add(person.id);

                                for (Integer selected_position : selected_positions)
                                    notifyItemChanged(selected_position);

                            } else
                                fragment.mListener.onFragmentInteraction(b);
                            // Suggested way is using interfaces
                            // LocalBroadcastManager.getInstance(mPersonCard.getContext()).sendBroadcast(i);

                            // No because it recreates the activity
                            // mPersonCard.getContext().startActivity(i);
                        });
            }
        }
    }
}
