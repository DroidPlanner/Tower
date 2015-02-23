package org.droidplanner.android.utils.rc.input.GameController;

import android.content.Context;
import android.provider.SyncStateContract.Constants;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.utils.rc.RCConstants;
import org.droidplanner.android.utils.rc.input.GenericInputDevice;
import org.droidplanner.android.utils.rc.input.GameController.Controller.BaseCommand;
import org.droidplanner.android.utils.rc.input.GameController.Controller.ButtonRemap;
import org.droidplanner.android.utils.rc.input.GameController.Controller.DoubleAxisRemap;
import org.droidplanner.android.utils.rc.input.GameController.Controller.SingleAxisRemap;
import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.model.Drone;

public class GameControllerDevice extends GenericInputDevice {

    private Controller controller;

    public GameControllerDevice(Context context) {
        super(context);
        controller = GameControllerConfig.getInstance(context).getController();
    }

    @Override
    public void onGenericMotionEvent(MotionEvent lastMotionEvent) {
        super.onGenericMotionEvent(lastMotionEvent);

        for (int channel : RCConstants.rchannels) {
            BaseCommand channelCommand = controller.joystickRemap.get(channel);

            if (channelCommand == null || !channelCommand.isValid()) {
                continue; //Skip to next loop
            }
            else if (channelCommand instanceof SingleAxisRemap) {
                SingleAxisRemap c = (SingleAxisRemap) channelCommand;

                if (c.isValid())
                    setChannelValue(channel, lastMotionEvent.getAxisValue(c.Trigger)
                            * (c.isReversed ? -1 : 1));
            }
            else if (channelCommand instanceof DoubleAxisRemap)// DoubleAxisRemap
            {
                DoubleAxisRemap c = (DoubleAxisRemap) channelCommand;
                float incrementAxisValue = lastMotionEvent.getAxisValue(c.TriggerIncrement);
                float decrementAxisValue = lastMotionEvent.getAxisValue(c.TriggerDecrement);
                float newValue = incrementAxisValue - decrementAxisValue;
                setChannelValue(channel, newValue);
            }
        }

        /*
         * float deadzone = lastMotionEvent.getDevice().getMotionRange(0).getFlat(); Vector2
         * stickInput = new Vector2(100, 100); if(stickInput.len() < deadzone) stickInput =
         * Vector2.Zero; else stickInput = stickInput.nor().scl((stickInput.len() - deadzone) / (1 -
         * deadzone));
         */

        notifyChannelsChanged();
    }

    @Override
    public void onKeyUp(int keyCode, KeyEvent event) {
        super.onKeyUp(keyCode, event);

        for (int channel : RCConstants.rchannels) {
            BaseCommand channelCommand = controller.joystickRemap.get(channel);

            if (channelCommand != null && channelCommand.isValid()
                    && channelCommand instanceof ButtonRemap) {
                ButtonRemap remap = (ButtonRemap) channelCommand;
                if (remap.Trigger == keyCode) {
                    setChannelValue(channel, reverse(getChannelValue(channel)));
                }
            }
        }

        ButtonRemap remap = controller.buttonRemap.get(keyCode);
        if (remap != null && remap.isValid()) {
            switch (remap.Action) {
                case ButtonRemap.ARM_DISARM:
                    DroidPlannerApp app =
                            (DroidPlannerApp) this.context.getApplicationContext();
                    Drone drone = app.getDrone();
                    boolean armed = drone.getState().isArmed();
                    MavLinkArm.sendArmMessage(drone, !armed);
                    break;
            }
        }
        this.notifyChannelsChanged();
    }

    private float reverse(float channelValue) {
        if (channelValue < 0)
            return 1.0f;
        return -1.0f;
    }

}
