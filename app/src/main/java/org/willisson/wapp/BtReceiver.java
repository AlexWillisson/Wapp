package org.willisson.wapp;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by pace on 11/27/15.
 */
public class BtReceiver extends BroadcastReceiver {
    public void onReceive (Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.i("WAPP", "BT connected " + device);

        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.i("WAPP", "BT gone " + device);
        }
    }
}
