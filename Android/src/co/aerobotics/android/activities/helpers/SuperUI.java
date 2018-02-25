package co.aerobotics.android.activities.helpers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import co.aerobotics.android.AppService;
import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.MApplication;
import co.aerobotics.android.dialogs.OkDialog;
import co.aerobotics.android.fragments.DroneMap;
import co.aerobotics.android.fragments.SettingsFragment;
import co.aerobotics.android.data.AeroviewPolygons;
import co.aerobotics.android.utils.Utils;
import co.aerobotics.android.utils.prefs.DroidPlannerPrefs;
import co.aerobotics.android.utils.unit.UnitManager;
import co.aerobotics.android.utils.unit.systems.UnitSystem;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import co.aerobotics.android.R;
import co.aerobotics.android.dialogs.SlideToUnlockDialog;
import co.aerobotics.android.dialogs.SupportYesNoDialog;

import co.aerobotics.android.proxy.mission.MissionProxy;

import java.util.HashMap;
import java.util.Map;

import dji.sdk.base.BaseProduct;

/**
 * Parent class for the app activity classes.
 */
public abstract class SuperUI extends AppCompatActivity implements DroidPlannerApp.ApiListener,
        SupportYesNoDialog.Listener, ServiceConnection {

    private static final String MISSION_UPLOAD_CHECK_DIALOG_TAG = "Mission Upload check.";
    private static final String PERMISSION = "permission";

    private static final IntentFilter superIntentFilter = new IntentFilter();

    static {
        superIntentFilter.addAction(AttributeEvent.STATE_CONNECTED);
        superIntentFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        superIntentFilter.addAction(SettingsFragment.ACTION_ADVANCED_MENU_UPDATED);
        superIntentFilter.addAction(DroidPlannerApp.FLAG_CONNECTION_CHANGE);
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
                case DroidPlannerApp.FLAG_CONNECTION_CHANGE:
                    onConnectionChange();
                    break;
            }
        }
    };

    private ScreenOrientation screenOrientation = new ScreenOrientation(this);
    private LocalBroadcastManager lbm;
    private Handler mHandler;
    private MixpanelAPI mMixpanel;

    /**
     * Handle to the app preferences.
     */
    protected DroidPlannerPrefs mAppPrefs;
    protected UnitSystem unitSystem;
    protected DroidPlannerApp dpApp;

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

    /*public void setToolbarTitle(CharSequence title){
        if(statusFragment == null)
            return;

        statusFragment.setTitle(title);
    }

    public void setToolbarTitle(int titleResId){
        if(statusFragment == null)
            return;

        statusFragment.setTitle(getString(titleResId));
    }*/

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name){

    }

    protected void addToolbarFragment(){
        /*final int toolbarId = getToolbarId();
        final FragmentManager fm = getSupportFragmentManager();
        statusFragment = (VehicleStatusFragment) fm.findFragmentById(toolbarId);
        if(statusFragment == null){
            statusFragment = new VehicleStatusFragment();
            fm.beginTransaction().add(toolbarId, statusFragment).commit();
        }*/
    }

    protected abstract int getToolbarId();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getApplicationContext();
        mMixpanel = MixpanelAPI.getInstance(this, DroidPlannerApp.getInstance().getMixpanelToken());
/*
        //Request user permissions for API versions > 6
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            ActivityCompat.requestPermissions(SuperUI.this,
                    new String[]{
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE,
                            Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        } else {
            if(!DroidPlannerApp.getInstance().isSDKRegistered){
                DroidPlannerApp.getInstance().registerSDK();
            }
        }

*/
        mHandler = new Handler();

        dpApp = DroidPlannerApp.getInstance();
        lbm = LocalBroadcastManager.getInstance(context);

        mAppPrefs = DroidPlannerPrefs.getInstance(context);
        unitSystem = UnitManager.getUnitSystem(context);

		/*
         * Used to supplant wake lock_drawable acquisition (previously in
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 1:
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Fill with results

                for (int i = 0; i < permissions.length; i++) {
                    perms.put(permissions[i], grantResults[i]);
                }
                if (perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    mMixpanel.track("FPA: ReadPhoneStateGranted");
                    if(!DroidPlannerApp.getInstance().isSDKRegistered){
                        //DroidPlannerApp.getInstance().registerSDK();
                    }

                } else {
                    // Permission Denied
                    mMixpanel.track("FPA: ReadPhoneStateDenied");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)){
                            OkDialog dialog = OkDialog.newInstance(getApplicationContext(),
                                    "Permissions Warning", "Access to Phone State is required for this application to function properly"
                                    , new OkDialog.Listener() {
                                        @Override
                                        public void onOk() {
                                            requestPermissions(new String[] {Manifest.permission.READ_PHONE_STATE},
                                                    1);
                                        }

                                        @Override
                                        public void onCancel() {
                                            Toast.makeText(getApplicationContext(), "READ_PHONE_STATE Denied", Toast.LENGTH_SHORT)
                                                    .show();
                                        }

                                        @Override
                                        public void onDismiss() {}
                                    }, true);
                            dialog.show(getSupportFragmentManager(), "Permissions Dialog");
                        }

                    }

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
        /*if (dpApp.getDrone().isConnected())
            onDroneConnected();
        else
            onDroneDisconnected();*/

        notifyStatusChange();
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            LocalBroadcastManager.getInstance(SuperUI.this).sendBroadcast(new Intent(MissionProxy.ACTION_MISSION_PROXY_UPDATE));
            LocalBroadcastManager.getInstance(SuperUI.this).sendBroadcast(new Intent(AeroviewPolygons.ACTION_POLYGON_UPDATE));
            LocalBroadcastManager.getInstance(SuperUI.this).sendBroadcast(new Intent(DroneMap.ACTION_UPDATE_CAMERA_MARKERS));
        }
    };


    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(superReceiver);
        onDroneDisconnected();
    }

    protected void onConnectionChange(){
        BaseProduct drone = DroidPlannerApp.getProductInstance();
        if (drone != null && drone.isConnected()) {
            onDroneConnected();
        } else{
            onDroneDisconnected();
        }
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

        final Context context = getApplicationContext();
        unitSystem = UnitManager.getUnitSystem(context);
        dpApp.addApiListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        dpApp.removeApiListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        /*getMenuInflater().inflate(R.menu.menu_super_activiy, menu);

        final MenuItem toggleConnectionItem = menu.findItem(R.id.menu_connect);
        final boolean areMissionMenusEnabled = enableMissionMenus();

        BaseProduct drone = DroidPlannerApp.getProductInstance();
        if (drone == null || !drone.isConnected()){
            menu.setGroupEnabled(R.id.menu_group_connected, false);
            menu.setGroupVisible(R.id.menu_group_connected, false);


            toggleConnectionItem.setTitle(R.string.menu_connect);
            toggleConnectionItem.setVisible(false);

            return super.onCreateOptionsMenu(menu);
        }

        menu.setGroupEnabled(R.id.menu_group_connected, areMissionMenusEnabled);
        menu.setGroupVisible(R.id.menu_group_connected, areMissionMenusEnabled);

        final MenuItem killSwitchItem = menu.findItem(R.id.menu_kill_switch);
        killSwitchItem.setEnabled(false);
        killSwitchItem.setVisible(false);

        toggleConnectionItem.setTitle(R.string.menu_disconnect);
        toggleConnectionItem.setVisible(false);*/

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
                //missionProxy.addTakeOffAndRTL();
                //missionProxy.sendMissionToAPM(drone);
                break;
        }
    }

    @Override
    public void onDialogNo(String dialogTag) {
        final Drone drone = dpApp.getDrone();
        final MissionProxy missionProxy = dpApp.getMissionProxy();

        switch(dialogTag){
            case MISSION_UPLOAD_CHECK_DIALOG_TAG:
                //missionProxy.sendMissionToAPM(drone);
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

            //case R.id.menu_download_mission:
                //MissionApi.getApi(dpApi).loadWaypoints();
                //return true;

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
        //BaseProduct mProduct = DroidPlannerApp.getProductInstance();
        if (drone.isConnected()) {
            dpApp.disconnectFromDrone();
            //DJISDKManager.getInstance().stopConnectionToProduct();
            //Toast.makeText(getApplicationContext(), "Disconnecting", Toast.LENGTH_LONG).show();
        } else {
            dpApp.connectToDrone();
            //Toast.makeText(getApplicationContext(), "Connecting", Toast.LENGTH_LONG).show();
            //DJISDKManager.getInstance().startConnectionToProduct();
        }
    }
}