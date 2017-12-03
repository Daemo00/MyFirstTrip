package com.daemo.myfirsttrip.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.daemo.myfirsttrip.R;
import com.daemo.myfirsttrip.common.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Arrays;
import java.util.List;

public abstract class DetailFragment extends MySuperFragment implements EventListener<DocumentSnapshot>, OnCompleteListener<Void> {
    private DocumentReference itemRef;
    private ListenerRegistration listenerRegistration;
    private DetailFragmentMode currStatus;
    private ViewPager mViewPager;
    private SubListsPagerAdapter mPagerAdapter;


    abstract DocumentReference getItemRef(String itemId);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeByArgs(getArguments());
    }

    private void initializeByArgs(Bundle args) {
        if (args == null || args.isEmpty()) {
            // From items list, add an item
            currStatus = DetailFragmentMode.NEW;
            needsRefreshLayout = false;
            createDraftItemFromRef(null, task -> {
                if (task.getException() != null) {
                    getMySuperActivity().showToast(task.getException().getMessage());
                    return;
                }
                itemRef = task.getResult();
                if (listenerRegistration == null)
                    listenerRegistration = itemRef.addSnapshotListener(DetailFragment.this);
            });
            return;
        }

        if (args.containsKey(getExtraItemId())
                && args.containsKey(Constants.EXTRA_EDIT)
                && args.getBoolean(Constants.EXTRA_EDIT)) {
            // Click on edit from the item
            currStatus = DetailFragmentMode.EDIT;
            needsRefreshLayout = true;
            createDraftItemFromRef(getItemRef(args.getString(getExtraItemId())), task -> {
                if (task.getException() != null) {
                    getMySuperActivity().showToast(task.getException().getMessage());
                    return;
                }
                itemRef = task.getResult();
                if (listenerRegistration == null)
                    listenerRegistration = itemRef.addSnapshotListener(DetailFragment.this);
            });
            return;
        }

        if (args.containsKey(getExtraItemId())) {
            // Simply click on an item to see details
            currStatus = DetailFragmentMode.VIEW;
            needsRefreshLayout = true;
            itemRef = getItemRef(args.getString(getExtraItemId()));
        }
    }

    abstract void createDraftItemFromRef(DocumentReference itemDocReference, OnCompleteListener<DocumentReference> listener);

    protected abstract String getExtraItemId();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = null;
        switch (currStatus) {
            case EDIT:
            case NEW:
                root = inflater.inflate(getEditLayout(), container, false);
                break;
            case VIEW:
                root = inflater.inflate(getLayout(), container, false);
                break;
        }
        if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            viewGroup.addView(root);
            return viewGroup;
        }
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (listenerRegistration == null && itemRef != null)
            listenerRegistration = itemRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        switch (currStatus) {
            case EDIT:
            case NEW:
                inflater.inflate(getMenuEdit(), menu);
                break;
            case VIEW:
                inflater.inflate(getMenu(), menu);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == getClearMenuItem()) {
            cleanData();
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null)
                fragmentManager.popBackStack();
            return true;
        } else if (itemId == getConfirmMenuItem()) {
            confirmItem();
            return true;
        } else if (itemId == getEditMenuItem()) {
            if (isItemDraft())
                getMySuperActivity().showOkCancelDialog("Confirm",
                        "Confirm the current modifications before choosing other tripsIds",
                        (dialogInterface, i) -> confirmItem());
            else
                editItem();
            return true;
        } else if (itemId == getChooseMenuItem1()) {
            chooseItem1();
            return true;
        } else if (itemId == getChooseMenuItem2()) {
            chooseItem2();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected abstract int getChooseMenuItem2();

    protected abstract boolean isItemDraft();

    private void chooseItem1() {
        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putBoolean(Constants.EXTRA_CHOOSE, true);
        bb.putString(getExtraItemId(), getItemId());
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, getListFragmentName1());
        mListener.onFragmentInteraction(b);
    }

    private void chooseItem2() {
        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putBoolean(Constants.EXTRA_CHOOSE, true);
        bb.putString(getExtraItemId(), getItemId());
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, getListFragmentName2());
        mListener.onFragmentInteraction(b);
    }

    protected abstract String getListFragmentName2();

    protected abstract String getItemId();

    @Override
    public boolean allowBackPress() {
        if (isItemSet() && isItemDraft()) {
            getMySuperActivity().showOkCancelDialog("Confirm",
                    "Confirm the current modifications or cancel them",
                    (dialogInterface, i) -> confirmItem(),
                    null,
                    (dialogInterface, i) -> {
                        FragmentManager fragmentManager = getFragmentManager();
                        if (fragmentManager != null)
                            fragmentManager.popBackStack();
                    });
            return false;
        }
        return super.allowBackPress();
    }

    protected abstract boolean isItemSet();

    private void confirmItem() {
        //TODO validation
        View root = getView();
        if (root != null)
            setItemDetails(root);

        setRefreshing(true);
        commitItem(task -> setRefreshing(false));
        // needed, otherwise this fragment isn't correctly removed
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.popBackStack();
            fragmentManager.popBackStack();
        }

        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putString(getExtraItemId(), getItemId());
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, getDetailFragmentName());
        mListener.onFragmentInteraction(b);
    }

    private void editItem() {
        Bundle b = new Bundle();
        Bundle bb = new Bundle();
        bb.putString(getExtraItemId(), getItemId());
        bb.putBoolean(Constants.EXTRA_EDIT, true);
        b.putBundle(Constants.EXTRA_BUNDLE_FOR_FRAGMENT, bb);
        b.putBoolean(Constants.EXTRA_ADD_TO_BACKSTACK, true);
        b.putString(Constants.EXTRA_REPLACE_FRAGMENT, getDetailFragmentName());
        mListener.onFragmentInteraction(b);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = view.findViewById(R.id.pager);
        TabLayout tabLayout = view.findViewById(R.id.pager_tabs);
        tabLayout.setupWithViewPager(mViewPager);
        switch (currStatus) {
            case NEW:
            case EDIT:
            case VIEW:
                setRefreshing(true);
                break;
        }
    }

    private void loadEditLayout(@Nullable View view) {
        if (view == null) return;
        setEditViewDetails(view);
    }

    protected abstract void setEditViewDetails(View view);

    private void loadLayout(@Nullable View view) {
        if (view == null) return;
        setViewDetails(view);
    }

    @Override
    public void onRefresh() {
        mPagerAdapter.refreshLists();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanData();
    }

    private void cleanData() {
        if (isItemSet() && isItemDraft())
            deleteItem(this);
    }

    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            getMySuperActivity().showToast(e.getMessage());
            return;
        }

        if (!documentSnapshot.exists()) {
            setRefreshing(false);
            getMySuperActivity().showToast("Snapshot doesn't exist yet");
            return;
        }

        setItem(documentSnapshot);
        switch (currStatus) {
            case EDIT:
            case NEW:
                if (!isItemDraft())
                    getMySuperActivity().showToast("This should be a draft");
                loadEditLayout(getView());
                break;
            case VIEW:
                loadLayout(getView());
                break;
        }
        mPagerAdapter = new SubListsPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        setRefreshing(false);
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.getException() != null)
            getMySuperActivity().showToast(task.getException().getMessage());
        setRefreshing(false);
    }

    protected abstract int getLayout();

    protected abstract int getEditLayout();

    protected abstract int getMenuEdit();

    protected abstract int getMenu();

    protected abstract int getChooseMenuItem1();

    protected abstract int getEditMenuItem();

    protected abstract int getConfirmMenuItem();

    protected abstract int getClearMenuItem();

    protected abstract String getListFragmentName1();

    protected abstract void setItemDetails(View view);

    protected abstract void commitItem(OnCompleteListener<Void> listener);

    protected abstract String getDetailFragmentName();

    protected abstract void setViewDetails(@NonNull View view);

    protected abstract void deleteItem(OnCompleteListener<Void> listener);

    protected abstract void setItem(DocumentSnapshot documentSnapshot);

    private class SubListsPagerAdapter extends FragmentPagerAdapter {
        private final Bundle b = new Bundle();
        private final List<ListFragment> listFragments;

        private SubListsPagerAdapter(FragmentManager fm) {
            super(fm);
            switch (currStatus) {
                case EDIT:
                case NEW:
                    b.putBoolean(Constants.EXTRA_EDIT, true);
                case VIEW:
                    b.putString(getExtraItemId(), DetailFragment.this.getItemId());
                    break;
            }
            listFragments = Arrays.asList(
                    (ListFragment) Fragment.instantiate(getContext(), getListFragmentName1(), b),
                    (ListFragment) Fragment.instantiate(getContext(), getListFragmentName2(), b)
            );
        }

        @Override
        public Fragment getItem(int position) {
            return listFragments.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return listFragments.get(position).title;
        }

        @Override
        public int getCount() {
            return 2;
        }

        void refreshLists() {
            for (ListFragment listFragment : listFragments)
                listFragment.mAdapter.setQuery(listFragment.mAdapter.mQuery);
        }
    }
}
