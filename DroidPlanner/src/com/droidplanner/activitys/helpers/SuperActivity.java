package com.droidplanner.activitys.helpers;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.DroidPlannerApp.ConnectionStateListner;
import com.droidplanner.DroidPlannerApp.OnSystemArmListener;
import com.droidplanner.R;
import com.droidplanner.activitys.SettingsActivity;
import com.droidplanner.dialogs.AltitudeDialog;
import com.droidplanner.dialogs.AltitudeDialog.OnAltitudeChangedListner;
import com.droidplanner.drone.Drone;

public abstract class SuperActivity extends Activity implements
		ConnectionStateListner, OnAltitudeChangedListner, OnSystemArmListener{

	public abstract int getNavigationItem();

	public DroidPlannerApp app;
	public Drone drone;
	private MenuItem connectButton;
	private MenuItem armButton;

	private ScreenOrientation screenOrientation = new ScreenOrientation(this);

	public SuperActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		app = (DroidPlannerApp) getApplication();
		app.conectionListner = this;
		app.onSystemArmListener = this;
		this.drone = app.drone;

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		screenOrientation.unlock();
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
		if(armButton != null){
			armButton.setEnabled(false);
		}
		screenOrientation.unlock();
	}

	public void notifyConnected() {
		if (connectButton != null) {
			connectButton.setTitle(getResources().getString(
					R.string.menu_disconnect));
		}
		if(armButton != null){
			armButton.setEnabled(true);
		}
		screenOrientation.requestLock();
	}

	public void notifyArmed() {
		if (armButton != null) {
			armButton.setTitle(getResources().getString(
					R.string.menu_disarm));
		}
	}

	public void notifyDisarmed() {
		if (armButton != null) {
			armButton.setTitle(getResources().getString(
					R.string.menu_arm));
		}
	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.menu_super_activiy, menu);
		//armButton = menu.findItem(R.id.menu_arm);
		//connectButton = menu.findItem(R.id.menu_connect);
		//drone.MavClient.queryConnectionState();
		return super.onCreateOptionsMenu(menu);
	}*/

	public void changeDefaultAlt() {
		AltitudeDialog dialog = new AltitudeDialog(this);
		dialog.build(drone.mission.getDefaultAlt(), this);
	}

	@Override
	public void onAltitudeChanged(double newAltitude) {
		drone.mission.setDefaultAlt(newAltitude);
	}
}