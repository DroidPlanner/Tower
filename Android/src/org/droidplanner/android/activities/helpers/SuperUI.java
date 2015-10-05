package org.droidplanner.android.activities.helpers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.CapabilityApi;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import org.droidplanner.android.AppService;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.SlideToUnlockDialog;
import org.droidplanner.android.dialogs.SupportYesNoDialog;
import org.droidplanner.android.dialogs.SupportYesNoWithPrefsDialog;
import org.droidplanner.android.fragments.SettingsFragment;
import org.droidplanner.android.fragments.actionbar.VehicleStatusFragment;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.unit.UnitManager;
import org.droidplanner.android.utils.unit.systems.UnitSystem;

/**
 * Parent class for the app activity classes.
 */
public abstract class SuperUI extends AppCompatActivity implements DroidPlannerApp.ApiListener,
        SupportYesNoDialog.Listener, ServiceConnection {

    private static final String MISSION_UPLOAD_CHECK_DIALOG_TAG = "Mission Upload check.";

    private static final IntentFilter superIntentFilter = new IntentFilter();

    static {
        superIntentFilter.addAction(AttributeEvent.STATE_CONNECTED);
        superIntentFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        superIntentFilter.addAction(SettingsFragment.ACTION_ADVANCED_MENU_UPDATED);
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

                case SettingsFragment.ACTION_ADVANCED_MENU_UPDATED:
                    supportInvalidateOptionsMenu();
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

    private VehicleStatusFragment statusFragment;

    @Override
    public void setContentView(int resId){
        super.setContentView(resId);

        final int toolbarId = getToolbarId();
        final Toolbar toolbar = (Toolbar) findViewById(toolbarId);
        initToolbar(toolbar);
    }

    @Override
    public void setContentView(View view){
        super.setContentView(view);

        final int toolbarId = getToolbarId();
        final Toolbar toolbar = (Toolbar) findViewById(toolbarId);
        initToolbar(toolbar);
    }

    protected void initToolbar(Toolbar toolbar){
        if(toolbar == null)
            return;

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        addToolbarFragment();
    }

    public void setToolbarTitle(CharSequence title){
        if(statusFragment == null)
            return;

        statusFragment.setTitle(title);
    }

    public void setToolbarTitle(int titleResId){
        if(statusFragment == null)
            return;

        statusFragment.setTitle(getString(titleResId));
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name){

    }

    protected void addToolbarFragment(){
        final int toolbarId = getToolbarId();
        final FragmentManager fm = getSupportFragmentManager();
        statusFragment = (VehicleStatusFragment) fm.findFragmentById(toolbarId);
        if(statusFragment == null){
            statusFragment = new VehicleStatusFragment();
            fm.beginTransaction().add(toolbarId, statusFragment).commit();
        }
    }

    protected abstract int getToolbarId();

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

        bindService(new Intent(context, AppService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(this);
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

    protected void onDroneConnected() {
        invalidateOptionsMenu();
        screenOrientation.requestLock();
    }

    protected void onDroneDisconnected() {
        invalidateOptionsMenu();
        screenOrientation.unlock();
    }

    @Override
    protected void onStart() {
        super.onStart();

        unitSystem = UnitManager.getUnitSystem(getApplicationContext());
        dpApp.addApiListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        dpApp.removeApiListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_super_activiy, menu);

        final MenuItem toggleConnectionItem = menu.findItem(R.id.menu_connect);

        Drone drone = dpApp.getDrone();
        if (drone != null && drone.isConnected()) {
            menu.setGroupEnabled(R.id.menu_group_connected, true);
            menu.setGroupVisible(R.id.menu_group_connected, true);

            final MenuItem killSwitchItem = menu.findItem(R.id.menu_kill_switch);
            final boolean isKillEnabled = mAppPrefs.isKillSwitchEnabled();
            if(killSwitchItem != null && isKillEnabled) {
                CapabilityApi.getApi(drone).checkFeatureSupport(CapabilityApi.FeatureIds.KILL_SWITCH, new CapabilityApi.FeatureSupportListener() {
                    @Override
                    public void onFeatureSupportResult(String s, int i, Bundle bundle) {
                        switch (i) {
                            case CapabilityApi.FEATURE_SUPPORTED:
                                killSwitchItem.setEnabled(true);
                                killSwitchItem.setVisible(true);
                                break;

                            default:
                                killSwitchItem.setEnabled(false);
                                killSwitchItem.setVisible(false);
                                break;
                        }
                    }
                });

            }

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
    public void onDialogYes(String dialogTag) {
        final Drone drone = dpApp.getDrone();
        final MissionProxy missionProxy = dpApp.getMissionProxy();

        switch(dialogTag){
            case MISSION_UPLOAD_CHECK_DIALOG_TAG:
                missionProxy.addTakeOffAndRTL();
                missionProxy.sendMissionToAPM(drone);
                break;
        }
    }

    @Override
    public void onDialogNo(String dialogTag) {
        final Drone drone = dpApp.getDrone();
        final MissionProxy missionProxy = dpApp.getMissionProxy();

        switch(dialogTag){
            case MISSION_UPLOAD_CHECK_DIALOG_TAG:
                missionProxy.sendMissionToAPM(drone);
                break;
        }
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
                    SupportYesNoWithPrefsDialog dialog = SupportYesNoWithPrefsDialog.newInstance(
                            getApplicationContext(), MISSION_UPLOAD_CHECK_DIALOG_TAG,
                            getString(R.string.mission_upload_title),
                            getString(R.string.mission_upload_message),
                            getString(android.R.string.ok),
                            getString(R.string.label_skip),
                            DroidPlannerPrefs.PREF_AUTO_INSERT_MISSION_TAKEOFF_RTL_LAND, this);

                    if (dialog != null) {
                        dialog.show(getSupportFragmentManager(), MISSION_UPLOAD_CHECK_DIALOG_TAG);
                    }
                }
                return true;
            }

            case R.id.menu_download_mission:
                MissionApi.getApi(dpApi).loadWaypoints();
                return true;

            case R.id.menu_kill_switch:
                SlideToUnlockDialog unlockDialog = SlideToUnlockDialog.newInstance("disable vehicle", new Runnable() {
                    @Override
                    public void run() {
                        VehicleApi.getApi(dpApi).arm(false, true, new SimpleCommandListener() {
                            @Override
                            public void onError(int error) {
                                final int errorMsgId;
                                switch(error){
                                    case CommandExecutionError.COMMAND_UNSUPPORTED:
                                        errorMsgId = R.string.error_kill_switch_unsupported;
                                        break;

                                    default:
                                        errorMsgId = R.string.error_kill_switch_failed;
                                        break;
                                }

                                Toast.makeText(getApplicationContext(), errorMsgId, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onTimeout() {
                                Toast.makeText(getApplicationContext(), R.string.error_kill_switch_failed, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                unlockDialog.show(getSupportFragmentManager(), "Slide to use the Kill Switch");
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