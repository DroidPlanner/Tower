package com.droidplanner;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.MAVLink.Drone;
import com.MAVLink.WaypointMananger;
import com.MAVLink.waypoint;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.ardupilotmega.msg_request_data_stream;
import com.MAVLink.Messages.ardupilotmega.msg_set_mode;
import com.droidplanner.fragments.FlightMapFragment;
import com.droidplanner.fragments.FlightMapFragment.OnFlighDataListener;
import com.droidplanner.fragments.HudFragment;
import com.droidplanner.service.MAVLinkClient;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect;
import com.droidplanner.widgets.spinners.SelectWaypointSpinner.OnWaypointSpinnerSelectedListener;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;
import com.google.android.gms.maps.model.LatLng;

public class FlightDataActivity extends SuperActivity implements OnFlighDataListener, OnSpinnerItemSelectedListener, OnWaypointSpinnerSelectedListener {
	
	private MenuItem connectButton;
	private FlightMapFragment flightMapFragment;
	private HudFragment hudFragment;
	private Drone drone;
	private SpinnerSelfSelect fligthModeSpinner;
	private SelectWaypointSpinner wpSpinner;

	@Override
	int getNavigationItem() {
		return 2;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.flightdata);
		flightMapFragment = ((FlightMapFragment)getFragmentManager().findFragmentById(R.id.flightMapFragment));
		hudFragment = ((HudFragment)getFragmentManager().findFragmentById(R.id.hud_fragment2));
		MAVClient.init();
		
		this.drone = ((DroidPlannerApp) getApplication()).drone;
		flightMapFragment.updateMissionPath(drone);
		flightMapFragment.updateHomeToMap(drone);
	}


	@Override
	protected void onStop() {
		super.onDestroy();
		MAVClient.onDestroy();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_flightdata, menu);
		connectButton = menu.findItem(R.id.menu_connect);
		
		MenuItem flightModeMenu = menu.findItem( R.id.menu_flight_modes_spinner);
		fligthModeSpinner = (SpinnerSelfSelect) flightModeMenu.getActionView();
		fligthModeSpinner.setAdapter(ArrayAdapter.createFromResource( this,
		        R.array.menu_fligth_modes,
		        android.R.layout.simple_spinner_dropdown_item ));
		fligthModeSpinner.setOnSpinnerItemSelectedListener(this);
		
		MenuItem wpMenu = menu.findItem( R.id.menu_wp_spinner);
		wpSpinner = (SelectWaypointSpinner) wpMenu.getActionView();
		wpSpinner.buildSpinner(this,this);	
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_connect:
			MAVClient.sendConnectMessage();
			return true;
		case R.id.menu_clearFlightPath:
			flightMapFragment.clearFlightPath();
			return true;
		case R.id.menu_zoom:
			flightMapFragment.zoomToLastKnowPosition();
			return true;			
		case R.id.menu_load_from_apm:
			waypointMananger.getWaypoints();
			return true;			
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public void OnWaypointSpinnerSelected(int item) {
		waypointMananger.setCurrentWaypoint((short) item);
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
			changeFlightMode(text);		
	}


	public MAVLinkClient MAVClient = new MAVLinkClient(this) {
		@Override
		public void notifyReceivedData(MAVLinkMessage msg) {
			hudFragment.receiveData(msg);
			flightMapFragment.receiveData(msg);
			waypointMananger.processMessage(msg);
		}

		@Override
		public void notifyDisconnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_connect));
		}

		@Override
		public void notifyConnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_disconnect));
			setupMavlinkStreamRate();
		}
	};
	
	WaypointMananger waypointMananger = new WaypointMananger(MAVClient) {
		@Override
		public void onWaypointsReceived(List<waypoint> waypoints) {
			if (waypoints != null) {
				Toast.makeText(getApplicationContext(),
						"Waypoints received from Drone", Toast.LENGTH_SHORT)
						.show();
				drone.setHome(waypoints.get(0));
				waypoints.remove(0); // Remove Home waypoint
				drone.clearWaypoints();
				drone.addWaypoints(waypoints);
				flightMapFragment.updateMissionPath(drone);
				flightMapFragment.updateHomeToMap(drone);
				wpSpinner.updateWpSpinner(drone);
			}
		}
		
		@Override
		public void onWriteWaypoints(msg_mission_ack msg) {
		}
	};

	private void setupMavlinkStreamRate() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		int rate = Integer.parseInt(prefs.getString("pref_mavlink_stream_rate",
				"0"));
		if (rate == 0) {
			requestMavlinkDataStream(10, 0, false); // MAV_DATA_STREAM_RAW_CONTROLLER;
		} else {
			requestMavlinkDataStream(10, rate, true);
		}
	}

	private void requestMavlinkDataStream(int stream_id, int rate, boolean start) {
		msg_request_data_stream msg = new msg_request_data_stream();
		msg.target_system = 1;
		msg.target_component = 1;

		msg.req_message_rate = (short) rate;
		msg.req_stream_id = (byte) stream_id;

		msg.start_stop = (byte) (start ? 1 : 0);
		MAVClient.sendMavPacket(msg.pack());
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
		MAVClient.sendMavPacket(msg.pack());
	}

	private void changeFlightMode(String string) {
		int mode = ApmModes.toInt(string);
		if(mode==-1){
			return;
		}
		msg_set_mode msg = new msg_set_mode();
		msg.target_system = 1;
		msg.base_mode = 1; //TODO use meaningful constant
		msg.custom_mode = mode;
		MAVClient.sendMavPacket(msg.pack());			
	}
}
