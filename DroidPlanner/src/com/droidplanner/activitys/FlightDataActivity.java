package com.droidplanner.activitys;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.DroidPlannerApp.OnWaypointReceivedListner;
import com.droidplanner.R;
import com.droidplanner.drone.DroneInterfaces.DroneTypeListner;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.FlightMapFragment;
import com.droidplanner.fragments.FlightMapFragment.OnFlighDataListener;
import com.droidplanner.widgets.spinners.SelectModeSpinner;
import com.droidplanner.widgets.spinners.SelectModeSpinner.OnModeSpinnerSelectedListener;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner.OnWaypointSpinnerSelectedListener;
import com.google.android.gms.maps.model.LatLng;

public class FlightDataActivity extends SuperActivity implements OnFlighDataListener, OnWaypointSpinnerSelectedListener, OnWaypointReceivedListner, OnModeSpinnerSelectedListener, DroneTypeListner {
	
	private FlightMapFragment flightMapFragment;
	private SelectModeSpinner fligthModeSpinner;
	private SelectWaypointSpinner wpSpinner;
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_flightdata, menu);
		
		MenuItem flightModeMenu = menu.findItem( R.id.menu_flight_modes_spinner);
		fligthModeSpinner = (SelectModeSpinner) flightModeMenu.getActionView();
		fligthModeSpinner.buildSpinner(this, this);
		fligthModeSpinner.updateModeSpinner(drone);
		
		MenuItem wpMenu = menu.findItem( R.id.menu_wp_spinner);
		wpSpinner = (SelectWaypointSpinner) wpMenu.getActionView();
		wpSpinner.buildSpinner(this,this);	
		
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
		drone.waypointMananger.setCurrentWaypoint((short) item);
	}


	@Override
	public void onSetGuidedMode(LatLng point) {
		changeDefaultAlt();		
		guidedPoint = point;
	}

	@Override
	public void onWaypointsReceived() {
		flightMapFragment.updateMissionPath(drone);
		flightMapFragment.homeMarker.update(drone);
		wpSpinner.updateWpSpinner(drone);		
	}

	@Override
	public void OnModeSpinnerSelected(String text) {
		ApmModes mode = ApmModes.getMode(text,drone.type.getType());
		if (mode != ApmModes.UNKNOWN) {
			drone.state.changeFlightMode(mode);
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
		if(guidedPoint!=null){
			Toast.makeText(this, "Guided Mode ("+(int)newAltitude+"m)", Toast.LENGTH_SHORT).show();
			drone.state.setGuidedMode(new waypoint(guidedPoint, newAltitude)); 
			guidedPoint = null;
		}
	}

}
