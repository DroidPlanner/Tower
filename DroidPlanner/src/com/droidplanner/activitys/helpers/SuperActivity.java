package com.droidplanner.activitys.helpers;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.DroidPlannerApp.ConnectionStateListner;
import com.droidplanner.R;
import com.droidplanner.activitys.CameraActivity;
import com.droidplanner.activitys.ChartActivity;
import com.droidplanner.activitys.FlightDataActivity;
import com.droidplanner.activitys.GCPActivity;
import com.droidplanner.activitys.ParametersActivity;
import com.droidplanner.activitys.PlanningActivity;
import com.droidplanner.activitys.RCActivity;
import com.droidplanner.activitys.SettingsActivity;
import com.droidplanner.dialogs.AltitudeDialog;
import com.droidplanner.dialogs.AltitudeDialog.OnAltitudeChangedListner;
import com.droidplanner.drone.Drone;

public abstract class SuperActivity extends Activity implements
		OnNavigationListener, ConnectionStateListner, OnAltitudeChangedListner {

	public abstract int getNavigationItem();

	public DroidPlannerApp app;
	public Drone drone;
	private MenuItem connectButton;

	private ScreenOrientation screenOrientation = new ScreenOrientation(this);

	public SuperActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// Set up the action bar to show a dropdown list.
		setUpActionBar();
		app = (DroidPlannerApp) getApplication();
		app.conectionListner = this;
		this.drone = app.drone;

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		screenOrientation.unlock();
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
		case 6: // Chart
			navigationIntent = new Intent(this, ChartActivity.class);
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
			drone.MavClient.toggleConnectionState();
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
			return true;
		case R.id.menu_follow_me:
			app.followMe.toogleFollowMeState();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	public void notifyDisconnected() {
		if (connectButton != null) {
			connectButton.setTitle(getResources().getString(
					R.string.menu_connect));
		}
		screenOrientation.unlock();
	}

	public void notifyConnected() {
		if (connectButton != null) {
			connectButton.setTitle(getResources().getString(
					R.string.menu_disconnect));
		}
		screenOrientation.requestLock();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_super_activiy, menu);
		connectButton = menu.findItem(R.id.menu_connect);
		drone.MavClient.queryConnectionState();
		return super.onCreateOptionsMenu(menu);
	}

	public void changeDefaultAlt() {
		AltitudeDialog dialog = new AltitudeDialog(this);
		dialog.build(drone.mission.getDefaultAlt(), this);
	}

	@Override
	public void onAltitudeChanged(double newAltitude) {
		drone.mission.setDefaultAlt(newAltitude);
	}
}