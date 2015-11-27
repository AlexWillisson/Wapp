package org.willisson.wapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class HallEffectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hall);

	if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS) {
	    ((TextView)findViewById(R.id.gms)).setText("Google Play Services found.");
	}

	final TextView text_view = (TextView) findViewById(R.id.output);

	SensorManager sm = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
	sm.registerListener(new SensorEventListener() {
		public void onAccuracyChanged(Sensor sensor, int accuracy) { }
		public void onSensorChanged(SensorEvent event) {
		    double strength = 0;
		    for (int i = 0; i < 3; i++) {
			strength += event.values[i] * event.values[i];
		    }
		    strength = Math.sqrt(strength);
		    text_view.setText(Double.toString(strength));
		}
	    }, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 100000);
    }
}
