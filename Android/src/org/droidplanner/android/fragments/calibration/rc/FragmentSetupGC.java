package org.droidplanner.android.fragments.calibration.rc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.R;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.activities.interfaces.PhysicalDeviceEvents;
import org.droidplanner.android.dialogs.UninterruptingDialog;
import org.droidplanner.android.utils.rc.RCConstants;
import org.droidplanner.android.utils.rc.RCControlManager;
import org.droidplanner.android.utils.rc.input.AxisFinder;
import org.droidplanner.android.utils.rc.input.GenericInputDevice.IRCEvents;
import org.droidplanner.android.utils.rc.input.GameController.GameControllerConfig;
import org.droidplanner.android.widgets.rcchannel.GameControllerChannel;

public class FragmentSetupGC extends Fragment implements
        GameControllerChannel.GameControllerChannelEvents, PhysicalDeviceEvents, IRCEvents {

    private GameControllerConfig gcConfig;
    private GameControllerChannel[] channels;

    private RCControlManager rcOutput;
    private UninterruptingDialog dialog;
    private int nextChannelToAssignMode;
    private int nextChannelToAssign = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setup_gc_main, container, false);

        gcConfig = GameControllerConfig.getInstance(getActivity());

        String packageName = this.getActivity().getPackageName();
        channels = new GameControllerChannel[RCConstants.rchannels.length];
        for(int x = 0; x < RCConstants.rchannels.length; x++) {
            int id = getResources().getIdentifier("gc" + RCConstants.STRINGRCCHANNELS[x], "id", packageName);
            GameControllerChannel current = (GameControllerChannel) view.findViewById(id);
            current.IDENTIFIYING_CHANNEL_KEY = RCConstants.rchannels[x];
            current.setCheckedWithoutEvent(gcConfig.isReversed(RCConstants.rchannels[x]));
            current.setFirstMode(!gcConfig.isAssigned(current.IDENTIFIYING_CHANNEL_KEY, RCConstants.MODE_INCREMENTKEY));
            channels[x] = current;
        }

        ((SuperUI) getActivity()).registerPhysicalDeviceEventListener(this);
        rcOutput = new RCControlManager(this.getActivity());
        rcOutput.registerListener(this);

        dialog = new UninterruptingDialog(this.getActivity());
        dialog.setTitle("Waiting for input...");
        dialog.setIndeterminate(true);
        dialog.setMessage("Move Joystick to autodetect");
        dialog.setCancelable(true);
        dialog.setButton(ProgressDialog.BUTTON_NEUTRAL, "Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelJoystickAssign();
                        dialog.dismiss();
                    }

                });
        dialog.registerPhysicalDeviceEventsListener(this);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        for (int x = 0; x < channels.length; x++) {
            if (channels[RCConstants.rchannels[x]].isFirstMode())
                gcConfig.remove(RCConstants.rchannels[x], RCConstants.MODE_INCREMENTKEY);
            else
                gcConfig.remove(RCConstants.rchannels[x], RCConstants.MODE_SINGLEKEY);
            channels[x].setListener(null);
        }
        gcConfig.save();
    }

    @Override
    public void onResume() {
        super.onResume();
        for(int x = 0; x < channels.length; x++) {
            channels[x].setListener(this);
            channels[x].setCheckedWithoutEvent(gcConfig.isReversed(RCConstants.rchannels[x]));
        }
    }

    @Override
    public void onChannelsChanged(float[] channelsValue) {
        for (int x = 0; x < channels.length; x++)
            channels[x].setValue((int) channelsValue[RCConstants.rchannels[x]]);
    }

    @Override
    public void physicalJoyMoved(MotionEvent event) {
        rcOutput.onGenericMotionEvent(event);
        if (nextChannelToAssign != -1 && AxisFinder.figureOutAxis(event)) {
            gcConfig.assign(nextChannelToAssign, AxisFinder.getFiguredOutAxis(), nextChannelToAssignMode);
            cancelJoystickAssign();
            dialog.dismiss();
        }
    }

    public static String getTitle(Context c) {
        return "Game Controller Setup";
    }

    @Override
    public void OnSingleKeyPressed(GameControllerChannel gameControllerChannel) {
        nextChannelToAssignMode = RCConstants.MODE_SINGLEKEY;
        keyPressed(gameControllerChannel);
    }

    @Override
    public void OnIncrementPressed(GameControllerChannel gameControllerChannel) {
        nextChannelToAssignMode = RCConstants.MODE_INCREMENTKEY;
        keyPressed(gameControllerChannel);
    }

    @Override
    public void OnDecrementPressed(GameControllerChannel gameControllerChannel) {
        nextChannelToAssignMode = RCConstants.MODE_DECREMENTKEY;
        keyPressed(gameControllerChannel);
    }

    private void keyPressed(GameControllerChannel gameControllerChannel) {
        nextChannelToAssign = gameControllerChannel.IDENTIFIYING_CHANNEL_KEY;
        dialog.show();
    }

    @Override
    public void OnCheckedReverseChanged(GameControllerChannel v, boolean reversed) {
        gcConfig.setReversed(v.IDENTIFIYING_CHANNEL_KEY, reversed);
    }

    protected void cancelJoystickAssign() {
        nextChannelToAssign = -1;
    }

}
