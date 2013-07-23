package com.droidplanner.activitys;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.droidplanner.DroidPlannerApp.OnWaypointReceivedListner;
import com.droidplanner.R;
import com.droidplanner.MAVLink.Drone.DroneTypeListner;
import com.droidplanner.fragments.FlightMapFragment;

public class FlightDataActivity extends SuperFlightActivity implements OnWaypointReceivedListner, DroneTypeListner {
	
	private FlightMapFragment flightMapFragment;

	@Override
	int getNavigationItem() {
		return 1;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.flightdata);
				
		flightMapFragment = ((FlightMapFragment)getFragmentManager().findFragmentById(R.id.flightMapFragment));
		flightMapFragment.updateMissionPath(drone);
		flightMapFragment.updateHomeToMap(drone);
		
		app.setWaypointReceivedListner(this);
		drone.setDroneTypeChangedListner(this);
	}


	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_flightdata, menu);	
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_zoom:
			flightMapFragment.zoomToLastKnowPosition();
			return true;
		case R.id.menu_default_alt:
			changeDefaultAlt();
			return true;
		case R.id.menu_preflight_calibration:
			app.calibrationSetup.startCalibration(this);
			return true;
		case R.id.menu_clearFlightPath:
			flightMapFragment.clearFlightPath();
			return true;		
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
	
	@Override
	public void onWaypointsReceived() {
		super.onWaypointsReceived();
		flightMapFragment.updateMissionPath(drone);
		flightMapFragment.updateHomeToMap(drone);		
	}

	@Override
	public void onDroneTypeChanged() {
		super.onDroneTypeChanged();
		Log.d("DRONE", "Drone type changed");
		flightMapFragment.droneMarker.updateDroneMarkers();
	}

}
