package com.droidplanner.glass.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.glass.fragments.HudFragment;
import com.droidplanner.glass.utils.GlassUtils;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glass);

        final FragmentManager fm = getFragmentManager();
        Fragment glassFragment = fm.findFragmentById(R.id.glass_layout);
        if(glassFragment == null){
            glassFragment = new HudFragment();
            fm.beginTransaction().add(R.id.glass_layout, glassFragment).commit();
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
        getMenuInflater().inflate(R.menu.menu_super_activiy, menu);
        return true;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event){
        if(mGestureDetector != null){
            return mGestureDetector.onMotionEvent(event);
        }
        return super.onGenericMotionEvent(event);
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
}