package com.ox3dr.services.android.lib.drone.mission.item.command;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class CameraTrigger extends MissionCommand {

    private double triggerDistance;

    public CameraTrigger(double triggerDistance){
//        super(MissionItemType.CAMERA_TRIGGER);
        this.triggerDistance = triggerDistance;
    }
}
