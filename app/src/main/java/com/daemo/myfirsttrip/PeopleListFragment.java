package com.daemo.myfirsttrip;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.database.Data;
import com.daemo.myfirsttrip.models.Person;

import java.util.List;


public class PeopleListFragment extends MySuperFragment {

    public PeopleListFragment() {
        // Required empty public constructor
    }

    public static void fillListView(MySuperFragment fragment, RecyclerView recyclerView, List<Person> people) {
        recyclerView.setLayoutManager(new LinearLayoutManager(fragment.getContext()));
        recyclerView.setAdapter(new PeopleAdapter(fragment, people));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_people_list, container, false);
        PeopleListFragment.fillListView(this, root.findViewById(R.id.list_people), Data.people);
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
        inflater.inflate(R.menu.people_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_person:
                Bundle b = new Bundle();
                b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
                b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PersonDetailFragment.class.getName());
                mListener.onFragmentInteraction(b);
        }
        return super.onOptionsItemSelected(item);
    }
}

class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.ViewHolder> {
    private final List<Person> dataset;
    private final MySuperFragment fragment;

    // Provide a suitable constructor (depends on the kind of dataset)
    PeopleAdapter(MySuperFragment fragment, List<Person> dataset) {
        this.fragment = fragment;
        this.dataset = dataset;
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
        holder.setPerson(fragment, dataset.get(position));
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
        private final TextView mPersonTitle;
        private final TextView mPersonSubtitle;
        private final CardView mPersonCard;

        ViewHolder(CardView v) {
            super(v);
            mPersonCard = v;
            mPersonTitle = v.findViewById(R.id.person_title);
            mPersonSubtitle = v.findViewById(R.id.person_subtitle);
        }

        private void setPerson(MySuperFragment fragment, Person person) {
            this.mPersonTitle.setText(person.name);
            this.mPersonSubtitle.setText(person.surname);
//                Intent i = new Intent(Constants.ACTION_TRIP_SELECTED);
//                i.putExtra(Constants.EXTRA_TRIP_ID, trip.id);
            Bundle b = new Bundle();
            Bundle bb = new Bundle();
            bb.putInt(Constants.EXTRA_PERSON_ID, person.id);
            b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
            b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
            b.putString(Constants.EXTRA_REPLACE_FRAGMENT, PersonDetailFragment.class.getName());
            mPersonCard.setOnClickListener(
                    view -> {
                        fragment.mListener.onFragmentInteraction(b);
                        // Suggested way is using interfaces
                        // LocalBroadcastManager.getInstance(mPersonCard.getContext()).sendBroadcast(i);

                        // No because it recreates the activity
                        // mPersonCard.getContext().startActivity(i);
                    });
        }
    }
}