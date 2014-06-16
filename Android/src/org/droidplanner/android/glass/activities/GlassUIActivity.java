package org.droidplanner.android.glass.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;

import org.droidplanner.R;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.glass.fragments.DashboardFragment;
import org.droidplanner.android.glass.fragments.DashboardFragment.OnDashboardListener;
import org.droidplanner.android.glass.fragments.DashboardFragment.SectionInfo;
import org.droidplanner.android.glass.fragments.GlassSettingsFragment;
import org.droidplanner.android.glass.fragments.HudFragment;
import org.droidplanner.android.utils.Constants;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

import java.util.LinkedHashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * This is the main activity for the glass interface.
 */
public class GlassUIActivity extends SuperUI implements DroneInterfaces
        .OnDroneListener, OnDashboardListener {

    //TODO: update description resource for the section info.
    private final Map<SectionInfo, Runnable> mSectionInfos = new LinkedHashMap<SectionInfo,
            Runnable>();

    {
        mSectionInfos.put(new SectionInfo(R.string.flight_data, R.drawable.ic_action_plane_white,
                R.string.empty_string), new Runnable() {
            @Override
            public void run() {
                launchFlightData();
            }
        });
        mSectionInfos.put(new SectionInfo(R.string.mission_editor, R.drawable.ic_edit,
                R.string.empty_string), new Runnable() {
            @Override
            public void run() {
                launchMissionEditor();
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
     */
    private FragmentManager mFragManager;

    /**
     * Glass gesture detector.
     * Detects glass specific swipes, and taps, and uses it for navigation.
     */
    protected GestureDetector mGestureDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glass);

        updateConnectionPrefs();
        setUpGestureDetector();

        mFragManager = getSupportFragmentManager();

        Fragment dashboardFragment = getCurrentFragment();
        if (dashboardFragment == null) {
            dashboardFragment = new DashboardFragment();
            mFragManager.beginTransaction().add(R.id.glass_layout, dashboardFragment).commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Register for bus events
//        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop(){
        super.onStop();

        //Stop listening to bus events
//        EventBus.getDefault().unregister(this);
    }

    private void updateConnectionPrefs() {
        PreferenceManager.getDefaultSharedPreferences
                (getApplicationContext()).edit().putString(Constants.PREF_CONNECTION_TYPE,
                Utils.ConnectionType.BLUETOOTH.name()).apply();
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
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

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null && event.getSource() == InputDevice.SOURCE_TOUCHPAD) {
            return mGestureDetector.onMotionEvent(event);
        }
        return super.onGenericMotionEvent(event);
    }

    private void launchFlightData() {
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

    private void launchMissionEditor() {
        //TODO: complete
    }

    private void launchSettings() {
        //Replace the current fragment with the settings fragment.
        Fragment currentFragment = getCurrentFragment();
        if(!(currentFragment instanceof GlassSettingsFragment)){
            currentFragment = new GlassSettingsFragment();
            mFragManager.beginTransaction()
                    .replace(R.id.glass_layout, currentFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void setUpGestureDetector() {
        mGestureDetector = new GestureDetector(getApplicationContext());
    }

    /**
     * Used to detect glass specific gestures.
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
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
                invalidateOptionsMenu();
                break;
        }
    }

    @Override
    public void onSectionSelected(SectionInfo sectionInfo) {
        Runnable sectionCb = mSectionInfos.get(sectionInfo);
        if (sectionCb != null) { sectionCb.run(); }
    }

    @Override
    public DashboardFragment.SectionInfo[] getSectionsNames() {
        return mSectionInfos.keySet().toArray(new SectionInfo[mSectionInfos.size()]);
    }
}
