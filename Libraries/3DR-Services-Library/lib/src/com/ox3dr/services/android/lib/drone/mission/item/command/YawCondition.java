package com.ox3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItem;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/10/14.
 */
public class YawCondition extends MissionItem implements MissionItem.Command {

    private double angle;
    private double angularSpeed;
    private boolean isRelative;

    public YawCondition(){
        super(MissionItemType.YAW_CONDITION);
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngularSpeed() {
        return angularSpeed;
    }

    public void setAngularSpeed(double angularSpeed) {
        this.angularSpeed = angularSpeed;
    }

    public boolean isRelative() {
        return isRelative;
    }

    public void setRelative(boolean isRelative) {
        this.isRelative = isRelative;
    }

    public static final Creator<YawCondition> CREATOR = new Creator<YawCondition>() {
        @Override
        public YawCondition createFromParcel(Parcel source) {
            return (YawCondition) source.readSerializable();
        }

        @Override
        public YawCondition[] newArray(int size) {
            return new YawCondition[size];
        }
    };
}
