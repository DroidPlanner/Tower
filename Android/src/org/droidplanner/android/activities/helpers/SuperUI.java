package org.droidplanner.android.activities.helpers;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.YesNoWithPrefsDialog;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.api.services.DroidPlannerService;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.widgets.actionProviders.InfoBarActionProvider;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
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
public abstract class SuperUI extends FragmentActivity implements OnDroneListener,
        DroidPlannerApp.ApiListener {

    private ScreenOrientation screenOrientation = new ScreenOrientation(this);
    private InfoBarActionProvider infoBar;

	/**
	 * Handle to the app preferences.
	 */
	protected DroidPlannerPrefs mAppPrefs;

    protected DroidPlannerApp dpApp;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        dpApp = (DroidPlannerApp) getApplication();

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
		}

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
	}

    @Override
    public void onApiConnected(DroidPlannerApi api){
        invalidateOptionsMenu();

        api.addDroneListener(SuperUI.this);
        api.getMavClient().queryConnectionState();
        api.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
    }

	@Override
	public void onApiDisconnected() {
		dpApp.getApi().removeDroneListener(this);

		if (infoBar != null) {
			infoBar.setDrone(null);
			infoBar = null;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
        dpApp.addApiListener(this);
		maxVolumeIfEnabled();
	}

    @Override
    public void onStop(){
        super.onStop();
        dpApp.removeApiListener(this);
    }

	private void maxVolumeIfEnabled() {
		if (mAppPrefs.maxVolumeOnStart()) {
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		}
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		if (infoBar != null) {
			infoBar.onDroneEvent(event, drone);
		}

		switch (event) {
		case CONNECTED:
			invalidateOptionsMenu();
			screenOrientation.requestLock();
			break;

		case DISCONNECTED:
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
        DroidPlannerApi dpApi = dpApp.getApi();
		if (dpApi != null && dpApi.isConnected()) {
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

			if (infoBar != null) {
				infoBar.setDrone(dpApi.getDrone());
			}
		} else {
			menu.setGroupEnabled(R.id.menu_group_connected, false);
			menu.setGroupVisible(R.id.menu_group_connected, false);

			toggleConnectionItem.setTitle(R.string.menu_connect);

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
		case R.id.menu_send_mission: {
            DroidPlannerApi dpApi = dpApp.getApi();
            final MissionProxy missionProxy = dpApi.getMissionProxy();
            if (dpApi.getMission().hasTakeoffAndLandOrRTL()) {
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

                if (dialog != null) {
                    dialog.show(getSupportFragmentManager(), "Mission Upload check.");
                }
            }
            return true;
        }

		case R.id.menu_load_mission: {
            DroidPlannerApi dpApi = dpApp.getApi();
            if (dpApi != null) {
                dpApi.getWaypointManager().getWaypoints();
            }
            return true;
        }

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
        startService(new Intent(getApplicationContext(), DroidPlannerService.class).setAction
                (DroidPlannerService.ACTION_TOGGLE_DRONE_CONNECTION));
	}
}