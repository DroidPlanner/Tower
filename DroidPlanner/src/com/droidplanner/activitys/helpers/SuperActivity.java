package com.droidplanner.activitys.helpers;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.DroidPlannerApp.OnSystemArmListener;
import com.droidplanner.R;
import com.droidplanner.dialogs.AltitudeDialog;
import com.droidplanner.dialogs.AltitudeDialog.OnAltitudeChangedListner;
import com.droidplanner.dialogs.checklist.PreflightDialog;
import com.droidplanner.drone.Drone;
import com.droidplanner.fragments.helpers.OfflineMapFragment;
import com.droidplanner.helpers.units.Altitude;
import com.droidplanner.utils.Constants;
import com.droidplanner.utils.Utils;

public abstract class SuperActivity extends Activity implements
        OnAltitudeChangedListner, OnSystemArmListener {

    public DroidPlannerApp app;
    public Drone drone;
    private MenuItem armButton;

    public SuperActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        app = (DroidPlannerApp) getApplication();
        app.onSystemArmListener = this;
        this.drone = app.drone;

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                if (!drone.MavClient.isConnected()) {
                    final String connectionType = PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext())
                            .getString(Constants.PREF_CONNECTION_TYPE,
                                    Constants.DEFAULT_CONNECTION_TYPE);

                    if (Utils.ConnectionType.BLUETOOTH.name().equals(connectionType)) {
                        //Launch a bluetooth device selection screen for the user
                        startActivity(new Intent(this, BTDeviceSelectionActivity.class));
                        return true;
                    }
                }

            case R.id.menu_disconnect:
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
            case R.id.menu_preflight_checklist:
                showCheckList();
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

    private void showCheckList() {
        PreflightDialog dialog = new PreflightDialog();
        dialog.build(this, drone, false);

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

        drone.notifyMapTypeChanged();
    }

    public void notifyArmed() {
        if (armButton != null) {
            armButton.setTitle(getResources().getString(R.string.menu_disarm));
        }
    }

    public void notifyDisarmed() {
        if (armButton != null) {
            armButton.setTitle(getResources().getString(R.string.menu_arm));
        }
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