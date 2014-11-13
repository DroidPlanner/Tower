package com.ox3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItem;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class SetServo extends MissionItem implements MissionItem.Command {

    private int pwm;
    private int channel;

    public SetServo(){
        super(MissionItemType.SET_SERVO);
    }

    public int getPwm() {
        return pwm;
    }

    public void setPwm(int pwm) {
        this.pwm = pwm;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public static final Creator<SetServo> CREATOR = new Creator<SetServo>() {
        @Override
        public SetServo createFromParcel(Parcel source) {
            return (SetServo) source.readSerializable();
        }

        @Override
        public SetServo[] newArray(int size) {
            return new SetServo[size];
        }
    };
}
