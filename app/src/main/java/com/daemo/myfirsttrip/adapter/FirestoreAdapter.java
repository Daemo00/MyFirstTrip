package com.daemo.myfirsttrip.adapter;

import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.daemo.myfirsttrip.MyRefreshing;
import com.daemo.myfirsttrip.common.ItemTouchHelperAdapter;
import com.daemo.myfirsttrip.common.Utils;
import com.daemo.myfirsttrip.fragments.MySuperFragment;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * RecyclerView adapter for displaying the results of a Firestore {@link Query}.
 * <p>
 * Note that this class forgoes some efficiency to gain simplicity. For example, the result of
 * {@link DocumentSnapshot#toObject(Class)} is not cached so the same object may be deserialized
 * many times as the user scrolls.
 */
public abstract class FirestoreAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH>
        implements EventListener<QuerySnapshot>, ItemTouchHelperAdapter {
    private static final String TAG = Utils.getTag(FirestoreAdapter.class);
    public final String collection;
    public final ArrayList<DocumentSnapshot> mSnapshots = new ArrayList<>();
    public final Set<String> unselectedIds = new HashSet<>();
    final MySuperFragment fragment;
    final Set<Integer> selected_positions = new HashSet<>();
    public Set<String> selectedIds = new HashSet<>();
    public Query mQuery;
    boolean isChooseMode;
    boolean isClickable;
    MyRefreshing myRefreshing;
    boolean unique;
    private ListenerRegistration mRegistration;

    FirestoreAdapter(MySuperFragment fragment, Query query, Set<String> selectedIds, String collection) {
        this.fragment = fragment;
        this.myRefreshing = fragment;
        this.mQuery = query;
        this.selectedIds = selectedIds;
        this.collection = collection;
    }

    @Override
    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "onEvent:error", e);
            onError(e);
            return;
        }
        onDataChanging();
        // Dispatch the event
        Log.d(TAG, "onEvent:numChanges:" + documentSnapshots.getDocumentChanges().size());
        for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
            switch (change.getType()) {
                case ADDED:
                    onDocumentAdded(change);
                    break;
                case MODIFIED:
                    onDocumentModified(change);
                    break;
                case REMOVED:
                    onDocumentRemoved(change);
                    break;
            }
        }

        onDataChanged();
    }

    public void startListening() {
        if (mQuery != null && mRegistration == null) {
            mRegistration = mQuery.addSnapshotListener(this);
        }
    }

    private void stopListening() {
        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }

        mSnapshots.clear();
        notifyDataSetChanged();
    }

    public void setQuery(Query query) {
        // Stop listening
        stopListening();

        // Clear existing data
        mSnapshots.clear();
        notifyDataSetChanged();

        // Listen to new query
        mQuery = query;
        startListening();
    }

    @Override
    public int getItemCount() {
        return mSnapshots.size();
    }

    DocumentSnapshot getSnapshot(int index) {
        return mSnapshots.get(index);
    }

    private void onDocumentAdded(DocumentChange change) {
        mSnapshots.add(change.getNewIndex(), change.getDocument());
        notifyItemInserted(change.getNewIndex());
    }

    private void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            mSnapshots.set(change.getOldIndex(), change.getDocument());
            notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            mSnapshots.remove(change.getOldIndex());
            mSnapshots.add(change.getNewIndex(), change.getDocument());
            notifyItemMoved(change.getOldIndex(), change.getNewIndex());
        }
    }

    private void onDocumentRemoved(DocumentChange change) {
        mSnapshots.remove(change.getOldIndex());
        notifyItemRemoved(change.getOldIndex());
    }

    private void onError(Exception e) {
        fragment.getMySuperActivity().showToast(e.getMessage());
        e.printStackTrace();
    }

    private void onDataChanging() {
        myRefreshing.setRefreshing(true);
    }

    private void onDataChanged() {
        myRefreshing.setRefreshing(false);
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
                    deleteItem(mSnapshots.get(position));
                }
            }
        });
    }

    abstract void deleteItem(DocumentSnapshot itemSnapshot);

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition)
            for (int i = fromPosition; i < toPosition; i++)
                Collections.swap(mSnapshots, i, i + 1);
        else
            for (int i = fromPosition; i > toPosition; i--)
                Collections.swap(mSnapshots, i, i - 1);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void setChooseMode(boolean isChooseMode) {
        this.isChooseMode = isChooseMode;
        this.isClickable = !isChooseMode;
    }

    public void setClickable(boolean clickable) {
        this.isClickable = clickable;
    }

    public void setMyRefreshing(MyRefreshing myRefreshing) {
        this.myRefreshing = myRefreshing;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }
}
