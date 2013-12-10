package com.droidplanner.glass.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.fragments.SettingsFragment;
import com.droidplanner.glass.fragments.ChartFragment;
import com.droidplanner.glass.fragments.HudFragment;
import com.droidplanner.glass.utils.GlassUtils;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * This is the main activity for the glass interface.
 * @author Fredia Huya-Kouadio
 * @since 1.2.0
 */
public class GlassActivity extends SuperActivity implements DroidPlannerApp.ConnectionStateListner{

    private static final String TAG = GlassActivity.class.getName();

    /**
     * Glass gesture detector.
     * Detects glass specific swipes, and taps, and uses it for navigation.
     * @since 1.2.0
     */
    private GestureDetector mGestureDetector;

    /**
     * This is the activity fragment manager.
     * @since 1.2.0
     */
    private FragmentManager mFragManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glass);

        setUpGestureDetector();

        mFragManager = getFragmentManager();

        Fragment glassFragment = getCurrentFragment();
        if(glassFragment == null){
            glassFragment = new HudFragment();
            mFragManager.beginTransaction().add(R.id.glass_layout, glassFragment).commit();
        }
    }

    @Override
    public void onStart(){
        super.onStart();

        app.conectionListner = this;
        drone.MavClient.queryConnectionState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_glass_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_settings:{
                //Replace the current fragment with the SettingsFragment.
                Fragment currentFragment = getCurrentFragment();
                if(!(currentFragment instanceof SettingsFragment)){
                    currentFragment = new SettingsFragment();
                    mFragManager.beginTransaction()
                            .replace(R.id.glass_layout, currentFragment)
                            .addToBackStack(null)
                            .commit();
                }
                return true;
            }

            case R.id.menu_chart:{
                //Replace the current fragment with the chart fragment.
                Fragment currentFragment = getCurrentFragment();
                if(!(currentFragment instanceof ChartFragment)){
                    currentFragment = new ChartFragment();
                    mFragManager.beginTransaction()
                            .replace(R.id.glass_layout, currentFragment)
                            .addToBackStack(null)
                            .commit();
                }
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event){
        if(mGestureDetector != null && event.getSource() == InputDevice.SOURCE_TOUCHPAD){
            return mGestureDetector.onMotionEvent(event);
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        final MenuItem connectMenuItem = menu.findItem(R.id.menu_connect);
        int titleRes = drone.MavClient.isConnected() ? R.string.menu_disconnect: R.string
                .menu_connect;

        connectMenuItem.setTitle(titleRes);
        return true;
    }

    /**
     * Used to detect glass specific gestures.
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(GlassUtils.isGlassDevice() && keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
            openOptionsMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void notifyConnected() {

    }

    @Override
    public void notifyDisconnected() {

    }

    private Fragment getCurrentFragment(){
        return mFragManager.findFragmentById(R.id.glass_layout);
    }

    private void setUpGestureDetector(){
        if(GlassUtils.isGlassDevice()){
            mGestureDetector = new GestureDetector(getApplicationContext());
            mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
                @Override
                public boolean onGesture(Gesture gesture) {
                    return false;
                }
            });
        }
    }
}