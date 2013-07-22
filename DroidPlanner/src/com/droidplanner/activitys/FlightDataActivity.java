package com.droidplanner.activitys;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.droidplanner.DroidPlannerApp.OnWaypointReceivedListner;
import com.droidplanner.R;
import com.droidplanner.MAVLink.Drone.DroneTypeListner;
import com.droidplanner.fragments.FlightMapFragment;
import com.droidplanner.fragments.FlightMapFragment.OnFlighDataListener;
import com.google.android.gms.maps.model.LatLng;

public class FlightDataActivity extends SuperFlightActivity implements OnFlighDataListener, OnWaypointReceivedListner, DroneTypeListner {
	
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
	public void onSetGuidedMode(LatLng point) {
		changeDefaultAlt();		
		guidedPoint = point;
	}
	
	
	public void setGuidedMode(waypoint wp) {
		msg_mission_item msg = new msg_mission_item();
		msg.seq = 0;
		msg.current = 2;	//TODO use guided mode enum
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
	
	@Override
	public void onWaypointsReceived() {
		flightMapFragment.updateMissionPath(drone);
		flightMapFragment.updateHomeToMap(drone);
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
			setGuidedMode(new waypoint(guidedPoint, newAltitude)); 
			guidedPoint = null;
		}
	}

}
