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
import org.droidplanner.core.MAVLink.MavLinkROI;
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
	private InfoBarActionProvider infoBar;
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
		if (drone.getMavClient().isConnected()) {
			menu.setGroupEnabled(R.id.menu_group_connected, true);
			menu.setGroupVisible(R.id.menu_group_connected, true);

            final boolean areMissionMenusEnabled = enableMissionMenus();

            final MenuItem sendMission = menu.findItem(R.id.menu_send_mission);
            sendMission.setEnabled(areMissionMenusEnabled);
            sendMission.setVisible(areMissionMenusEnabled);

            final MenuItem loadMission = menu.findItem(R.id.menu_load_mission);
            loadMission.setEnabled(areMissionMenusEnabled);
            loadMission.setVisible(areMissionMenusEnabled);

			toggleConnectionItem.setTitle(R.string.menu_disconnect);
			toggleConnectionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

			if (infoBar != null) {
				infoBar.setDrone(drone);
			}
		} else {
			menu.setGroupEnabled(R.id.menu_group_connected, false);
			menu.setGroupVisible(R.id.menu_group_connected, false);

			toggleConnectionItem.setTitle(R.string.menu_connect);
			toggleConnectionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

			if (infoBar != null) {
				infoBar.setDrone(null);
			}
		}
		return super.onCreateOptionsMenu(menu);
	}

    protected boolean enableMissionMenus(){
        return false;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_send_mission:
            final MissionProxy missionProxy = app.getMissionProxy();

			if (missionProxy.getItems().isEmpty() || drone.getMission().hasTakeoffAndLandOrRTL()) {
                missionProxy.sendMissionToAPM();
			} else {
                YesNoWithPrefsDialog dialog = YesNoWithPrefsDialog.newInstance(getApplicationContext(),
                        "Mission Upload", "Do you want to append a Takeoff and RTL to your " +
                                "mission?", "Ok", "Skip", new YesNoDialog.Listener() {

                            @Override
                            public void onYes() {
                                missionProxy.addTakeOffAndRTL();
                                missionProxy.sendMissionToAPM();
                            }

                            @Override
                            public void onNo() {
                                missionProxy.sendMissionToAPM();
                            }
                        },
                        getString(R.string.pref_auto_insert_mission_takeoff_rtl_land_key));

                if(dialog != null) {
                    dialog.show(getSupportFragmentManager(), "Mission Upload check.");
                }
			}
			return true;

		case R.id.menu_load_mission:
			drone.getWaypointManager().getWaypoints();
			return true;
		case R.id.menu_epm_grab:
			MavLinkROI.empCommand(drone, false);
			return true;
		case R.id.menu_epm_release:
			MavLinkROI.empCommand(drone, true);
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

	private void setMapTypeFromItemId(int itemId) {
		final String mapType;
		switch (itemId) {
		case R.id.menu_map_type_hybrid:
			mapType = GoogleMapFragment.MAP_TYPE_HYBRID;
			break;
		case R.id.menu_map_type_normal:
			mapType = GoogleMapFragment.MAP_TYPE_NORMAL;
			break;
		case R.id.menu_map_type_terrain:
			mapType = GoogleMapFragment.MAP_TYPE_TERRAIN;
			break;
		default:
			mapType = GoogleMapFragment.MAP_TYPE_SATELLITE;
			break;
		}

		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putString(GoogleMapFragment.PREF_MAP_TYPE, mapType).commit();

		// drone.notifyMapTypeChanged();
	}

}