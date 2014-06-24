package org.droidplanner.android.glass.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;

import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.utils.DroidplannerPrefs;
import org.droidplanner.core.drone.DroneInterfaces;

/**
 * This is the main activity for the glass interface.
 */
public abstract class GlassUI extends SuperUI implements DroneInterfaces.OnDroneListener {

    /**
     * Handle to the app preferences.
     */
    protected DroidplannerPrefs mPrefs;

    /**
     * Glass gesture detector.
     * Detects glass specific swipes, and taps, and uses it for navigation.
     */
    protected GestureDetector mGestureDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        super.onCreate(savedInstanceState);

        final Context context = getApplicationContext();
        mPrefs = new DroidplannerPrefs(context);
        mGestureDetector = new GestureDetector(context);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null && event.getSource() == InputDevice.SOURCE_TOUCHPAD) {
            return mGestureDetector.onMotionEvent(event);
        }
        return super.onGenericMotionEvent(event);
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

        if(keyCode == KeyEvent.KEYCODE_CAMERA){
            //For debug purposes, trigger recording of the glass screen using 'screenrecord',
            // as well as recording through the glass camera.

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_CAMERA){
            //For debug purposes, trigger recording of the glass screen using 'screenrecord',
            // as well as recording through the glass camera.
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }
}
