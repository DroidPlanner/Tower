package org.droidplanner.android.utils.rc.input.GameController;

import android.content.Context;
import android.provider.SyncStateContract.Constants;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.droidplanner.android.utils.rc.RCConstants;
import org.droidplanner.android.utils.rc.input.GenericInputDevice;
import org.droidplanner.android.utils.rc.input.GameController.Controller.BaseCommand;
import org.droidplanner.android.utils.rc.input.GameController.Controller.DoubleAxisRemap;
import org.droidplanner.android.utils.rc.input.GameController.Controller.SingleAxisRemap;

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
                break;
            }
            else if (channelCommand instanceof SingleAxisRemap) {
                SingleAxisRemap c = (SingleAxisRemap) channelCommand;

                if (c.isValid())
                    setChannelValue(channel, lastMotionEvent.getAxisValue(c.Trigger)
                            * (c.isReversed ? -1 : 1));
            }
            else if(channelCommand instanceof DoubleAxisRemap)// DoubleAxisRemap
            {
                DoubleAxisRemap c = (DoubleAxisRemap) channelCommand;
                float incrementAxisValue = lastMotionEvent.getAxisValue(c.TriggerIncrement);
                float decrementAxisValue = lastMotionEvent.getAxisValue(c.TriggerDecrement);
                float newValue = incrementAxisValue - decrementAxisValue;
                setChannelValue(channel, newValue);
            }
        }
        
        notifyChannelsChanged();
    }

    public void onKeyUp(int keycode, KeyEvent event) {

    }
}
