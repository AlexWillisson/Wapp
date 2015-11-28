package org.willisson.wapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by pace on 11/27/15.
 */
public class BootReceiver extends BroadcastReceiver {
    public void onReceive (Context context, Intent intent) {
        String action = intent.getAction();
        Log.i("WAPP", "boot complete intent " + action);
    }
}
