package com.droidplanner;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class HUDActivity extends SuperActivity {

	public boolean running;
	MenuItem connectButton;

	@Override
	int getNavigationItem() {
		return 1;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.hud);
		
		
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
			//TODO reimplement MAVClient.sendConnectMessage();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

}
