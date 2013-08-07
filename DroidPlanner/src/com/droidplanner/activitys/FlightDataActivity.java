package com.droidplanner.activitys;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperFlightActivity;
import com.droidplanner.fragments.FlightMapFragment;
import com.droidplanner.helpers.RcOutput;

public class FlightDataActivity extends SuperFlightActivity implements
		OnWaypointUpdateListner, OnClickListener, OnTouchListener {
	
	private Button arm, disarm, launch, land, rtl, stabilize;
//	private OnTouchListener armListen;
	private RcOutput rcOutput;

	@Override
	public int getNavigationItem() {
		return 1;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.flightdata);

		mapFragment = ((FlightMapFragment) getFragmentManager()
				.findFragmentById(R.id.flightMapFragment));
		mapFragment.updateFragment();

		drone.mission.missionListner = this;
		drone.setDroneTypeChangedListner(this);
		drone = app.drone;
		rcOutput = new RcOutput(drone, this);
		arm = (Button) findViewById(R.id.arm);
		disarm = (Button) findViewById(R.id.disarm);
		launch = (Button) findViewById(R.id.launch);
		land = (Button) findViewById(R.id.land);
		rtl = (Button) findViewById(R.id.rtl);
		stabilize = (Button) findViewById(R.id.stabilize);
		arm.setOnTouchListener(this);
		disarm.setOnTouchListener(this);
		launch.setOnTouchListener(this);
		land.setOnClickListener(this);
		rtl.setOnClickListener(this);
		stabilize.setOnClickListener(this);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_flightdata, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_zoom:
			mapFragment.zoomToLastKnowPosition();
			return true;
		case R.id.menu_clearFlightPath:
			mapFragment.clearFlightPath();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.equals(launch)) {
			simulateLaunchEvent(event);
		} else if (v.equals(arm)) {
			rcOutput.simulateArmEvent(event);
		} else if (v.equals(disarm)) {
			rcOutput.simulateDisarmEvent(event);
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v.equals(rtl)) {
			OnModeSpinnerSelected("RTL");
		} else if (v.equals(stabilize)) {
			OnModeSpinnerSelected("Stabilize");
		} else if (v.equals(land)){
//			setLandPoint(new waypoint(drone.getPosition(), 0.0));
		}
		
	}
	
	private void simulateLaunchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
//			setLaunchPoint(new waypoint(drone.getPosition(),drone.defaultAlt));
			rcOutput.enableRcOverride();
			rcOutput.setRcChannel(RcOutput.TROTTLE, 1);	
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			rcOutput.disableRcOverride();
		}
	}

}
