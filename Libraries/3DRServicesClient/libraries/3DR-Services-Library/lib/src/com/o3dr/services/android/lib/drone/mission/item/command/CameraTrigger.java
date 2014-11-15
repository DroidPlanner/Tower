package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class CameraTrigger extends MissionItem implements MissionItem.Command {

    private double triggerDistance;

    public CameraTrigger(){
        super(MissionItemType.CAMERA_TRIGGER);
    }

    public double getTriggerDistance() {
        return triggerDistance;
    }

    public void setTriggerDistance(double triggerDistance) {
        this.triggerDistance = triggerDistance;
    }

    public static final Creator<CameraTrigger> CREATOR = new Creator<CameraTrigger>() {
        @Override
        public CameraTrigger createFromParcel(Parcel source) {
            return (CameraTrigger) source.readSerializable();
        }

        @Override
        public CameraTrigger[] newArray(int size) {
            return new CameraTrigger[size];
        }
    };

}
