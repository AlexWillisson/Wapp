package org.willisson.wapp;

import android.app.NotificationManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.gms.maps.model.LatLng;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

public class AlexActivity extends AppCompatActivity {
    public MulticastSocket socket;
    public Thread timer, rcv_thread;
    public boolean keep_going, multicast_active;
    public WifiManager.MulticastLock multicast_lock;
    public TextView last_msg_textview;
	public AppCompatActivity alex_activity;
	NotificationManager nm;
	PowerManager pm;
	PowerManager.WakeLock wl;
	String last_notification;
	SharedPreferences prefs;
	SharedPreferences.Editor prefs_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alex);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		nm = (NotificationManager) getSystemService (Context.NOTIFICATION_SERVICE);
		alex_activity = this;
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		multicast_active = false;
		last_notification = "";
		prefs = getSharedPreferences ("ATW", MODE_PRIVATE);
		prefs_editor = prefs.edit ();

		if (prefs.getBoolean ("initialized", false) == false) {
			send_toast ("initializing");

			prefs_editor.putFloat ("13:34lat", 42);
			prefs_editor.putFloat ("13:34lon", -71);

			prefs_editor.putFloat ("13:44lat", 43);
			prefs_editor.putFloat ("13:44lon", -71);

			prefs_editor.putFloat ("13:54lat", 43);
			prefs_editor.putFloat ("13:54lon", -70);

			prefs_editor.putFloat ("14:04lat", 42);
			prefs_editor.putFloat ("14:04lon", -70);

			prefs_editor.putBoolean ("initialized", true);
			prefs_editor.commit ();
		}
    }

	public void save_stuff (View view) {
		send_toast ("saving :'" + last_notification);
		prefs_editor.putString("last_notification", last_notification);
		prefs_editor.commit();
	}

	public void load_stuff (View view) {
		String s = prefs.getString ("last_notification",
									"THIS SPACE INTENTIONALLY LEFT BLANK");
		send_toast (s);
	}

	public void dump_loclog (View view) {
		int hr, min, idx;
		String time, latkey, lonkey;
        float lat, lon;
		ArrayList<LocLog> hist;
		LocLog node;

		hist = new ArrayList<LocLog> ();

		for (hr = 0; hr < 24; hr++) {
			for (min = 0; min < 60; min++) {
				time = hr + ":" + String.format ("%02d", min);
				latkey = time + "lat";
				lonkey = time + "lon";

				lat = prefs.getFloat (latkey, 0);
				lon = prefs.getFloat (lonkey, 0);

				if (lat != 0 && lon != 0) {
					hist.add (new LocLog (new LatLng (lat, lon), time));
				}
			}
		}

		for (idx = 0; idx < hist.size (); idx++) {
			node = hist.get (idx);
			Log.i ("WAPP", "loc: " + node.loc + ", time: " + node.tag);
		}
	}

	public void start_multicast (View view) {
		if (multicast_active == false) {
			multicast_active = true;
			rcv_thread_setup ();
			wl = pm.newWakeLock (PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "wltag");
		}
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
		last_notification = "bar baz";
	}

	public void sample_notification2 (View view) {
		Notification.Builder builder = new Notification.Builder (this)
			.setSmallIcon (R.mipmap.ic_launcher)
			.setContentTitle ("bar")
			.setContentText ("quuuuux")
			.setOngoing (true);
		Notification notification = builder.build ();
		nm.notify (42, notification);	
		last_notification = "quuuuux";
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
		if (wl != null && wl.isHeld ()) {
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
					last_notification = rmsg;
                }
            });

        } catch (Exception e) {
            Log.i ("WAPP", "rcv_step error " + e);
        }
    }
}

class LocLog {
	public LatLng loc;
	public String tag;

	public LocLog (LatLng latlng, String title) {
		loc = latlng;
		tag = title;
	}
}
