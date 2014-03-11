package org.droidplanner.glass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import com.MAVLink.Messages.ApmModes;
import org.droidplanner.MAVLink.MavLinkArm;
import org.droidplanner.R;
import org.droidplanner.activities.SettingsActivity;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces;
import org.droidplanner.glass.fragments.DashboardFragment;
import org.droidplanner.glass.fragments.DashboardFragment.*;
import org.droidplanner.glass.fragments.GlassMapFragment;
import org.droidplanner.glass.fragments.HudFragment;
import org.droidplanner.glass.utils.GlassUtils;
import org.droidplanner.utils.Constants;
import org.droidplanner.utils.Utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the main activity for the glass interface.
 *
 * @author Fredia Huya-Kouadio
 * @since 1.2.0
 */
public class GlassFlightActivity extends GlassActivity implements DroneInterfaces
        .OnDroneListener, OnDashboardListener {

    //TODO: update description resource for the section info.
    private final Map<SectionInfo, Runnable> mSectionInfos = new LinkedHashMap<SectionInfo,
                Runnable>();

    {
        mSectionInfos.put(new SectionInfo(R.string.flight_data, R.drawable.ic_action_plane,
                R.string.flight_data), new Runnable() {
            @Override
            public void run() {
                launchHud();
            }
        });
        mSectionInfos.put(new SectionInfo(R.string.settings, R.drawable.ic_action_settings,
                R.string.settings), new Runnable() {
            @Override
            public void run() {
                launchSettings();
            }
        });
    }

    /**
     * This is the activity fragment manager.
     *
     * @since 1.2.0
     */
    private FragmentManager mFragManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glass);

        updateConnectionPrefs();

        mFragManager = getSupportFragmentManager();

        Fragment dashboardFragment = getCurrentFragment();
        if (dashboardFragment == null) {
            dashboardFragment = new DashboardFragment();
            mFragManager.beginTransaction().add(R.id.glass_layout, dashboardFragment).commit();
        }
    }

    private void updateConnectionPrefs() {
        PreferenceManager.getDefaultSharedPreferences
                (getApplicationContext()).edit().putString(Constants.PREF_CONNECTION_TYPE,
                Utils.ConnectionType.BLUETOOTH.name()).apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_glass_activity, menu);

        //Update the toggle connection menu title
        final MenuItem connectMenuItem = menu.findItem(R.id.menu_connect);
        if (connectMenuItem != null) {
            connectMenuItem.setTitle(drone.MavClient.isConnected()
                    ? R.string.menu_disconnect
                    : R.string.menu_connect);
        }

        return true;
    }

    private void launchHud() {
        //Replace the current fragment with the hud fragment.
        Fragment currentFragment = getCurrentFragment();
        if (!(currentFragment instanceof HudFragment)) {
            currentFragment = new HudFragment();
            mFragManager.beginTransaction()
                    .replace(R.id.glass_layout, currentFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void launchMap() {
        //Replace the current fragment with the map fragment.
        Fragment currentFragment = getCurrentFragment();
        if (!(currentFragment instanceof GlassMapFragment)) {
            currentFragment = new GlassMapFragment();
            mFragManager.beginTransaction()
                    .replace(R.id.glass_layout, currentFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void launchSettings(){
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Used to detect glass specific gestures.
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (GlassUtils.isGlassDevice() && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (GlassUtils.isVoiceControlActive(getApplicationContext())) {
                openVoiceMenu();
            }
            else {
                openOptionsMenu();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Fragment getCurrentFragment() {
        return mFragManager.findFragmentById(R.id.glass_layout);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch (event) {
            case CONNECTED:
            case DISCONNECTED:
            case ARMING:
                if (GlassUtils.isVoiceControlActive(getApplicationContext())) {
                    invalidateVoiceMenu();
                }
                else {
                    invalidateOptionsMenu();
                }
                break;
        }
    }

    @Override
    public void onSectionSelected(SectionInfo sectionInfo) {
        Runnable sectionCb = mSectionInfos.get(sectionInfo);
        if(sectionCb != null)
            sectionCb.run();
    }

    @Override
    public DashboardFragment.SectionInfo[] getSectionsNames() {
        return mSectionInfos.keySet().toArray(new SectionInfo[mSectionInfos.size()]);
    }
}
