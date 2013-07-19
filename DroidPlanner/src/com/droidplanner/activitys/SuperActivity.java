package com.droidplanner.activitys;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.DroidPlannerApp.ConnectionStateListner;
import com.droidplanner.R;
import com.droidplanner.dialogs.AltitudeDialog;
import com.droidplanner.dialogs.AltitudeDialog.OnAltitudeChangedListner;
import com.droidplanner.drone.Drone;

public abstract class SuperActivity extends Activity implements
		OnNavigationListener, ConnectionStateListner, OnAltitudeChangedListner {

	abstract int getNavigationItem();
	public DroidPlannerApp app;
	private MenuItem connectButton;
	public Drone drone;
	public static int screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

	public SuperActivity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	
		// Set up the action bar to show a dropdown list.
		setUpActionBar();
		app = (DroidPlannerApp) getApplication();
		app.setConectionStateListner(this);
		this.drone = ((DroidPlannerApp) getApplication()).drone;
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setRequestedOrientation(screenRequestedOrientation);
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
		case 0: // Planning
			navigationIntent = new Intent(this, PlanningActivity.class);
			break;
		default:
		case 1: // Flight Data
			navigationIntent = new Intent(this, FlightDataActivity.class);
			navigationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			break;
		case 2: // RC
			navigationIntent = new Intent(this, RCActivity.class);
			break;
		case 3: // Parameters
			navigationIntent = new Intent(this, ParametersActivity.class);
			break;
		case 4: // Camera
			navigationIntent = new Intent(this, CameraActivity.class);
			break;
		case 5: // GCP
			navigationIntent = new Intent(this, GCPActivity.class);
			break;		
		}
		startActivity(navigationIntent);
		return false;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			case R.id.menu_connect:
				toggleConnectionState();
				return true;
			case R.id.menu_load_from_apm:
				drone.waypointMananger.getWaypoints();
				return true;	
			case R.id.menu_default_alt:
				changeDefaultAlt();
				return true;
			case R.id.menu_preflight_calibration:
				drone.calibrationSetup.startCalibration(this);
				return true;
			case R.id.menu_record_me:
				app.recordMe.toogleRecordMeState();
			default:
				return super.onMenuItemSelected(featureId, item);
		}
	}
	
	

	private void toggleConnectionState() {
		if (app.MAVClient.isConnected()) {
			app.MAVClient.close();
		}else{
			app.MAVClient.init();
		}
		
	}

	public void notifyDisconnected() {
		if (connectButton != null) {
			connectButton.setTitle(getResources().getString(
					R.string.menu_connect));
		}
		if (screenRequestedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
			screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
			setRequestedOrientation(screenRequestedOrientation);
		}
	}

	public void notifyConnected() {
		if (connectButton != null) {
			connectButton.setTitle(getResources().getString(
					R.string.menu_disconnect));		
		}
		if (PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext())
				.getBoolean("pref_lock_screen_orientation", false)) {
			int rotation = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation();
			int actualOrientation = getResources().getConfiguration().orientation;
			boolean naturalOrientationLandscape = (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && actualOrientation == Configuration.ORIENTATION_LANDSCAPE) ||
					                               ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && actualOrientation == Configuration.ORIENTATION_PORTRAIT));
			if (naturalOrientationLandscape) {
				switch(rotation) {
				case Surface.ROTATION_0:
					screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
					break;
				case Surface.ROTATION_90:
					screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
					break;
				case Surface.ROTATION_180:
					screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
					break;
				case Surface.ROTATION_270:
					screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					break;
				}
			} else {
				switch(rotation) {
				case Surface.ROTATION_0:
					screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					break;
				case Surface.ROTATION_90:
					screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
					break;
				case Surface.ROTATION_180:
					screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
					break;
				case Surface.ROTATION_270:
					screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
					break;
				}
			}
			setRequestedOrientation(screenRequestedOrientation);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		connectButton = menu.findItem(R.id.menu_connect);
		app.MAVClient.queryConnectionState();
		return super.onCreateOptionsMenu(menu);
	}

	protected void changeDefaultAlt() {
		AltitudeDialog dialog = new AltitudeDialog(this);
		dialog.build(drone.mission.getDefaultAlt(), this);
	}
	
	@Override
	public void onAltitudeChanged(double newAltitude) {
		drone.mission.setDefaultAlt(newAltitude);
	}
}