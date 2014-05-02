package org.droidplanner.android.glass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import org.droidplanner.R;
import org.droidplanner.android.activities.SettingsActivity;
import org.droidplanner.android.glass.fragments.DashboardFragment;
import org.droidplanner.android.glass.fragments.DashboardFragment.OnDashboardListener;
import org.droidplanner.android.glass.fragments.DashboardFragment.SectionInfo;
import org.droidplanner.android.glass.fragments.HudFragment;
import org.droidplanner.android.glass.utils.GlassUtils;
import org.droidplanner.android.utils.Constants;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

import java.util.LinkedHashMap;
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
        mSectionInfos.put(new SectionInfo(R.string.flight_data, R.drawable.ic_action_plane_white,
                R.string.empty_string), new Runnable() {
            @Override
            public void run() {
                launchHud();
            }
        });
        mSectionInfos.put(new SectionInfo(R.string.settings, R.drawable.ic_action_settings_white,
                R.string.empty_string), new Runnable() {
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
        //TODO: complete
    }

    private void launchSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Used to detect glass specific gestures.
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
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
        if (sectionCb != null)
            sectionCb.run();
    }

    @Override
    public DashboardFragment.SectionInfo[] getSectionsNames() {
        return mSectionInfos.keySet().toArray(new SectionInfo[mSectionInfos.size()]);
    }
}
