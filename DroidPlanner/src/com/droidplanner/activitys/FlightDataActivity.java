package com.droidplanner.activitys;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.R;
import com.droidplanner.drone.DroneInterfaces.DroneTypeListner;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.FlightMapFragment;
import com.droidplanner.fragments.FlightMapFragment.OnFlighDataListener;
import com.droidplanner.widgets.spinners.SelectModeSpinner.OnModeSpinnerSelectedListener;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner.OnWaypointSpinnerSelectedListener;
import com.google.android.gms.maps.model.LatLng;

public class FlightDataActivity extends SuperFlightActivity implements OnFlighDataListener, OnWaypointSpinnerSelectedListener, OnWaypointUpdateListner, OnModeSpinnerSelectedListener, DroneTypeListner {
	
	private FlightMapFragment flightMapFragment;
	private LatLng guidedPoint;

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
		flightMapFragment.homeMarker.update(drone);
		
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
		case R.id.menu_clearFlightPath:
			flightMapFragment.clearFlightPath();
			return true;		
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public void onSetGuidedMode(LatLng point) {
		changeDefaultAlt();		
		guidedPoint = point;
	}

	@Override
	public void onWaypointsUpdate() {
		flightMapFragment.updateMissionPath(drone);
		flightMapFragment.homeMarker.update(drone);
		wpSpinner.updateWpSpinner(drone);		
	}

	@Override
	public void onDroneTypeChanged() {
		Log.d("DRONE", "Drone type changed");
		flightMapFragment.droneMarker.updateDroneMarkers();
		fligthModeSpinner.updateModeSpinner(drone);
	}

	@Override
	public void onAltitudeChanged(double newAltitude) {
		super.onAltitudeChanged(newAltitude);
		if(guidedPoint!=null){
			Toast.makeText(this, "Guided Mode ("+(int)newAltitude+"m)", Toast.LENGTH_SHORT).show();
			drone.state.setGuidedMode(new waypoint(guidedPoint, newAltitude)); 
			guidedPoint = null;
		}
	}

}
