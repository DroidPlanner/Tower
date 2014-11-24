package org.droidplanner.android.utils.rc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.view.MotionEvent;

import org.droidplanner.android.fragments.calibration.rc.FragmentSetupRC;
import org.droidplanner.android.utils.rc.input.GenericInput;
import org.droidplanner.android.utils.rc.input.GenericInputDevice;
import org.droidplanner.android.utils.rc.input.GenericInputDevice.IRCEvents;

public class RCControlManager implements IRCEvents {
    GenericInput input = GenericInput.CONTROLLER;
    GenericInputDevice mDevice;
    Context context;
    private IRCEvents listener;

    private int rc_med = 1500;
    private int rc_range = 300;
    private int rc_max_throttle = 1800;
    private final SharedPreferences prefs;
    OnSharedPreferenceChangeListener prefChangeListener;

    public RCControlManager(Context context) {
        this.context = context;
        mDevice = input.getInstance(context);
        mDevice.registerListener(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefChangeListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                updateRange();
            }

        };
        prefs.registerOnSharedPreferenceChangeListener(prefChangeListener);
        updateRange();
        mDevice.start();
    }
    private void updateRange() {
        rc_max_throttle = 1000 + 10 * prefs.getInt(FragmentSetupRC.PREFTHROTTLELIMITKEY, 100);
        rc_range = 5 * prefs.getInt(FragmentSetupRC.PREFRANGELIMITKEY, 100);
    }
    public void registerListener(IRCEvents listener) {
        this.listener = listener;
    }

    public void unregisterListener() {
        listener = null;
    }

    @Override
    public void onChannelsChanged(float[] channels) {
        float[] output = new float[channels.length];
        for (int x = 0; x < output.length; x++) {
            output[x] = channels[x] * rc_range + rc_med;
        }
        output[RCConstants.THROTTLE] = channels[RCConstants.THROTTLE] * 500 + rc_med;
        if (output[RCConstants.THROTTLE] > rc_max_throttle)
            output[RCConstants.THROTTLE] = rc_max_throttle;

        if (listener != null)
            listener.onChannelsChanged(output);
    }

    public void onGenericMotionEvent(MotionEvent e) {
        mDevice.onGenericMotionEvent(e);
    }
}
