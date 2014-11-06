package com.ox3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItem;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class ChangeSpeed extends MissionItem implements MissionItem.Command {

    private double speed;

    public ChangeSpeed(){
        super(MissionItemType.CHANGE_SPEED);
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public static final Creator<ChangeSpeed> CREATOR = new Creator<ChangeSpeed>() {
        @Override
        public ChangeSpeed createFromParcel(Parcel source) {
            return (ChangeSpeed) source.readSerializable();
        }

        @Override
        public ChangeSpeed[] newArray(int size) {
            return new ChangeSpeed[size];
        }
    };
}
