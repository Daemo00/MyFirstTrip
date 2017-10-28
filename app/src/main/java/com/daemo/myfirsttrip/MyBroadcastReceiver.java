package com.daemo.myfirsttrip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.daemo.myfirsttrip.common.Utils;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Utils.getTag(this), Utils.debugIntent(intent));

    }
}
