package org.willisson.wapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.Manifest;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;

public class LocationActivity extends AppCompatActivity {

    LocationManager loc_man;
    LocationListener cell_listener;
    LocationListener gps_listener;

    SharedPreferences prefs;
    SharedPreferences.Editor prefs_editor;
    Gson gson;

    private final int PERMISSIONS = 1;

    private Location loc;
    private String location_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location);

	if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext()) != ConnectionResult.SUCCESS) {
	    send_toast("Google Play Services not found.");
	    finish();
	}

	prefs = getPreferences(MODE_PRIVATE);
	prefs_editor = prefs.edit();
	gson = new Gson();

	if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
	    != PackageManager.PERMISSION_GRANTED) {
	    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
		send_toast("Allow location permission");
		finish();
	    } else {
		requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS);
	    }
	} else {
	    get_location();
	}

	/*
	prefs_editor.putString("foo", "bar");
	prefs_editor.commit();
	prefs_editor.putString("foo", "bar");
	prefs_editor.commit();
	prefs_editor.putString("foo", "bar");
	prefs_editor.commit();
	prefs_editor.putString("foo", "bar");
	prefs_editor.commit();
	*/
    }

    public void onRequestPermissionsResult(int request_code, String[] permissions, int[] grantResults) {
	super.onRequestPermissionsResult(request_code, permissions, grantResults);
	if (request_code == PERMISSIONS) {
	    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
		get_location();
	    }
	}
    }

    private void show_location(TextView tv, Location location, String label) {
	tv.setText(label + " " + location.getLatitude() + ", " + location.getLongitude() + " (" + location.getAccuracy() + ")");
    }

    private void get_location() {
	final TextView text_view = (TextView) findViewById(R.id.output);

	loc_man = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	cell_listener = new LocationListener() {
		public void onLocationChanged(Location location) {
		    location_type = "Cell";
		    show_location(text_view, location, location_type);
		    loc = location;
		}
		public void onStatusChanged(String provider, int status, Bundle extras) { }
		public void onProviderEnabled(String provider) { }
		public void onProviderDisabled(String provider) { }
	    };
	gps_listener = new LocationListener() {
		public void onLocationChanged(Location location) {
		    loc_man.removeUpdates(cell_listener);
		    location_type = "GPS";
		    show_location(text_view, location, location_type);
		    loc = location;
		}
		public void onStatusChanged(String provider, int status, Bundle extras) { }
		public void onProviderEnabled(String provider) { }
		public void onProviderDisabled(String provider) { }
	    };
	loc_man.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, cell_listener);
	loc_man.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gps_listener);
    }

    private void clear_location_listeners() {
	if (loc_man != null) {
	    if (cell_listener != null) {
		loc_man.removeUpdates(cell_listener);
	    }
	    if (gps_listener != null) {
		loc_man.removeUpdates(gps_listener);
	    }
	}
    }

    protected void onPause() {
	super.onPause();
	clear_location_listeners();
    }
    protected void onStop() {
	super.onStop();
	clear_location_listeners();
    }

    public void send_toast (String text) {
	Context context = getApplicationContext ();
	int duration = Toast.LENGTH_SHORT;
	Toast toast = Toast.makeText (context, text, duration);
	toast.show ();
    }

    public void save_location(View v) {
	if (loc == null) {
	    send_toast("No location found yet.");
	} else {
	    String json = gson.toJson(loc);
	    prefs_editor.putString("location", json);
	    prefs_editor.putString("type", location_type);
	    prefs_editor.commit();
	}
    }

    public void load_location(View v) {
	String json = prefs.getString("location", "");
	if (json != "") {
	    Location location = gson.fromJson(json, Location.class);
	    show_location((TextView)findViewById(R.id.saved), location, prefs.getString("type", ""));
	} else {
	    send_toast("No saved location found.");
	}
    }

    public void see_location(View v) {
	String json = prefs.getString("location", "");
	if (json != "") {
	    Location location = gson.fromJson(json, Location.class);
	    Uri place_uri = Uri.parse("geo:" + location.getLatitude() + "," + location.getLongitude() + "?z=21");
	    Intent map_intent = new Intent(Intent.ACTION_VIEW, place_uri);
	    startActivity(map_intent);
	} else {
	    send_toast("No saved location found.");
	}
    }
}
