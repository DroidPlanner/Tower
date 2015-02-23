package org.droidplanner.android.utils.rc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import org.droidplanner.android.fragments.calibration.rc.FragmentSetupRC;
import org.droidplanner.android.utils.rc.input.GenericInput;
import org.droidplanner.android.utils.rc.input.GenericInputDevice;
import org.droidplanner.android.utils.rc.input.GenericInputDevice.IRCEvents;

import java.util.Vector;

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
    }

    private void updateRange() {
        rc_max_throttle = 1000 + 10 * prefs.getInt(FragmentSetupRC.PREFTHROTTLELIMITKEY, 100);
        rc_range = 5 * prefs.getInt(FragmentSetupRC.PREFRANGELIMITKEY, 100);
    }

    public void registerListener(IRCEvents listener) {
        this.listener = listener;
    }

    @Override
    public void onChannelsChanged(float[] channels) {
        int lastControlChannelIndex = 3;
        for (int x = 0; x < lastControlChannelIndex; x++)
            channels[x] = smooth(channels[x]);

        float[] output = new float[channels.length];
        for (int x = 0; x < output.length; x++) {
            if (x <= lastControlChannelIndex)
                output[x] = channels[x] * rc_range + rc_med;
            else
                output[x] = channels[x] * 500 + rc_med;
        }
        output[RCConstants.THROTTLE] = channels[RCConstants.THROTTLE] * 500 + rc_med;
        output[RCConstants.THROTTLE] = Math.min(output[RCConstants.THROTTLE], rc_max_throttle);

        if (listener != null)
            listener.onChannelsChanged(output);
    }

    private float smooth(float f) {
        double newValue = Math.pow(f, 2);
        if (f < 0)
            newValue = -newValue;
        return (float) newValue;
    }

    public void onGenericMotionEvent(MotionEvent e) {
        mDevice.onGenericMotionEvent(e);
    }

    public void onKeyUp(int keyCode, KeyEvent event) {
        mDevice.onKeyUp(keyCode, event);
    }
}
