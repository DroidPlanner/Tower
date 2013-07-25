package com.droidplanner.activitys;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.R;
import com.droidplanner.drone.DroneInterfaces.DroneTypeListner;
import com.droidplanner.drone.variables.GuidedPoint;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.FlightMapFragment.OnFlighDataListener;
import com.droidplanner.widgets.spinners.SelectModeSpinner;
import com.droidplanner.widgets.spinners.SelectModeSpinner.OnModeSpinnerSelectedListener;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner.OnWaypointSpinnerSelectedListener;

public abstract class SuperFlightActivity extends SuperActivity implements
		OnModeSpinnerSelectedListener, OnWaypointSpinnerSelectedListener,
		OnFlighDataListener, DroneTypeListner, OnWaypointUpdateListner {

	private SelectModeSpinner fligthModeSpinner;
	private SelectWaypointSpinner wpSpinner;

	private GuidedPoint guidedPoint;

	public SuperFlightActivity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
	public void OnModeSpinnerSelected(String text) {
		ApmModes mode = ApmModes.getMode(text, drone.type.getType());
		if (mode != ApmModes.UNKNOWN) {
			drone.state.changeFlightMode(mode);
		}
	}

	@Override
	public void OnWaypointSpinnerSelected(int item) {
		drone.waypointMananger.setCurrentWaypoint((short) item);
	}

	@Override
	public void onSetGuidedMode(GuidedPoint guidedPoint) {
		changeDefaultAlt();
		this.guidedPoint = guidedPoint;
	}

	@Override
	public void onAltitudeChanged(double newAltitude) {
		super.onAltitudeChanged(newAltitude);
		if (guidedPoint != null) {
			Toast.makeText(this, "Guided Mode (" + (int) newAltitude + "m)",
					Toast.LENGTH_SHORT).show();
			drone.state.setGuidedMode(new waypoint(guidedPoint.getCoord(), newAltitude));
			guidedPoint = null;
		}
	}

	@Override
	public void onDroneTypeChanged() {
		fligthModeSpinner.updateModeSpinner(drone);
	}

	@Override
	public void onWaypointsUpdate() {
		wpSpinner.updateWpSpinner(drone);
	}
}
