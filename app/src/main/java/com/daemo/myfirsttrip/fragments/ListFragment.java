package com.daemo.myfirsttrip.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.daemo.myfirsttrip.MyRefreshing;
import com.daemo.myfirsttrip.R;
import com.daemo.myfirsttrip.adapter.FirestoreAdapter;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.common.SimpleItemTouchHelperCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public abstract class ListFragment extends MySuperFragment implements EventListener<DocumentSnapshot> {

    public FirestoreAdapter mAdapter;
    private DocumentReference docReference;
    private RecyclerView mRecyclerView;
    private ListenerRegistration listenerRegistration;
    private ListFragmentMode currStatus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null || args.isEmpty()) {
            currStatus = ListFragmentMode.ALL;
            needsRefreshLayout = true;
        } else if (args.containsKey(getExtraItemId())) {
            docReference = getDocReference(args);
            currStatus = ListFragmentMode.NESTED;
            needsRefreshLayout = false;
            if (args.containsKey(Constants.EXTRA_EDIT) && args.getBoolean(Constants.EXTRA_EDIT)) {
                currStatus = ListFragmentMode.NESTED_EDIT;
                needsRefreshLayout = false;
            } else if (args.containsKey(Constants.EXTRA_CHOOSE) && args.getBoolean(Constants.EXTRA_CHOOSE)) {
                currStatus = ListFragmentMode.CHOOSE;
                needsRefreshLayout = true;
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = inflater.inflate(getLayout(), container, false);
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
        mRecyclerView = view.findViewById(getList_id());
        switch (currStatus) {
            case CHOOSE:
            case NESTED:
            case NESTED_EDIT:
                // List will be updated when docReference is resolved
                break;
            case ALL:
                fillListView();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        switch (currStatus) {
            case CHOOSE:
            case NESTED:
            case NESTED_EDIT:
                if (listenerRegistration == null && docReference!= null)
                    listenerRegistration = docReference.addSnapshotListener(this);
                break;
            case ALL:
                break;
        }
    }

    @Override
    public void onRefresh() {
        if (mAdapter != null)
            mAdapter.setQuery(mAdapter.mQuery);
    }

    @Override
    public void onStop() {
        super.onStop();
        switch (currStatus) {
            case CHOOSE:
            case NESTED:
            case NESTED_EDIT:
                if (listenerRegistration != null) {
                    listenerRegistration.remove();
                    listenerRegistration = null;
                }
                break;
            case ALL:
                break;
        }
    }

    private void fillListView() {
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        Query query;
        Set<String> selected_ids = new HashSet<>();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        switch (currStatus) {
            case CHOOSE:
                if (isItemSet()) {
                    // Get all items and populate selected_ids
                    query = mFirestore.collection(getCollection())
                            .limit(Constants.QUERY_LIMIT);
                    selected_ids.addAll(getItemRelatedIds());
                    mAdapter = generateAdapter(query, selected_ids);
                }
                break;
            case NESTED:
            case NESTED_EDIT:
                if (isItemSet()) {
                    // Get only items in origItem
                    query = mFirestore.collection(getCollection())
                            .whereEqualTo(getNestedFilter(), 1)
                            .limit(Constants.QUERY_LIMIT);
                    mAdapter = generateAdapter(query, selected_ids);
                    mAdapter.setMyRefreshing((MyRefreshing) getParentFragment());
                }
                break;
            case ALL:
                // Get all the items!
                query = mFirestore.collection(getCollection())
                        .limit(Constants.QUERY_LIMIT);
                mAdapter = generateAdapter(query, selected_ids);
                // Allow edit on the list only if we are in the main list
                ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
                ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                touchHelper.attachToRecyclerView(mRecyclerView);
                break;
        }
        mAdapter.setChooseMode(currStatus.equals(ListFragmentMode.CHOOSE));
        mAdapter.setClickable(!currStatus.equals(ListFragmentMode.NESTED_EDIT));
        mAdapter.startListening();
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        switch (currStatus) {
            case CHOOSE:
                inflater.inflate(getMenu_choose(), menu);
                break;
            case NESTED:
            case NESTED_EDIT:
                break;
            case ALL:
                inflater.inflate(getMenuAll(), menu);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == getMenu_item_add()) {
            Bundle b = new Bundle();
            b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
            b.putString(Constants.EXTRA_REPLACE_FRAGMENT, getDetailFragmentName());
            mListener.onFragmentInteraction(b);
            return true;
        } else if (itemId == R.id.confirm_selection) {
            confirmSelection();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmSelection() {
        // Update the origItem with the selected items
        if (!getIsDraft())
            getMySuperActivity().showToast("Something went wrong, this should be a draft");
        Map<String, Integer> relatedIds = new HashMap<>();
        for (Object selected_id : mAdapter.selected_ids)
            if (selected_id instanceof String) {
                String selectedId = (String) selected_id;
                relatedIds.put(selectedId, 1);
            }
        setItemRelatedIds(relatedIds);

        setRefreshing(true);

        updateItem(task -> {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null)
                // Go back to whoever called this
                fragmentManager.popBackStack();
            else
                getMySuperActivity().showToast("Fragment manager not found");
            setRefreshing(false);
        });
    }

    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            getMySuperActivity().showToast(e.getMessage());
            return;
        }
        setItem(documentSnapshot);
        fillListView();
    }

    protected abstract Set<String> getItemRelatedIds();

    protected abstract void setItemRelatedIds(Map<String, Integer> selectedIds);

    protected abstract String getDetailFragmentName();

    protected abstract void updateItem(OnCompleteListener<Void> listener);

    protected abstract void setItem(DocumentSnapshot documentSnapshot);

    protected abstract String getExtraItemId();

    protected abstract DocumentReference getDocReference(Bundle args);

    protected abstract int getLayout();

    protected abstract int getList_id();

    protected abstract String getCollection();

    @NonNull
    protected abstract FirestoreAdapter generateAdapter(Query query, Set<String> selected_ids);

    protected abstract boolean isItemSet();

    abstract boolean getIsDraft();

    protected abstract String getNestedFilter();

    protected abstract int getMenu_choose();

    abstract int getMenuAll();

    protected abstract int getMenu_item_add();
}
