package org.droidplanner.activitys.helpers;

import org.droidplanner.DroidPlannerApp;
import org.droidplanner.R;
import org.droidplanner.activitys.ConfigurationActivity;
import org.droidplanner.activitys.SettingsActivity;
import org.droidplanner.dialogs.AltitudeDialog;
import org.droidplanner.dialogs.AltitudeDialog.OnAltitudeChangedListner;
import org.droidplanner.drone.Drone;
import org.droidplanner.fragments.helpers.OfflineMapFragment;
import org.droidplanner.helpers.units.Altitude;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public abstract class SuperActivity extends HelpActivity implements
		OnAltitudeChangedListner {

	public DroidPlannerApp app;
	public Drone drone;

	public SuperActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		app = (DroidPlannerApp) getApplication();
		this.drone = app.drone;

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_drone_setup:
			startActivity(new Intent(this, ConfigurationActivity.class));
			return true;
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
//			drone.calibrationSetup.startCalibration(this);
			return true;
		case R.id.menu_record_me:
			app.recordMe.toogleRecordMeState();
			return true;
		case R.id.menu_follow_me:
			app.followMe.toogleFollowMeState();
			return true;
		case R.id.menu_preflight_checklist:
			return true;
		case R.id.menu_map_type_hybrid:
		case R.id.menu_map_type_normal:
		case R.id.menu_map_type_terrain:
		case R.id.menu_map_type_satellite:
			setMapTypeFromItemId(item.getItemId());
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	private void setMapTypeFromItemId(int itemId) {
		final String mapType;
		switch (itemId) {
		case R.id.menu_map_type_hybrid:
			mapType = OfflineMapFragment.MAP_TYPE_HYBRID;
			break;
		case R.id.menu_map_type_normal:
			mapType = OfflineMapFragment.MAP_TYPE_NORMAL;
			break;
		case R.id.menu_map_type_terrain:
			mapType = OfflineMapFragment.MAP_TYPE_TERRAIN;
			break;
		default:
			mapType = OfflineMapFragment.MAP_TYPE_SATELLITE;
			break;
		}

		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putString(OfflineMapFragment.PREF_MAP_TYPE, mapType).commit();

		//drone.notifyMapTypeChanged();
	}

	public void changeDefaultAlt() {
		AltitudeDialog dialog = new AltitudeDialog(this);
		dialog.build(drone.mission.getDefaultAlt(), this);
	}

	@Override
	public void onAltitudeChanged(Altitude newAltitude) {
		drone.mission.setDefaultAlt(newAltitude);
	}
}