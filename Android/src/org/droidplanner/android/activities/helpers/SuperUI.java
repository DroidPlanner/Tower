package org.droidplanner.android.activities.helpers;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.fragments.helpers.BTDeviceListFragment;
import org.droidplanner.android.fragments.helpers.OfflineMapFragment;
import org.droidplanner.android.utils.Constants;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.widgets.actionProviders.InfoBarActionProvider;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.gcs.GCSHeartbeat;

import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

public abstract class SuperUI extends FragmentActivity implements
		OnDroneListener {
	private ScreenOrientation screenOrientation = new ScreenOrientation(this);
	private InfoBarActionProvider infoBar;
	private GCSHeartbeat gcsHeartbeat;
	public DroidPlannerApp app;
	public Drone drone;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		/*
		 * Used to supplant wake lock acquisition (previously in
		 * org.droidplanner.android.service .MAVLinkService) as suggested by the
		 * android android.os.PowerManager#newWakeLock documentation.
		 */
		if (PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).getBoolean("pref_keep_screen_bright",
				false)) {
			getWindow()
					.addFlags(
							android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		app = (DroidPlannerApp) getApplication();
		this.drone = app.drone;

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		screenOrientation.unlock();
		Utils.updateUILanguage(getApplicationContext());
		gcsHeartbeat = new GCSHeartbeat(drone, 1);
	}

	@Override
	protected void onStart() {
		super.onStart();
		drone.events.addDroneListener(this);
		drone.MavClient.queryConnectionState();
		drone.events.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		drone.events.removeDroneListener(this);

		if (infoBar != null) {
			infoBar.setDrone(null);
			infoBar = null;
		}
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		if (infoBar != null) {
			infoBar.onDroneEvent(event, drone);
		}

		switch (event) {
		case CONNECTED:
			gcsHeartbeat.setActive(true);
			invalidateOptionsMenu();
			screenOrientation.requestLock();
			break;
		case DISCONNECTED:
			gcsHeartbeat.setActive(false);
			invalidateOptionsMenu();
			screenOrientation.unlock();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Reset the previous info bar
		if (infoBar != null) {
			infoBar.setDrone(null);
			infoBar = null;
		}

		getMenuInflater().inflate(R.menu.menu_super_activiy, menu);

		final MenuItem toggleConnectionItem = menu.findItem(R.id.menu_connect);
		final MenuItem infoBarItem = menu.findItem(R.id.menu_info_bar);
		if (infoBarItem != null)
			infoBar = (InfoBarActionProvider) infoBarItem.getActionProvider();

		// Configure the info bar action provider if we're connected
		if (drone.MavClient.isConnected()) {
			menu.setGroupEnabled(R.id.menu_group_connected, true);
			menu.setGroupVisible(R.id.menu_group_connected, true);

			toggleConnectionItem.setTitle(R.string.menu_disconnect);
			toggleConnectionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

			if (infoBar != null) {
				infoBar.setDrone(drone);
			}
		} else {
			menu.setGroupEnabled(R.id.menu_group_connected, false);
			menu.setGroupVisible(R.id.menu_group_connected, false);

			toggleConnectionItem.setTitle(R.string.menu_connect);
			toggleConnectionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
					| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

			if (infoBar != null) {
				infoBar.setDrone(null);
			}
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_send_mission:
			drone.mission.sendMissionToAPM();
			return true;

		case R.id.menu_load_mission:
			drone.waypointManager.getWaypoints();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			toggleDroneConnection();
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

	protected void toggleDroneConnection() {
		if (!drone.MavClient.isConnected()) {
			final String connectionType = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext())
					.getString(Constants.PREF_CONNECTION_TYPE,
							Constants.DEFAULT_CONNECTION_TYPE);

			if (Utils.ConnectionType.BLUETOOTH.name().equals(connectionType)) {
				// Launch a bluetooth device selection screen for the user
				new BTDeviceListFragment().show(getSupportFragmentManager(),
						"Device selection dialog");
				return;
			}
		}
		drone.MavClient.toggleConnectionState();
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

		// drone.notifyMapTypeChanged();
	}

}