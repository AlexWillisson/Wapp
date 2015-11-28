package org.willisson.wapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;

public class RootActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);
    }

    public void start_alex (View view) {
		Intent intent = new Intent (this, AlexActivity.class);
		startActivity(intent);
    }

    public void start_pace (View view) {
        Intent intent = new Intent (this, PaceActivity.class);
        startActivity(intent);
    }

    public void start_bt (View view) {
        Intent intent = new Intent (this, BtActivity.class);
        startActivity(intent);
    }

    public void start_hall(View v) {
		Intent intent = new Intent(this, HallEffectActivity.class);
		startActivity(intent);
    }
}
