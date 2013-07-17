package com.droidplanner.activitys;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.ardupilotmega.msg_set_mode;
import com.droidplanner.DroidPlannerApp.OnWaypointReceivedListner;
import com.droidplanner.R;
import com.droidplanner.MAVLink.Drone.DroneTypeListner;
import com.droidplanner.fragments.FlightMapFragment;
import com.droidplanner.fragments.FlightMapFragment.OnFlighDataListener;
import com.droidplanner.helpers.RcOutput;
import com.droidplanner.widgets.spinners.SelectModeSpinner;
import com.droidplanner.widgets.spinners.SelectModeSpinner.OnModeSpinnerSelectedListener;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner.OnWaypointSpinnerSelectedListener;
import com.google.android.gms.maps.model.LatLng;

public class FlightDataActivity extends SuperActivity implements
		OnFlighDataListener, OnWaypointSpinnerSelectedListener,
		OnWaypointReceivedListner, OnModeSpinnerSelectedListener,
		DroneTypeListner, OnTouchListener, OnClickListener {

	private FlightMapFragment flightMapFragment;
	private SelectModeSpinner fligthModeSpinner;
	private SelectWaypointSpinner wpSpinner;
	private LatLng guidedPoint;
	private Button launch, arm, disarm, rtl, stabilize;
	private RcOutput rcOutput;

	@Override
	int getNavigationItem() {
		return 1;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.flightdata);

		flightMapFragment = ((FlightMapFragment) getFragmentManager()
				.findFragmentById(R.id.flightMapFragment));
		flightMapFragment.updateMissionPath(drone);
		flightMapFragment.updateHomeToMap(drone);

		app.setWaypointReceivedListner(this);
		drone.setDroneTypeChangedListner(this);

		// Buttons
		rcOutput = new RcOutput(app.MAVClient, this);

		launch = (Button) findViewById(R.id.launch);
		arm = (Button) findViewById(R.id.arm);
		disarm = (Button) findViewById(R.id.disarm);
		rtl = (Button) findViewById(R.id.rtl);
		stabilize = (Button) findViewById(R.id.stabilize);
		
		// Button listeners
		rtl.setOnClickListener(this);
		stabilize.setOnClickListener(this);
		launch.setOnTouchListener(this);
		arm.setOnTouchListener(this);
		disarm.setOnTouchListener(this);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_flightdata, menu);

		MenuItem flightModeMenu = menu.findItem(R.id.menu_flight_modes_spinner);
		fligthModeSpinner = (SelectModeSpinner) flightModeMenu.getActionView();
		fligthModeSpinner.buildSpinner(this, this);
		fligthModeSpinner.updateModeSpinner(drone);

		MenuItem wpMenu = menu.findItem(R.id.menu_wp_spinner);
		wpSpinner = (SelectWaypointSpinner) wpMenu.getActionView();
		wpSpinner.buildSpinner(this, this);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_clearFlightPath:
			flightMapFragment.clearFlightPath();
			return true;
		case R.id.menu_zoom:
			flightMapFragment.zoomToLastKnowPosition();
			return true;
		case R.id.menu_follow_me:
			app.followMe.toogleFollowMeState();
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public void OnWaypointSpinnerSelected(int item) {
		app.waypointMananger.setCurrentWaypoint((short) item);
	}

	@Override
	public void onSetGuidedMode(LatLng point) {
		changeDefaultAlt();
		guidedPoint = point;
	}

	public void setGuidedMode(waypoint wp) {
		msg_mission_item msg = new msg_mission_item();
		msg.seq = 0;
		msg.current = 2; // TODO use guided mode enum
		msg.frame = 0; // TODO use correct parameter
		msg.command = 16; // TODO use correct parameter
		msg.param1 = 0; // TODO use correct parameter
		msg.param2 = 0; // TODO use correct parameter
		msg.param3 = 0; // TODO use correct parameter
		msg.param4 = 0; // TODO use correct parameter
		msg.x = (float) wp.coord.latitude;
		msg.y = (float) wp.coord.longitude;
		msg.z = wp.Height.floatValue();
		msg.autocontinue = 1; // TODO use correct parameter
		msg.target_system = 1;
		msg.target_component = 1;
		app.MAVClient.sendMavPacket(msg.pack());
	}

	public void setLaunchPoint(waypoint wp) {
		msg_mission_item msg = new msg_mission_item();
		msg.seq = 0;
		msg.current = 2;	//TODO use guided mode enum
		msg.frame = 0; // TODO use correct parameter
		msg.command = 22; // TODO use correct parameter
		msg.param1 = 0; // TODO use correct parameter
		msg.param2 = 0; // TODO use correct parameter
		msg.param3 = 0; // TODO use correct parameter
		msg.param4 = 0; // TODO use correct parameter
		msg.x = (float) wp.coord.latitude;
		msg.y = (float) wp.coord.longitude;
		msg.z = wp.Height.floatValue();
		msg.autocontinue = 1; // TODO use correct parameter
		msg.target_system = 1;
		msg.target_component = 1;
		app.MAVClient.sendMavPacket(msg.pack());
	}
	
	private void changeFlightMode(ApmModes mode) {
		msg_set_mode msg = new msg_set_mode();
		msg.target_system = 1;
		msg.base_mode = 1; // TODO use meaningful constant
		msg.custom_mode = mode.getNumber();
		app.MAVClient.sendMavPacket(msg.pack());
	}

	@Override
	public void onWaypointsReceived() {
		flightMapFragment.updateMissionPath(drone);
		flightMapFragment.updateHomeToMap(drone);
		wpSpinner.updateWpSpinner(drone);
	}

	@Override
	public void OnModeSpinnerSelected(String text) {
		ApmModes mode = ApmModes.getMode(text, drone.getType());
		if (mode != ApmModes.UNKNOWN) {
			changeFlightMode(mode);
		}
	}

	@Override
	public void onDroneTypeChanged() {
		Log.d("DRONE", "Drone type changed");
		fligthModeSpinner.updateModeSpinner(drone);
		flightMapFragment.droneMarker.updateDroneMarkers();
	}

	@Override
	public void onAltitudeChanged(double newAltitude) {
		super.onAltitudeChanged(newAltitude);
		if (guidedPoint != null) {
			Toast.makeText(this, "Guided Mode (" + (int) newAltitude + "m)",
					Toast.LENGTH_SHORT).show();
			setGuidedMode(new waypoint(guidedPoint, newAltitude));
			guidedPoint = null;
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
		}
	}
	
	private void simulateLaunchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			setLaunchPoint(new waypoint(drone.getPosition(),drone.defaultAlt));
			rcOutput.enableRcOverride();
			rcOutput.setRcChannel(RcOutput.THROTTLE, 1);	
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			rcOutput.disableRcOverride();
		}
	}


}
