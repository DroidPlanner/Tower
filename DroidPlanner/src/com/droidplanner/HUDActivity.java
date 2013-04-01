package com.droidplanner;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.MAVLink.Messages.MAVLinkMessage;
import com.droidplanner.fragments.HudFragment;
import com.droidplanner.service.MAVLinkClient;

public class HUDActivity extends SuperActivity {

	public boolean running;
	MenuItem connectButton;
	private HudFragment hudFragment;

	@Override
	int getNavigationItem() {
		return 1;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.hud);
		
		hudFragment = ((HudFragment)getFragmentManager().findFragmentById(R.id.hud_fragment));
		
	}
	
	@Override
	protected void onResume() {
		super.onRestart();
		MAVClient.init();
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
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			MAVClient.sendConnectMessage();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	public MAVLinkClient MAVClient = new MAVLinkClient(this) {

		@Override
		public void notifyConnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_disconnect));
		}

		@Override
		public void notifyDisconnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_connect));
		}

		@Override
		public void notifyReceivedData(MAVLinkMessage m) {
			hudFragment.receiveData(m);
		}

	};

}
