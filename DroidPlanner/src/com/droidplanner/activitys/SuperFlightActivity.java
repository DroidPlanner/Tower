package com.droidplanner.activitys;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.ardupilotmega.msg_set_mode;
import com.droidplanner.R;
import com.droidplanner.fragments.FlightMapFragment.OnFlighDataListener;
import com.droidplanner.widgets.spinners.SelectModeSpinner;
import com.droidplanner.widgets.spinners.SelectModeSpinner.OnModeSpinnerSelectedListener;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner.OnWaypointSpinnerSelectedListener;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner;
import com.google.android.gms.maps.model.LatLng;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class SuperFlightActivity extends SuperActivity implements OnModeSpinnerSelectedListener, OnWaypointSpinnerSelectedListener, OnFlighDataListener {

	public SelectModeSpinner fligthModeSpinner;
	public SelectWaypointSpinner wpSpinner;
	
	private LatLng guidedPoint;
	
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
			case R.id.menu_follow_me:
				app.followMe.toogleFollowMeState();
				return true;
			case R.id.menu_load_from_apm:
				app.waypointMananger.getWaypoints();
				return true;	
			default:
				return super.onMenuItemSelected(featureId, item);
		}
	}
	
	@Override
	public void OnModeSpinnerSelected(String text) {
		ApmModes mode = ApmModes.getMode(text,drone.getType());
		if (mode != ApmModes.UNKNOWN) {
			changeFlightMode(mode);
		}		
	}
	
	@Override
	public void OnWaypointSpinnerSelected(int item) {
		app.waypointMananger.setCurrentWaypoint((short) item);
	}
	
	
	private void changeFlightMode(ApmModes mode) {
		msg_set_mode msg = new msg_set_mode();
		msg.target_system = 1;
		msg.base_mode = 1; //TODO use meaningful constant
		msg.custom_mode = mode.getNumber();
		app.MAVClient.sendMavPacket(msg.pack());			
	}
	
	@Override
	public void onSetGuidedMode(LatLng point) {
		changeDefaultAlt();		
		guidedPoint = point;
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
	
}
