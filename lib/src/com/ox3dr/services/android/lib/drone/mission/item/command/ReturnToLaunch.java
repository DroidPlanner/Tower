package com.ox3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItem;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class ReturnToLaunch extends MissionItem implements MissionItem.Command{

    private double returnAltitude;

    public ReturnToLaunch(){
        super(MissionItemType.RETURN_TO_LAUNCH);
    }

    public double getReturnAltitude() {
        return returnAltitude;
    }

    public void setReturnAltitude(double returnAltitude) {
        this.returnAltitude = returnAltitude;
    }

    public static final Creator<ReturnToLaunch> CREATOR = new Creator<ReturnToLaunch>() {
        @Override
        public ReturnToLaunch createFromParcel(Parcel source) {
            return (ReturnToLaunch) source.readSerializable();
        }

        @Override
        public ReturnToLaunch[] newArray(int size) {
            return new ReturnToLaunch[size];
        }
    };
}
