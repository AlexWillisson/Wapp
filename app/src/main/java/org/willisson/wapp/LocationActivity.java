package org.willisson.wapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class LocationActivity extends AppCompatActivity {

    LocationManager loc_man;
    LocationListener cell_listener;
    LocationListener gps_listener;

    private final int PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location);

	if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext()) != ConnectionResult.SUCCESS) {
	    send_toast("Google Play Services not found.");
	    finish();
	}

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
    }

    public void onRequestPermissionsResult(int request_code, String[] permissions, int[] grantResults) {
	super.onRequestPermissionsResult(request_code, permissions, grantResults);
	if (request_code == PERMISSIONS) {
	    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
		get_location();
	    }
	}
    }

    private void get_location() {
	final TextView text_view = (TextView) findViewById(R.id.output);

	loc_man = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	cell_listener = new LocationListener() {
		public void onLocationChanged(Location location) {
		    text_view.setText("Cell " + location.getLatitude() + ", " + location.getLongitude() + " (" + location.getAccuracy() + ")");
		}
		public void onStatusChanged(String provider, int status, Bundle extras) { }
		public void onProviderEnabled(String provider) { }
		public void onProviderDisabled(String provider) { }
	    };
	gps_listener = new LocationListener() {
		public void onLocationChanged(Location location) {
		    loc_man.removeUpdates(cell_listener);
		    text_view.setText("GPS " + location.getLatitude() + ", " + location.getLongitude() + " (" + location.getAccuracy() + ")");
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
}
