package com.droidplanner.activitys.helpers;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.MAVLink.MavLinkArm;
import com.droidplanner.R;
import com.droidplanner.drone.DroneInterfaces.DroneTypeListner;
import com.droidplanner.drone.variables.GuidedPoint;
import com.droidplanner.drone.variables.GuidedPoint.OnGuidedListener;
import com.droidplanner.fragments.FlightMapFragment;
import com.droidplanner.fragments.HudFragment;
import com.droidplanner.widgets.spinners.SelectModeSpinner;
import com.droidplanner.widgets.spinners.SelectModeSpinner.OnModeSpinnerSelectedListener;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner.OnWaypointSpinnerSelectedListener;
import com.droidplanner.helpers.RcOutput;

public abstract class SuperFlightActivity extends SuperActivity implements
		OnModeSpinnerSelectedListener, OnWaypointSpinnerSelectedListener,
		OnGuidedListener, DroneTypeListner, OnWaypointUpdateListner {


	private SelectModeSpinner fligthModeSpinner;
	private SelectWaypointSpinner wpSpinner;

	public FlightMapFragment mapFragment;
	public HudFragment hudFragment;

	public SuperFlightActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		drone.guidedPoint.setOnGuidedListner(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_super_flight_activity, menu);

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
		case R.id.menu_arm:
			if (drone.MavClient.isConnected()) {
				if (!drone.state.isArmed())
					drone.tts.speak("Arming the vehicle, please standby");
				MavLinkArm.sendArmMessage(drone, !drone.state.isArmed());

			}
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public void OnModeSpinnerSelected(String text) {
		ApmModes mode = ApmModes.getMode(text, drone.type.getType());
		if (ApmModes.isValid(mode)) {
			drone.state.changeFlightMode(mode);
		}
	}

	@Override
	public void OnWaypointSpinnerSelected(int item) {
		drone.waypointMananger.setCurrentWaypoint((short) item);
	}

	@Override
	public void onGuidedPoint(GuidedPoint guidedPoint) {
		changeDefaultAlt();
	}

	@Override
	public void onAltitudeChanged(double newAltitude,boolean applyToAll) {
		super.onAltitudeChanged(newAltitude, applyToAll);
		if (drone.guidedPoint.isCoordValid()) {
			drone.guidedPoint.setGuidedMode();
		}
	}

	@Override
	public void onDroneTypeChanged() {
		Log.d("DRONE", "Drone type changed");
		fligthModeSpinner.updateModeSpinner(drone);
		if (mapFragment != null) {
			mapFragment.droneMarker.updateDroneMarkers();
		}
	}

	@Override
	public void onWaypointsUpdate() {
		wpSpinner.updateWpSpinner(drone);
		if (mapFragment != null) {
			mapFragment.updateFragment();
		}
        
    }
    
    public void sendRC(){
        RcOutput rcOutput = new RcOutput(drone, this);
        rcOutput.enableRcOverride();
        rcOutput.setRcChannel(RcOutput.TROTTLE, 1);
        try{
            Thread.sleep(500);
        } catch (InterruptedException e) {
            //TODO Auto-generated catch block
            e.printStackTrace();
            
        }
        rcOutput.disableRcOverride();
    }
}
