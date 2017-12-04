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
import com.daemo.myfirsttrip.database.DataPerson;
import com.daemo.myfirsttrip.fragments.MySuperFragment;
import com.daemo.myfirsttrip.fragments.PersonDetailFragment;
import com.daemo.myfirsttrip.models.Person;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.Set;


public class PeopleAdapter extends FirestoreAdapter<PeopleAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    // Provide a suitable constructor (depends on the kind of dataset)
    public PeopleAdapter(MySuperFragment fragment, Query mQuery, Set<String> selected_ids) {
        super(fragment, mQuery, selected_ids, Constants.PEOPLE_COLLECTION);
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
        Person person = getSnapshot(position).toObject(Person.class);
        holder.setPerson(fragment, person);
        if (isChooseMode)
            // Here I am just highlighting the background
            holder.isSelected(selectedIds.contains(person.getId()));
    }

    @Override
    void deleteItem(DocumentSnapshot itemSnapshot) {
        myRefreshing.setRefreshing(true);
        DataPerson.deletePersonBatch(itemSnapshot.getReference(), null, task -> myRefreshing.setRefreshing(false));
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private final TextView mPersonTitle;
        private final TextView mPersonSubtitle;
        private final TextView mPersonTotalDebt;
        private final CardView mPersonCard;
        private final View mSelectedView;

        ViewHolder(CardView v) {
            super(v);
            mPersonCard = v;
            mPersonTitle = v.findViewById(R.id.person_name);
            mPersonSubtitle = v.findViewById(R.id.person_surname);
            mPersonTotalDebt = v.findViewById(R.id.person_total_debt);
            mSelectedView = v.findViewById(R.id.selected);
        }

        void isSelected(boolean isSelected) {
            mSelectedView.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }

        private void setPerson(MySuperFragment fragment, Person person) {
            this.mPersonTitle.setText(person.getName());
            this.mPersonSubtitle.setText(person.getSurname());
            this.mPersonTotalDebt.setText(String.valueOf(person.getTotalDebt()));
            if (isClickable) {
                Bundle b = new Bundle();
                Bundle bb = new Bundle();
                bb.putString(Constants.EXTRA_PERSON_ID, person.getId());
                b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
                b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
                b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PersonDetailFragment.class.getName());
                mPersonCard.setOnClickListener(
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

                                if (selectedIds.contains(person.getId())) {
                                    unselectedIds.add(person.getId());
                                    selectedIds.remove(person.getId());
                                } else
                                    selectedIds.add(person.getId());

                                for (Integer selected_position : selected_positions)
                                    notifyItemChanged(selected_position);

                            } else
                                fragment.mListener.onFragmentInteraction(b);
                        });
            }
        }
    }
}
