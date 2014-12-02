package org.droidplanner.android.activities.helpers;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;

import org.droidplanner.android.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.YesNoWithPrefsDialog;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.widgets.actionProviders.InfoBarActionProvider;

/**
 * Parent class for the app activity classes.
 */
public abstract class SuperUI extends FragmentActivity implements DroidPlannerApp.ApiListener {

    private static final IntentFilter superIntentFilter = new IntentFilter();
    static {
        superIntentFilter.addAction(AttributeEvent.STATE_CONNECTED);
        superIntentFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
    }

    private final BroadcastReceiver superReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(AttributeEvent.STATE_CONNECTED.equals(action)){
                onDroneConnected();
            }
            else if(AttributeEvent.STATE_DISCONNECTED.equals(action)){
                onDroneDisconnected();
            }
        }
    };

	private ScreenOrientation screenOrientation = new ScreenOrientation(this);
	private InfoBarActionProvider infoBar;
    private LocalBroadcastManager lbm;

	/**
	 * Handle to the app preferences.
	 */
	protected DroidPlannerPrefs mAppPrefs;

	protected DroidPlannerApp dpApp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        final Context context = getApplicationContext();
		dpApp = (DroidPlannerApp) getApplication();
        lbm = LocalBroadcastManager.getInstance(context);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeButtonEnabled(true);
		}

		mAppPrefs = new DroidPlannerPrefs(context);

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
		Utils.updateUILanguage(context);
	}

    @Override
    public void onDestroy(){
        super.onDestroy();
        lbm = null;
    }

    protected LocalBroadcastManager getBroadcastManager(){
        return lbm;
    }

	@Override
	public void onApiConnected() {
		invalidateOptionsMenu();

        getBroadcastManager().registerReceiver(superReceiver, superIntentFilter);
        if(dpApp.getDrone().isConnected())
            onDroneConnected();
        else
            onDroneDisconnected();

        lbm.sendBroadcast(new Intent(MissionProxy.ACTION_MISSION_PROXY_UPDATE));
	}

	@Override
	public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(superReceiver);

		if (infoBar != null) {
			infoBar.setDrone(null);
			infoBar = null;
		}

        onDroneDisconnected();
	}

    private void onDroneConnected(){
        invalidateOptionsMenu();
        screenOrientation.requestLock();
    }

    private void onDroneDisconnected(){
        invalidateOptionsMenu();
        screenOrientation.unlock();
    }

	@Override
	protected void onStart() {
		super.onStart();
		dpApp.addApiListener(this);
		maxVolumeIfEnabled();
	}

	@Override
	public void onStop() {
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
		Drone dpApi = dpApp.getDrone();
		if (dpApi.isConnected()) {
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
				infoBar.setDrone(dpApi);
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

	protected boolean enableMissionMenus() {
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Drone dpApi = dpApp.getDrone();

		switch (item.getItemId()) {
		case R.id.menu_send_mission: {
			final MissionProxy missionProxy = dpApp.getMissionProxy();
			if (missionProxy.getItems().isEmpty() || missionProxy.hasTakeoffAndLandOrRTL()) {
				missionProxy.sendMissionToAPM(dpApi);
			} else {
				YesNoWithPrefsDialog dialog = YesNoWithPrefsDialog.newInstance(
						getApplicationContext(), "Mission Upload",
						"Do you want to append a Takeoff and RTL to your " + "mission?", "Ok",
						"Skip", new YesNoDialog.Listener() {

							@Override
							public void onYes() {
								missionProxy.addTakeOffAndRTL();
								missionProxy.sendMissionToAPM(dpApi);
							}

							@Override
							public void onNo() {
								missionProxy.sendMissionToAPM(dpApi);
							}
						}, getString(R.string.pref_auto_insert_mission_takeoff_rtl_land_key));

				if (dialog != null) {
					dialog.show(getSupportFragmentManager(), "Mission Upload check.");
				}
			}
			return true;
		}

		case R.id.menu_load_mission:
			dpApi.loadWaypoints();
			return true;
		case R.id.menu_triggerCamera:
			dpApi.triggerCamera();
			return true;
		case R.id.menu_epm_grab:
			dpApi.epmCommand(false);
			return true;
		case R.id.menu_epm_release:
			dpApi.epmCommand(true);
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
        final Drone drone = dpApp.getDrone();
		if(drone.isConnected())
            dpApp.disconnectFromDrone();
        else
            dpApp.connectToDrone();
	}
}