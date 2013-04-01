package com.droidplanner;

import android.app.ActionBar;
import android.app.TaskStackBuilder;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

public abstract class SuperActivity extends Activity implements
		OnNavigationListener {
	abstract int getNavigationItem();

	public SuperActivity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	
		// Set up the action bar to show a dropdown list.
		setUpActionBar();
	}

	public void setUpActionBar() {
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this,
				R.array.menu_dropdown,
				android.R.layout.simple_spinner_dropdown_item);
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, this);
		actionBar.setSelectedNavigationItem(getNavigationItem());
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (itemPosition == getNavigationItem()) {
			return false;
		}
		Intent navigationIntent;
		switch (itemPosition) {
		default:
		case 0: // Planning
			navigationIntent = new Intent(this, PlanningActivity.class);
			break;
		case 1: // HUD
			navigationIntent = new Intent(this, HUDActivity.class);
			break;
		case 2: // Flight Data
			navigationIntent = new Intent(this, FlightDataActivity.class);
			navigationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			break;
		case 3: // PID
			navigationIntent = new Intent(this, RCActivity.class);
			break;
		case 4: // Terminal
			navigationIntent = new Intent(this, TerminalActivity.class);
			break;
		case 5: // GCP
			navigationIntent = new Intent(this, GCPActivity.class);
			break;
		}
		startActivity(navigationIntent);
		return false;
	}
}