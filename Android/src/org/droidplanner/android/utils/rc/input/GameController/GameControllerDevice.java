package org.droidplanner.android.utils.rc.input.GameController;

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;

import org.droidplanner.android.utils.rc.input.GenericInputDevice;
import org.droidplanner.android.utils.rc.input.GameController.GameControllerMappingParser.Controller;
import org.droidplanner.android.utils.rc.input.GameController.GameControllerMappingParser.DoubleAxisRemap;
import org.droidplanner.android.utils.rc.input.GameController.GameControllerMappingParser.SingleAxisRemap;

import java.util.List;

public class GameControllerDevice extends GenericInputDevice {

    private Controller controller;
    private Handler mHandler;
    private boolean stopped;
    private MotionEvent lastMotionEvent;
    private List<SingleAxisRemap> remap;
    private List<DoubleAxisRemap> doubleRemap;

    public GameControllerDevice(Context context) {
        super(context);
        controller = GameControllerConfig.getInstance(context).getController();
        remap = controller.getAxisRemap();
        doubleRemap = controller.getDoubleAxisRemap();

        mHandler = new Handler();
        stopped = true;
        start();
    }

    @Override
    public void start() {
        if (stopped) {
            stopped = false;
            mHandler.post(secondInputMethod);
        }
    }

    @Override
    public void stop() {
        stopped = true;
        mHandler.removeCallbacks(secondInputMethod);
    }

    @Override
    public void onGenericMotionEvent(MotionEvent e) {
        super.onGenericMotionEvent(e);
        lastMotionEvent = e;

        for (int x = 0; x < remap.size(); x++) {
            SingleAxisRemap c = remap.get(x);
            setChannelValue(c.mDestination, lastMotionEvent.getAxisValue(c.mSourceAxis) * c.mMultiplierReversed);
        }

        if (isListenerBound()) {
            listener.onChannelsChanged(rc_channels);
        }
    }

    private void updateChannelsMode2() {
        if(lastMotionEvent == null)
            return;

        for (int x = 0; x < doubleRemap.size(); x++) {
            DoubleAxisRemap c = doubleRemap.get(x);
            if (c.mIncrementAxis != -1)
                setChannelValue(c.mDestination, rc_channels[c.mDestination] + lastMotionEvent.getAxisValue(c.mIncrementAxis) * 0.03f);
            if (c.mDecrementAxis != -1)
                setChannelValue(c.mDestination, rc_channels[c.mDestination] - lastMotionEvent.getAxisValue(c.mDecrementAxis) * 0.03f);
        }

        if (isListenerBound()) {
            listener.onChannelsChanged(rc_channels);
        }
    }

    Runnable secondInputMethod = new Runnable() {
        @Override
        public void run() {
            updateChannelsMode2();
            if (!stopped)
                mHandler.postDelayed(this, 50);
        }

    };

}
