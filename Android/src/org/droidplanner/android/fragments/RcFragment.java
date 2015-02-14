package org.droidplanner.android.fragments;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.helpers.ControllerEventCaptureView;
import org.droidplanner.android.activities.interfaces.PhysicalDeviceEvents;
import org.droidplanner.android.helpers.RcOutput;
import org.droidplanner.android.utils.rc.RCConstants;
import org.droidplanner.android.utils.rc.RCControlManager;
import org.droidplanner.android.utils.rc.input.GenericInputDevice.IRCEvents;
import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.model.Drone;

public class RcFragment extends Fragment implements IRCEvents, PhysicalDeviceEvents {

    private TextView textViewThrottle, textViewRudder, textViewAileron, textViewElevator;

    private RCControlManager rcManager;
    private RcOutput rcOutput;
    private Drone drone;
    
    private ControllerEventCaptureView eventsView;
    private WindowManager wm;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        eventsView = new ControllerEventCaptureView(this.getActivity());
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                5,
                5,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSPARENT);
        eventsView.registerPhysicalDeviceEventListener(this);
        
        wm = (WindowManager) this.getActivity().getSystemService(Activity.WINDOW_SERVICE);
        wm.addView(eventsView, params);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rc, container, false);

        textViewThrottle = (TextView) view.findViewById(R.id.textViewRCThrottle);
        textViewThrottle.setText("Move");

        textViewRudder = (TextView) view.findViewById(R.id.textViewRCRudder);
        textViewRudder.setText("Controller");

        textViewElevator = (TextView) view.findViewById(R.id.textViewRCElevator);
        textViewElevator.setText("To");

        textViewAileron = (TextView) view.findViewById(R.id.textViewRCAileron);
        textViewAileron.setText("Initialize");

        drone = ((DroidPlannerApp) this.getActivity().getApplication()).getDrone();
        rcOutput = new RcOutput(drone, this.getActivity());
        rcManager = new RCControlManager(this.getActivity());
        rcManager.registerListener(this);
        rcOutput.enableRcOverride();
        return view;
    }
    
    @Override
    public void onStop() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rcManager.registerListener(null);
        rcOutput.disableRcOverride();
        wm.removeView(eventsView);
    }

    @Override
    public void onChannelsChanged(float[] channels) {
        textViewThrottle.setText("Thrt: " + Math.round(channels[RCConstants.THROTTLE]) + "");
        textViewRudder.setText("Rudd: " + Math.round(channels[RCConstants.RUDDER]) + "");
        textViewElevator.setText("Elev: " + Math.round(channels[RCConstants.ELEVATOR]) + "");
        textViewAileron.setText("Ail: " + Math.round(channels[RCConstants.AILERON]) + "");

        for(int x = 0; x < RCConstants.rchannels.length; x++) {
            rcOutput.setRcChannel(RCConstants.rchannels[x], channels[RCConstants.rchannels[x]]);
        }
    }

    @Override
    public void physicalJoyMoved(MotionEvent event) { // Will be changed to send InputDevice to RcControlManager
        rcManager.onGenericMotionEvent(event);
    }

    @Override
    public void physicalKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_START) {
            if (drone.isConnectionAlive())
                MavLinkArm.sendArmMessage(drone, !drone.getState().isArmed());
        }
    }
}
