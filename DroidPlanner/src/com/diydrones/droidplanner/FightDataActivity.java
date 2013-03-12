package com.diydrones.droidplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_request_data_stream;
import com.diydrones.droidplanner.fragments.FlightMapFragment;
import com.diydrones.droidplanner.fragments.HudFragment;
import com.diydrones.droidplanner.service.MAVLinkClient;

public class FightDataActivity extends SuperActivity {

	private MenuItem connectButton;
	private FlightMapFragment flightMapFragment;
	private HudFragment hudFragment;

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
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		MAVClient.onDestroy();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_flightdata, menu);
		connectButton = menu.findItem(R.id.menu_connect);
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
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}


	public MAVLinkClient MAVClient = new MAVLinkClient(this) {
		@Override
		public void notifyReceivedData(MAVLinkMessage msg) {
			hudFragment.receiveData(msg);
			flightMapFragment.receiveData(msg);
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
}
