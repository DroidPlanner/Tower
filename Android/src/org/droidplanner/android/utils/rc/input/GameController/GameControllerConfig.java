package org.droidplanner.android.utils.rc.input.GameController;

import android.content.Context;

import org.droidplanner.android.utils.file.IO.GameControllerConfigReaderWriter;
import org.droidplanner.android.utils.rc.RCConstants;
import org.droidplanner.android.utils.rc.input.GameController.GameControllerMappingParser.Controller;
import org.droidplanner.android.utils.rc.input.GameController.GameControllerMappingParser.SingleAxisRemap;

public class GameControllerConfig {
    static GameControllerConfig gcInstance;
    GameControllerMappingParser mapping;
    private Controller controller;

    public static GameControllerConfig getInstance(Context context) {
        if (gcInstance == null) {
            gcInstance = new GameControllerConfig();
            gcInstance.load(context);
        }
        return gcInstance;
    }

    public void load(Context context) {
        GameControllerConfigReaderWriter reader = new GameControllerConfigReaderWriter();
        mapping = reader.load(context);
        controller = mapping.mappingController;
    }

    public void save() {
        GameControllerConfigReaderWriter writer = new GameControllerConfigReaderWriter();
        writer.save(mapping.getJSON());
    }

    public void assign(int channel, int axis, int assign_mode) {
        switch (assign_mode) {
            case RCConstants.MODE_SINGLEKEY:
                controller.getOrCreateSingleAxisRemap(channel).mSourceAxis = axis;
                break;
            case RCConstants.MODE_INCREMENTKEY:
                controller.getOrCreateDoubleAxisRemap(channel).mIncrementAxis = axis;
                break;
            case RCConstants.MODE_DECREMENTKEY:
                controller.getOrCreateDoubleAxisRemap(channel).mDecrementAxis = axis;
                break;
        }
    }
    public boolean isAssigned(int channel, int assign_mode) {
        switch (assign_mode) {
            case RCConstants.MODE_SINGLEKEY:
                return controller.getSingleAxisRemap(channel) != null;
            case RCConstants.MODE_INCREMENTKEY:
            case RCConstants.MODE_DECREMENTKEY:
                return controller.getDoubleAxisRemap(channel) != null;
        }
        return false;
    }
    public void remove(int channel, int assign_mode) {
        switch (assign_mode) {
            case RCConstants.MODE_SINGLEKEY:
                controller.removeSingleKeyRemap(channel);
                break;
            case RCConstants.MODE_INCREMENTKEY:
            case RCConstants.MODE_DECREMENTKEY:
                controller.removeDoubleKeyRemap(channel);
                break;
        }
    }

    public void setReversed(int channel, boolean isReversed) {
        setReversed(channel, isReversed ? -1 : 1);
    }
    private void setReversed(int channel, int reversed) {
        controller.getOrCreateSingleAxisRemap(channel).mMultiplierReversed = reversed;
    }

    public boolean isReversed(int channel) {
        SingleAxisRemap remap = controller.getSingleAxisRemap(channel);
        return remap != null && remap.mMultiplierReversed == -1;
    }

    public Controller getController() {
        return controller;
    }
}
