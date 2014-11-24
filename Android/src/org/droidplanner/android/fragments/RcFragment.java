package org.droidplanner.android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.activities.interfaces.PhysicalDeviceEvents;
import org.droidplanner.android.helpers.RcOutput;
import org.droidplanner.android.utils.rc.RCConstants;
import org.droidplanner.android.utils.rc.RCControlManager;
import org.droidplanner.android.utils.rc.input.GenericInputDevice.IRCEvents;
import org.droidplanner.core.model.Drone;

public class RcFragment extends Fragment implements IRCEvents, PhysicalDeviceEvents {

    private TextView textViewThrottle, textViewRudder, textViewAileron, textViewElevator;

    private RCControlManager rcManager;
    private RcOutput rcOutput;

    private Switch switchTurnOnTurnOff;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rc, container, false);

        textViewThrottle = (TextView) view.findViewById(R.id.textViewRCThrottle);
        textViewThrottle.setText("(Thrt: 0%)");

        textViewRudder = (TextView) view.findViewById(R.id.textViewRCRudder);
        textViewRudder.setText("(Rudd: 0%)");

        textViewElevator = (TextView) view.findViewById(R.id.textViewRCElevator);
        textViewElevator.setText("(Elev: 0%)");

        textViewAileron = (TextView) view.findViewById(R.id.textViewRCAileron);
        textViewAileron.setText("(Ail: 0%)");

        switchTurnOnTurnOff = (Switch) view.findViewById(R.id.switchTurnOnTurnOff);
        switchTurnOnTurnOff.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    rcOutput.enableRcOverride();
                else
                    rcOutput.disableRcOverride();
            }

        });
        Drone drone = ((DroidPlannerApp) this.getActivity().getApplication()).getDrone();
        rcOutput = new RcOutput(drone, this.getActivity());
        rcManager = new RCControlManager(this.getActivity());
        ((SuperUI) getActivity()).registerPhysicalDeviceEventListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        rcManager.registerListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        rcManager.registerListener(null);
    }

    @Override
    public void onStop() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

}
