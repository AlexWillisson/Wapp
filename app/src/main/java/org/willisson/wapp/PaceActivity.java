package org.willisson.wapp;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class PaceActivity extends AppCompatActivity {
    public DatagramSocket socket;
    public Thread timer;
    public boolean timer_keep_running;

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

        timer_setup();


    }

    @Override
    protected void onDestroy() {
        timer_keep_running = false;
        super.onDestroy();

    }

    void timer_setup() {
        Log.i("WAPP", "timer_setup");
        if (timer == null) {
            Log.i ("WAPP", "creating new timer");
            timer = new Thread() {
                public void run() {
                    udp_setup ();

                    try {
                        while (timer_keep_running) {
                            Log.i("WAPP", "tick");
                            udp_tick ();
                            Thread.sleep(1000);
                        }
                    } catch (Exception e) {
                        Log.i("WAPP", "thread error " + e);
                    }
                    Log.i ("WAPP", "timer done");
                }
            };
            Log.i ("WAPP", "starting timer");
            timer_keep_running = true;
            timer.start();
        }
    }

    void udp_setup () {
        Log.i ("WAPP", "doing udp_setup");
        try {
            WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
            WifiManager.MulticastLock lock = wifi.createMulticastLock ("WAPP");
            lock.acquire();
            Log.i("WAPP", "got lock");

            if (socket == null) {
                Log.i ("WAPP", "creating multicast socket");
                socket = new MulticastSocket(20151);

                InetSocketAddress addr;
                addr = new InetSocketAddress("224.0.0.1", 20151);
                socket.connect(addr);
                if (socket.getLocalAddress().getAddress()[0] == 10) {
                    Log.i ("WAPP", "setup for emulator");
                    socket.close();
                    socket = new DatagramSocket(20151);
                    addr = new InetSocketAddress("192.168.1.157", 20151);
                    socket.connect(addr);
                }
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

}

