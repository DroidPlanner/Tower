package com.droidplanner.activitys;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.ardupilotmega.msg_set_mode;
import com.droidplanner.R;
import com.droidplanner.widgets.spinners.SelectModeSpinner;
import com.droidplanner.widgets.spinners.SelectModeSpinner.OnModeSpinnerSelectedListener;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner.OnWaypointSpinnerSelectedListener;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public abstract class SuperFlightActivity extends SuperActivity implements OnModeSpinnerSelectedListener, OnWaypointSpinnerSelectedListener {

	public SelectModeSpinner fligthModeSpinner;
	public SelectWaypointSpinner wpSpinner;
	
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
	
}
