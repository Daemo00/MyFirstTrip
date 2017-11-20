package com.daemo.myfirsttrip.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import com.daemo.myfirsttrip.MyRefreshing;
import com.daemo.myfirsttrip.MySuperActivity;
import com.daemo.myfirsttrip.R.id;
import com.daemo.myfirsttrip.common.Utils;

public abstract class MySuperFragment extends Fragment implements MyRefreshing {
    public final String title = Utils.getTag(this);
    public MySuperFragment.OnFragmentInteractionListener mListener;
    boolean needsRefreshLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    public MySuperFragment() {
        Log.d(Utils.getTag(this), "Called constructor");
    }

    public MySuperActivity getMySuperActivity() {
        if (getActivity() instanceof MySuperActivity) return (MySuperActivity) getActivity();
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(Utils.getTag(this), "onCreate, arguments is " + getArguments());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (needsRefreshLayout) {
            swipeRefreshLayout = generateSwipeRefreshLayout();
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                viewGroup.addView(swipeRefreshLayout);
                return viewGroup;
            }
            return swipeRefreshLayout;
        }
        return view;
    }

    private SwipeRefreshLayout generateSwipeRefreshLayout() {
        Context context = getContext();
        if (context == null)
            return null;
        SwipeRefreshLayout swipeRefreshLayout = new SwipeRefreshLayout(getContext());
        swipeRefreshLayout.setId(id.swipe_layout_superFragment);
        swipeRefreshLayout.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        swipeRefreshLayout.setOnRefreshListener(this);
        return swipeRefreshLayout;
    }

    @Override
    public void setRefreshing(boolean isRefreshing) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(isRefreshing);
            MySuperActivity mySuperActivity = getMySuperActivity();
            if (mySuperActivity == null) return;
            if (isRefreshing) {
                mySuperActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            } else {
                mySuperActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
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

    public boolean allowBackPress() {
        return true;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Bundle bundle);
    }
}