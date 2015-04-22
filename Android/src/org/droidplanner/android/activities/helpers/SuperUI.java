package org.droidplanner.android.activities.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.YesNoWithPrefsDialog;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.unit.UnitManager;
import org.droidplanner.android.utils.unit.systems.UnitSystem;

/**
 * Parent class for the app activity classes.
 */
public abstract class SuperUI extends AppCompatActivity implements DroidPlannerApp.ApiListener {

    private static final IntentFilter superIntentFilter = new IntentFilter();

    static {
        superIntentFilter.addAction(AttributeEvent.STATE_CONNECTED);
        superIntentFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        superIntentFilter.addAction(Utils.ACTION_UPDATE_OPTIONS_MENU);
    }

    private final BroadcastReceiver superReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.STATE_CONNECTED:
                    onDroneConnected();
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                    onDroneDisconnected();
                    break;

                case Utils.ACTION_UPDATE_OPTIONS_MENU:
                    invalidateOptionsMenu();
                    break;
            }
        }
    };

    private ScreenOrientation screenOrientation = new ScreenOrientation(this);
    private LocalBroadcastManager lbm;

    /**
     * Handle to the app preferences.
     */
    protected DroidPlannerPrefs mAppPrefs;
    protected UnitSystem unitSystem;
    protected DroidPlannerApp dpApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getApplicationContext();

        dpApp = (DroidPlannerApp) getApplication();
        lbm = LocalBroadcastManager.getInstance(context);

        mAppPrefs = new DroidPlannerPrefs(context);
        unitSystem = UnitManager.getUnitSystem(context);

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
    public void onDestroy() {
        super.onDestroy();
        lbm = null;
    }

    protected LocalBroadcastManager getBroadcastManager() {
        return lbm;
    }

    @Override
    public void onApiConnected() {
        invalidateOptionsMenu();

        getBroadcastManager().registerReceiver(superReceiver, superIntentFilter);
        if (dpApp.getDrone().isConnected())
            onDroneConnected();
        else
            onDroneDisconnected();

        lbm.sendBroadcast(new Intent(MissionProxy.ACTION_MISSION_PROXY_UPDATE));
    }

    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(superReceiver);
        onDroneDisconnected();
    }

    private void onDroneConnected() {
        invalidateOptionsMenu();
        screenOrientation.requestLock();
    }

    private void onDroneDisconnected() {
        invalidateOptionsMenu();
        screenOrientation.unlock();
    }

    @Override
    protected void onStart() {
        super.onStart();

        unitSystem = UnitManager.getUnitSystem(getApplicationContext());
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
        getMenuInflater().inflate(R.menu.menu_super_activiy, menu);

        final MenuItem toggleConnectionItem = menu.findItem(R.id.menu_connect);

        Drone dpApi = dpApp.getDrone();
        if (dpApi != null && dpApi.isConnected()) {
            menu.setGroupEnabled(R.id.menu_group_connected, true);
            menu.setGroupVisible(R.id.menu_group_connected, true);

            final boolean isAdvancedEnabled = mAppPrefs.isAdvancedMenuEnabled();
            final MenuItem advancedSubMenu = menu.findItem(R.id.menu_advanced);
            advancedSubMenu.setEnabled(isAdvancedEnabled);
            advancedSubMenu.setVisible(isAdvancedEnabled);

            final boolean areMissionMenusEnabled = enableMissionMenus();

            final MenuItem sendMission = menu.findItem(R.id.menu_upload_mission);
            sendMission.setEnabled(areMissionMenusEnabled);
            sendMission.setVisible(areMissionMenusEnabled);

            final MenuItem loadMission = menu.findItem(R.id.menu_download_mission);
            loadMission.setEnabled(areMissionMenusEnabled);
            loadMission.setVisible(areMissionMenusEnabled);

            toggleConnectionItem.setTitle(R.string.menu_disconnect);
        } else {
            menu.setGroupEnabled(R.id.menu_group_connected, false);
            menu.setGroupVisible(R.id.menu_group_connected, false);

            toggleConnectionItem.setTitle(R.string.menu_connect);
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
            case R.id.menu_connect:
                toggleDroneConnection();
                return true;

            case R.id.menu_upload_mission: {
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

            case R.id.menu_download_mission:
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

    public void toggleDroneConnection() {
        final Drone drone = dpApp.getDrone();
        if (drone != null && drone.isConnected())
            dpApp.disconnectFromDrone();
        else
            dpApp.connectToDrone();
    }
}