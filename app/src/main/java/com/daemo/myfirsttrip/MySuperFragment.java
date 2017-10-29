package com.daemo.myfirsttrip;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.daemo.myfirsttrip.R.id;
import com.daemo.myfirsttrip.common.Utils;

import java.util.Arrays;

public class MySuperFragment extends Fragment implements OnRefreshListener {
    final String title = Utils.getTag(this);
    MySuperFragment.OnFragmentInteractionListener mListener;

    public MySuperFragment() {
        Log.d(Utils.getTag(this), "Called constructor");
    }

    MySuperActivity getMySuperActivity() {
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
        SwipeRefreshLayout swipeRefreshLayout = getSwipeRefreshLayout();
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            viewGroup.addView(swipeRefreshLayout);
            return viewGroup;
        }

        return swipeRefreshLayout;
    }

    private SwipeRefreshLayout getSwipeRefreshLayout() {
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
    public void onRefresh() {
        stopRefreshing();
    }

    private void stopRefreshing() {
        View view = getView();
        if (view != null) {
            View layout = view.findViewById(id.swipe_layout_superFragment);
            if (layout instanceof SwipeRefreshLayout) {
                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) layout;
                swipeRefreshLayout.setRefreshing(false);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(Utils.getTag(this), "onRequestPermissionsResult(" + requestCode + ", " + Arrays.toString(permissions) + ", " + Arrays.toString(grantResults) + ")");
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
    interface OnFragmentInteractionListener {
        void onFragmentInteraction(Bundle bundle);
    }
}