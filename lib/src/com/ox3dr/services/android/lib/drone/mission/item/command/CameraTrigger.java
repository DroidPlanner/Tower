package com.ox3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItem;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

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

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        super.writeToParcel(dest, flags);
        dest.writeDouble(triggerDistance);
    }

    protected CameraTrigger(Parcel in){
        super(in);
        this.triggerDistance = in.readDouble();
    }

    public static final Creator<CameraTrigger> CREATOR = new Creator<CameraTrigger>() {
        @Override
        public CameraTrigger createFromParcel(Parcel source) {
            return new CameraTrigger(source);
        }

        @Override
        public CameraTrigger[] newArray(int size) {
            return new CameraTrigger[size];
        }
    };

}
