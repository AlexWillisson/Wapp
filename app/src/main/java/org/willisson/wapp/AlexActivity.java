package org.willisson.wapp;

import android.app.NotificationManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class AlexActivity extends AppCompatActivity {
	NotificationManager nm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alex);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		nm = (NotificationManager) getSystemService (Context.NOTIFICATION_SERVICE);
    }

	public void toast_btn (View view) {
		send_toast("hello, world");
	}

	public void sample_notification1 (View view) {
		send_toast ("notifying");
		Notification.Builder builder = new Notification.Builder (this)
			.setSmallIcon (R.mipmap.ic_launcher)
			.setContentTitle ("foo")
			.setContentText ("bar baz")
			.setOngoing (true);
		Notification notification = builder.build ();
		nm.notify (42, notification);	
		Log.i ("WAPP", "notified!");
	}

	public void sample_notification2 (View view) {
		send_toast ("notifying");
		Notification.Builder builder = new Notification.Builder (this)
			.setSmallIcon (R.mipmap.ic_launcher)
			.setContentTitle ("bar")
			.setContentText ("quuuuux")
			.setOngoing (true);
		Notification notification = builder.build ();
		nm.notify (42, notification);	
		Log.i ("WAPP", "notified!");
	}

	public void send_toast (String text) {
		Context context = getApplicationContext ();
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText (context, text, duration);
		toast.show();
	}
}
