package org.willisson.wapp;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by pace on 11/28/15.
 */
public class MyService extends IntentService {
    public MyService () {
        super("MyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("WAPP", "MyService.onHandleIntent");
        int count = 0;
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;

            Intent i2 = new Intent (PaceActivity.ACTION_UPDATE);
            i2.putExtra ("val", "count = " + count);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i2);

        }

    }

    void turn_on_screen () {
        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        PowerManager.WakeLock screenLock
                = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                    | PowerManager.ON_AFTER_RELEASE,
                                "WAPP");
        Log.i ("WAPP", "acquire wake lock");
        screenLock.acquire();

        Log.i ("WAPP", "release wake lock");
        screenLock.release();
    }
}
