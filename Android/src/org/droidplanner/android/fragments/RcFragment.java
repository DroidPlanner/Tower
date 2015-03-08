package org.droidplanner.android.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.o3dr.android.client.Drone;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.activities.helpers.ControllerEventCaptureView;
import org.droidplanner.android.activities.interfaces.PhysicalDeviceEvents;
import org.droidplanner.android.helpers.RcOutput;
import org.droidplanner.android.utils.rc.RCConstants;
import org.droidplanner.android.utils.rc.RCControlManager;
import org.droidplanner.android.utils.rc.input.GenericInputDevice.IRCEvents;

public class RcFragment extends Fragment implements IRCEvents, PhysicalDeviceEvents {

    //private TextView textViewThrottle, textViewRudder, textViewAileron, textViewElevator;

    private RCControlManager rcManager;
    private RcOutput rcOutput;
    private Drone drone;

    private TelemetryFragment telemetryFragment;
    public static final String SAVED_RC_VALUES = "saved_rc_values";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        drone = ((DroidPlannerApp) this.getActivity().getApplicationContext()).getDrone();
        rcOutput = new RcOutput(drone, this.getActivity());
        rcOutput.enableRcOverride();
        rcManager = new RCControlManager(this.getActivity());
        rcManager.registerListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rc, container, false);

        telemetryFragment = ((FlightActivity) this.getActivity()).telemetryFragment; //TODO make listener in telemetry fragment instead of updating it like this

        Intent intent = new Intent(this.getActivity().getApplicationContext(), GlobalMotionEventListener.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null) {
            rcManager.mDevice.setRCChannels(savedInstanceState.getFloatArray(SAVED_RC_VALUES));
            rcManager.mDevice.notifyChannelsChanged();
        }
    }

    private GlobalMotionEventListener mService;
    public boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GlobalMotionEventListener.LocalBinder binder = (GlobalMotionEventListener.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            registerListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void registerListener() {
        mService.getMotionView().registerPhysicalDeviceEventListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rcManager.registerListener(null);
        rcOutput.disableRcOverride();

        if (mBound) {
            mService.stopSelf();
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onChannelsChanged(float[] channels) {
        telemetryFragment.updateControllerStatus(channels);

        for(int x = 0; x < RCConstants.rchannels.length; x++) {
            rcOutput.setRcChannel(RCConstants.rchannels[x], channels[RCConstants.rchannels[x]]);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloatArray(SAVED_RC_VALUES, rcManager.mDevice.getRCChannels());
    }

    @Override
    public void physicalJoyMoved(MotionEvent event) {
        rcManager.onGenericMotionEvent(event);
    }

    @Override
    public void physicalKeyUp(int keyCode, KeyEvent event) {
        rcManager.onKeyUp(keyCode, event);
    }
}
