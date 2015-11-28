package org.willisson.wapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class PaceActivity extends AppCompatActivity {
    public MulticastSocket socket;
    public Thread timer, rcv_thread;
    public boolean keep_going;
    public WifiManager.MulticastLock multicast_lock;
    public TextView last_msg_textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pace);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        last_msg_textview = (TextView)findViewById (R.id.last_msg);
        Log.i("WAPP", "last_msg_textview " + last_msg_textview);

        LocalBroadcastManager mgr = LocalBroadcastManager.getInstance (this);
        IntentFilter filter = new IntentFilter();
        filter.addAction (ACTION_UPDATE);
        mgr.registerReceiver(brcv, filter);

        rcv_thread_setup();
    }

    @Override
    protected void onDestroy() {
        keep_going = false;
        super.onDestroy();

    }
    public static final String ACTION_UPDATE = "org.willisson.wapp.ACTION_UPDATE";

    private BroadcastReceiver brcv = new BroadcastReceiver () {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals (ACTION_UPDATE)) {
                String val = intent.getStringExtra ("val");
                Log.i ("WAPP", "got update intent: " + val);
            }
        }
    };

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

            runOnUiThread (new Runnable () {
                public void run () {
                    last_msg_textview.setText (rmsg);
                }
            });

        } catch (Exception e) {
            Log.i ("WAPP", "rcv_step error " + e);
        }
    }

    public void start_service(View view) {
        Log.i ("WAPP", "start_service");
        Intent intent = new Intent (this, MyService.class);
        startService (intent);
    }

}

