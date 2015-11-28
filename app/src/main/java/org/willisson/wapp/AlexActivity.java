package org.willisson.wapp;

import android.app.NotificationManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class AlexActivity extends AppCompatActivity {
    public MulticastSocket socket;
    public Thread timer, rcv_thread;
    public boolean keep_going;
    public WifiManager.MulticastLock multicast_lock;
    public TextView last_msg_textview;
	public AppCompatActivity alex_activity;
	NotificationManager nm;
	PowerManager pm;
	PowerManager.WakeLock wl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alex);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		nm = (NotificationManager) getSystemService (Context.NOTIFICATION_SERVICE);
		alex_activity = this;
        rcv_thread_setup ();
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock (PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "wltag");
    }

	public void toast_btn (View view) {
		send_toast("hello, world");
	}

	public void sample_notification1 (View view) {
		Notification.Builder builder = new Notification.Builder (this)
			.setSmallIcon (R.mipmap.ic_launcher)
			.setContentTitle ("foo")
			.setContentText ("bar baz")
			.setOngoing (true);
		Notification notification = builder.build ();
		nm.notify(42, notification);
	}

	public void sample_notification2 (View view) {
		Notification.Builder builder = new Notification.Builder (this)
			.setSmallIcon (R.mipmap.ic_launcher)
			.setContentTitle ("bar")
			.setContentText ("quuuuux")
			.setOngoing (true);
		Notification notification = builder.build ();
		nm.notify (42, notification);	
	}

	public void send_toast (String text) {
		Context context = getApplicationContext ();
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText (context, text, duration);
		toast.show();
	}

	public void to_map (View view) {
		Intent intent = new Intent (this, MapsActivity.class);
		startActivity(intent);
	}

    @Override
    protected void onDestroy() {
        keep_going = false;
		if (wl.isHeld ()) {
			wl.release ();
		}
        super.onDestroy();
    }

    void rcv_thread_setup() {
        Log.i("WAPP", "rcv_thread_setup");
        if (rcv_thread == null) {
            rcv_thread = new Thread() {
                public void run() {
                    udp_setup ();

                    try {
                        while (keep_going) {
                            rcv_step ();
                        }
                    } catch (Exception e) {
                        Log.i("WAPP", "rcv_thread error " + e);
                    }
                }
            };
            Log.i ("WAPP", "starting rcv_thread");
            keep_going = true;
            rcv_thread.start();
        }
    }

    void udp_setup () {
        Log.i ("WAPP", "doing udp_setup");
        try {


            if (socket == null) {
                Log.i ("WAPP", "creating multicast socket");
                socket = new MulticastSocket(20151);


                Log.i ("WAPP", "my socket " + socket.getLocalAddress());

                WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                multicast_lock = wifi.createMulticastLock ("WAPP");
                Log.i("APP", "about to lock: " + multicast_lock.isHeld());
                multicast_lock.acquire();
                Log.i("WAPP", "got lock: " + multicast_lock.isHeld() + " " + multicast_lock);

                InetAddress maddr = InetAddress.getByName ("224.0.0.1");
                socket.joinGroup(maddr);
            }
       } catch (Exception e) {
            Log.i("WAPP", "udp error " + e);
        }
    }
    public int tick_count;

    void udp_tick () {
        try {
            tick_count++;
            String msg = "hello " + socket.getLocalAddress() + " " + timer + " " + tick_count;
            byte[] xbytes = msg.getBytes();
            DatagramPacket xpkt = new DatagramPacket(xbytes, xbytes.length);
            socket.send(xpkt);
            Log.i("WAPP", "send done");
        } catch (Exception e) {
            Log.i ("WAPP", "udp_tick error" + e);
        }
    }

    void rcv_step () {
        try {
            byte[] rbuf = new byte[2000];
            DatagramPacket rpkt = new DatagramPacket(rbuf, rbuf.length);
            Log.i ("WAPP", "about to call receive");
            socket.receive(rpkt);
            final String rmsg = new String (rpkt.getData(), 0, rpkt.getLength(), "UTF-8");
            Log.i("WAPP", "rcv " + " " + rpkt.getSocketAddress() + " " + rmsg);
            Log.i("WAPP", "lock: " + multicast_lock.isHeld() + " " + multicast_lock);

			wl.acquire ();
			wl.release ();

            runOnUiThread (new Runnable() {
                public void run() {
                    Notification.Builder builder
                            = new Notification.Builder(alex_activity)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("pkt")
                            .setContentText(rmsg)
                            .setOngoing(true);
                    Notification notification = builder.build();
                    nm.notify(42, notification);
                }
            });

        } catch (Exception e) {
            Log.i ("WAPP", "rcv_step error " + e);
        }
    }
}
