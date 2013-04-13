package com.droidplanner;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.MAVLink.Drone;
import com.MAVLink.waypoint;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.ardupilotmega.msg_set_mode;
import com.droidplanner.DroidPlannerApp.OnWaypointReceivedListner;
import com.droidplanner.fragments.FlightMapFragment;
import com.droidplanner.fragments.FlightMapFragment.OnFlighDataListener;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner.OnWaypointSpinnerSelectedListener;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;
import com.google.android.gms.maps.model.LatLng;

public class FlightDataActivity extends SuperActivity implements OnFlighDataListener, OnSpinnerItemSelectedListener, OnWaypointSpinnerSelectedListener, OnWaypointReceivedListner {
	
	private FlightMapFragment flightMapFragment;
	private Drone drone;
	private SpinnerSelfSelect fligthModeSpinner;
	private SelectWaypointSpinner wpSpinner;

	@Override
	int getNavigationItem() {
		return 1;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.flightdata);
		flightMapFragment = ((FlightMapFragment)getFragmentManager().findFragmentById(R.id.flightMapFragment));
				
		this.drone = ((DroidPlannerApp) getApplication()).drone;
		flightMapFragment.updateMissionPath(drone);
		flightMapFragment.updateHomeToMap(drone);
		
		app.setWaypointReceivedListner(this);
	}


	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_flightdata, menu);
		
		MenuItem flightModeMenu = menu.findItem( R.id.menu_flight_modes_spinner);
		fligthModeSpinner = (SpinnerSelfSelect) flightModeMenu.getActionView();
		fligthModeSpinner.setAdapter(ArrayAdapter.createFromResource( this,
		        R.array.menu_fligth_modes,
		        android.R.layout.simple_spinner_dropdown_item ));
		fligthModeSpinner.setOnSpinnerItemSelectedListener(this);
		
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
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public void OnWaypointSpinnerSelected(int item) {
		app.waypointMananger.setCurrentWaypoint((short) item);
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
			changeFlightMode(text);		
	}
	

	@Override
	public void onSetGuidedMode(LatLng point) {
		Toast.makeText(this, "Guided Mode", Toast.LENGTH_SHORT).show();
		setGuidedMode(new waypoint(point, 1000.0)); // Use default altitude to set guided mode.
		
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

	private void changeFlightMode(String string) {
		ApmModes mode = ApmModes.getMode(string);
		if(mode == ApmModes.UNKNOWN){
			return;
		}
		msg_set_mode msg = new msg_set_mode();
		msg.target_system = 1;
		msg.base_mode = 1; //TODO use meaningful constant
		msg.custom_mode = mode.getNumber();
		app.MAVClient.sendMavPacket(msg.pack());			
	}

	@Override
	public void onWaypointsReceived() {
		flightMapFragment.updateMissionPath(drone);
		flightMapFragment.updateHomeToMap(drone);
		wpSpinner.updateWpSpinner(drone);		
	}
}
