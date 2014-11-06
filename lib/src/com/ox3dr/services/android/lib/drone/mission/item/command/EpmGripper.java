package com.ox3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItem;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class EpmGripper extends MissionItem implements MissionItem.Command {

    private boolean release;

    public EpmGripper(){
        super(MissionItemType.EPM_GRIPPER, "EPM Gripper");
    }

    public boolean isRelease() {
        return release;
    }

    public void setRelease(boolean release) {
        this.release = release;
    }

    public static final Creator<EpmGripper> CREATOR = new Creator<EpmGripper>() {
        @Override
        public EpmGripper createFromParcel(Parcel source) {
            return (EpmGripper) source.readSerializable();
        }

        @Override
        public EpmGripper[] newArray(int size) {
            return new EpmGripper[size];
        }
    };
}
