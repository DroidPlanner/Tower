package org.droidplanner.android.activities.helpers;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.YesNoWithPrefsDialog;
import org.droidplanner.android.fragments.helpers.BTDeviceListFragment;
import org.droidplanner.android.maps.providers.google_map.GoogleMapFragment;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.widgets.actionProviders.InfoBarActionProvider;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.gcs.GCSHeartbeat;
import org.droidplanner.core.model.Drone;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Parent class for the app activity classes.
 */
public abstract class SuperUI extends FragmentActivity implements OnDroneListener {

	public final static String ACTION_TOGGLE_DRONE_CONNECTION = SuperUI.class.getName()
			+ ".ACTION_TOGGLE_DRONE_CONNECTION";

    private ScreenOrientation screenOrientation = new ScreenOrientation(this);
	private GCSHeartbeat gcsHeartbeat;
	public DroidPlannerApp app;
	public Drone drone;

	/**
	 * Handle to the app preferences.
	 */
	protected DroidPlannerPrefs mAppPrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
		}

		app = (DroidPlannerApp) getApplication();
		this.drone = app.getDrone();
		gcsHeartbeat = new GCSHeartbeat(drone, 1);
		mAppPrefs = new DroidPlannerPrefs(getApplicationContext());

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		/*
		 * Used to supplant wake lock acquisition (previously in
		 * org.droidplanner.android.service .MAVLinkService) as suggested by the
		 * android android.os.PowerManager#newWakeLock documentation.
		 */
		if (mAppPrefs.keepScreenOn()) {
			getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		screenOrientation.unlock();
		Utils.updateUILanguage(getApplicationContext());

		handleIntent(getIntent());
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent == null)
			return;

		final String action = intent.getAction();
		if (ACTION_TOGGLE_DRONE_CONNECTION.equals(action)) {
			toggleDroneConnection();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		maxVolumeIfEnabled();
		drone.addDroneListener(this);
		drone.getMavClient().queryConnectionState();
		drone.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
	}

	private void maxVolumeIfEnabled() {
		if (mAppPrefs.maxVolumeOnStart()) {
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		drone.removeDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
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
		getMenuInflater().inflate(R.menu.menu_super_activiy, menu);

		final MenuItem toggleConnectionItem = menu.findItem(R.id.menu_connect);

		// Configure the info bar action provider if we're connected
		if (drone.getMavClient().isConnected()) {
			menu.setGroupEnabled(R.id.menu_group_connected, true);
			menu.setGroupVisible(R.id.menu_group_connected, true);

			toggleConnectionItem.setTitle(R.string.menu_disconnect);

		} else {
			menu.setGroupEnabled(R.id.menu_group_connected, false);
			menu.setGroupVisible(R.id.menu_group_connected, false);

			toggleConnectionItem.setTitle(R.string.menu_connect);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_load_mission:
			drone.getWaypointManager().getWaypoints();
			return true;

		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
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

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	public void toggleDroneConnection() {
		if (!drone.getMavClient().isConnected()) {
			final String connectionType = mAppPrefs.getMavLinkConnectionType();

			if (Utils.ConnectionType.BLUETOOTH.name().equals(connectionType)) {
				// Launch a bluetooth device selection screen for the user
				final String address = mAppPrefs.getBluetoothDeviceAddress();
				if (address == null || address.isEmpty()) {
					new BTDeviceListFragment().show(getSupportFragmentManager(),
							"Device selection dialog");
					return;
				}
			}
		}
		drone.getMavClient().toggleConnectionState();
	}
}