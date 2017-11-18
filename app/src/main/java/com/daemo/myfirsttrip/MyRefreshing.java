package com.daemo.myfirsttrip;

import android.support.v4.widget.SwipeRefreshLayout;

public interface MyRefreshing extends SwipeRefreshLayout.OnRefreshListener {

    void setRefreshing(boolean isRefreshing);
}
