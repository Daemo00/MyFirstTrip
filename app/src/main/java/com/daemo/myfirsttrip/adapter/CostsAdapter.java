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
import com.daemo.myfirsttrip.database.DataCost;
import com.daemo.myfirsttrip.fragments.CostDetailFragment;
import com.daemo.myfirsttrip.fragments.MySuperFragment;
import com.daemo.myfirsttrip.models.Cost;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.Locale;
import java.util.Set;


public class CostsAdapter extends FirestoreAdapter<CostsAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    // Provide a suitable constructor (depends on the kind of dataset)
    public CostsAdapter(MySuperFragment fragment, Query mQuery, Set<String> selected_ids) {
        super(fragment, mQuery, selected_ids, Constants.COSTS_COLLECTION);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CostsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cost_card_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new CostsAdapter.ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CostsAdapter.ViewHolder holder, int position) {
        Cost cost = getSnapshot(position).toObject(Cost.class);
        holder.setCost(fragment, cost);
        if (isChooseMode)
            // Here I am just highlighting the background
            holder.isSelected(selectedIds.contains(cost.getId()));
    }

    @Override
    void deleteItem(DocumentSnapshot itemSnapshot) {
        myRefreshing.setRefreshing(true);
        DataCost.deleteCostBatch(itemSnapshot, null, task -> myRefreshing.setRefreshing(false));
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private final TextView mCostQuantity;
        private final TextView mCostMotivation;
        private final CardView mCostCard;
        private final View mSelectedView;

        ViewHolder(CardView v) {
            super(v);
            mCostCard = v;
            mCostQuantity = v.findViewById(R.id.cost_quantity);
            mCostMotivation = v.findViewById(R.id.cost_motivation);
            mSelectedView = v.findViewById(R.id.selected);
        }

        void isSelected(boolean isSelected) {
            mSelectedView.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }

        private void setCost(MySuperFragment fragment, Cost cost) {
            this.mCostQuantity.setText(String.format(Locale.getDefault(), "%f", cost.getQuantity()));
            this.mCostMotivation.setText(cost.getMotivation());
            if (isClickable) {
                Bundle b = new Bundle();
                Bundle bb = new Bundle();
                bb.putString(Constants.EXTRA_COST_ID, cost.getId());
                b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
                b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
                b.putString(Constants.EXTRA_REPLACE_FRAGMENT, CostDetailFragment.class.getName());
                mCostCard.setOnClickListener(
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

                                if (selectedIds.contains(cost.getId())) {
                                    unselectedIds.add(cost.getId());
                                    selectedIds.remove(cost.getId());
                                } else
                                    selectedIds.add(cost.getId());

                                for (Integer selected_position : selected_positions)
                                    notifyItemChanged(selected_position);

                            } else
                                fragment.mListener.onFragmentInteraction(b);
                        });
            }
        }
    }
}
