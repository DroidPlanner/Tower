package org.droidplanner.android.utils.rc.input.GameController;

import android.content.Context;

import org.droidplanner.android.utils.file.IO.GameControllerConfigReaderWriter;
import org.droidplanner.android.utils.rc.input.GameController.Controller.BaseCommand;
import org.droidplanner.android.utils.rc.input.GameController.Controller.ButtonRemap;
import org.droidplanner.android.utils.rc.input.GameController.Controller.DoubleAxisRemap;
import org.droidplanner.android.utils.rc.input.GameController.Controller.SingleAxisRemap;

import java.util.Iterator;
import java.util.Map.Entry;

public class GameControllerConfig {
    private GameControllerConfigReaderWriter dataStore = new GameControllerConfigReaderWriter();
    private Controller controller;

    private static GameControllerConfig gcInstance;

    public static GameControllerConfig getInstance(Context context) {
        if (gcInstance == null) {
            gcInstance = new GameControllerConfig();
            gcInstance.load(context);
        }
        return gcInstance;
    }
    
    private void load(Context context) {
        controller = dataStore.load(context);
        if (controller == null)
            controller = new Controller();
    }

    public void save() {
        dataStore.save(controller);
    }

    public Controller getController() {
        return controller;
    }

    public boolean isSingleAxis(int channel) {
        BaseCommand command = controller.joystickRemap.get(channel);
        return command == null || command instanceof SingleAxisRemap
                || command instanceof ButtonRemap;
    }

    public boolean isSingleAxisButton(int channel) {
        BaseCommand command = controller.joystickRemap.get(channel);
        return command != null && command instanceof ButtonRemap;
    }
    
    public boolean isReversed(int channelId) {
        if(isSingleAxisButton(channelId))
            return false;
        if (isSingleAxis(channelId))
            return getSingleRemap(channelId).isReversed;

        return false;
    }

    public SingleAxisRemap getSingleRemap(int channel) {
        SingleAxisRemap remap = null;
        if (isSingleAxis(channel) && !isSingleAxisButton(channel)) {
            remap = (SingleAxisRemap) controller.joystickRemap.get(channel);
        }
        if (remap == null) {
            remap = new SingleAxisRemap();
            remap.Action = channel;

            controller.joystickRemap.put(channel, remap); // Put it in map if it doesnt exist
        }
        return remap;
    }

    public DoubleAxisRemap getDoubleRemap(int channel) {
        DoubleAxisRemap remap = null;
        if (!isSingleAxis(channel)) {
            remap = (DoubleAxisRemap) controller.joystickRemap.get(channel);
        }

        if (remap == null) {
            remap = new DoubleAxisRemap();
            remap.Action = channel;
            controller.joystickRemap.put(channel, remap); // Put it in map if it doesnt exist
        }
        return remap;
    }

    public ButtonRemap getButtonRemap(int keycode) {
        ButtonRemap remap = null;
        remap = controller.buttonRemap.get(keycode);
        if (remap == null) {
            remap = new ButtonRemap();
            remap.Trigger = keycode;
            controller.buttonRemap.put(keycode, remap);
        }
        return remap;
    }

    public ButtonRemap getJoystickButtonRemap(int channelId) {
        ButtonRemap remap;
        if (isSingleAxisButton(channelId)) {
            remap = (ButtonRemap) controller.joystickRemap.get(channelId);
        }
        else {
            remap = new ButtonRemap();
            remap.Action = channelId;
            controller.joystickRemap.put(channelId, remap);
        }
        return remap;
    }

    public void removeButtonRemapAction(int Action) {
        Iterator<Entry<Integer, ButtonRemap>> it = controller.buttonRemap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, ButtonRemap> pair = it.next();
            ButtonRemap value = (ButtonRemap) pair.getValue();
            if(value.Action == Action)
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

}
