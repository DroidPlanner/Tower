package org.droidplanner.android.utils.rc.input;

import static org.droidplanner.android.utils.rc.RCConstants.AILERON;
import static org.droidplanner.android.utils.rc.RCConstants.ELEVATOR;
import static org.droidplanner.android.utils.rc.RCConstants.RC5;
import static org.droidplanner.android.utils.rc.RCConstants.RC6;
import static org.droidplanner.android.utils.rc.RCConstants.RC7;
import static org.droidplanner.android.utils.rc.RCConstants.RC8;
import static org.droidplanner.android.utils.rc.RCConstants.RUDDER;
import static org.droidplanner.android.utils.rc.RCConstants.THROTTLE;

import android.content.Context;
import android.view.MotionEvent;

public abstract class GenericInputDevice {

    public interface IRCEvents {
        public abstract void onChannelsChanged(float[] channels);
    }

    protected IRCEvents listener;
    protected Context context;
    protected float[] rc_channels = new float[8];

    public GenericInputDevice(Context context) {
        this.context = context;

        rc_channels[AILERON] = 0.0f;
        rc_channels[ELEVATOR] = 0.0f;
        rc_channels[THROTTLE] = -1.0f;
        rc_channels[RUDDER] = 0.0f;

        rc_channels[RC5] = -1.0f;
        rc_channels[RC6] = -1.0f;
        rc_channels[RC7] = -1.0f;
        rc_channels[RC8] = -1.0f;
    }

    protected void setChannelValue(int channel, float value) {
        if (value > 1.0f)
            value = 1.0f;
        else if (value < -1.0f)
            value = -1.0f;
        rc_channels[channel] = value;
    }

    public void registerListener(IRCEvents listener) {
        this.listener = listener;
    }

    public void unregisterListener() {
        listener = null;
    }

    public void onGenericMotionEvent(MotionEvent e) {
        // Do nothing, let subclass implement if necessary
    }

    protected boolean isListenerBound() {
        return listener != null;
    }
    protected void notifyChannelsChanged() {
        if (isListenerBound()) {
            listener.onChannelsChanged(rc_channels);
        }
    }
}
