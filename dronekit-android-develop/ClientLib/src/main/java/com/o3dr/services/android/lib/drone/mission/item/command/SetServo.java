package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

/**
 * Mission command used to move a servo to a particular pwm value.
 */
public class SetServo extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    private int pwm;
    private int channel;

    public SetServo(){
        super(MissionItemType.SET_SERVO);
    }

    public SetServo(SetServo copy){
        this();
        pwm = copy.pwm;
        channel = copy.channel;
    }

    /**
     * @return PWM value to output to the servo
     */
    public int getPwm() {
        return pwm;
    }

    /**
     * Set PWM value to output to the servo
     * @param pwm value to output to the servo
     */
    public void setPwm(int pwm) {
        this.pwm = pwm;
    }

    /**
     * @return the output channel the servo is attached to
     */
    public int getChannel() {
        return channel;
    }

    /**
     * @param channel the output channel the servo is attached to
     */
    public void setChannel(int channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "SetServo{" +
                "channel=" + channel +
                ", pwm=" + pwm +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SetServo)) return false;
        if (!super.equals(o)) return false;

        SetServo setServo = (SetServo) o;

        if (pwm != setServo.pwm) return false;
        return channel == setServo.channel;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + pwm;
        result = 31 * result + channel;
        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.pwm);
        dest.writeInt(this.channel);
    }

    private SetServo(Parcel in) {
        super(in);
        this.pwm = in.readInt();
        this.channel = in.readInt();
    }

    @Override
    public MissionItem clone() {
        return new SetServo(this);
    }

    public static final Creator<SetServo> CREATOR = new Creator<SetServo>() {
        public SetServo createFromParcel(Parcel source) {
            return new SetServo(source);
        }

        public SetServo[] newArray(int size) {
            return new SetServo[size];
        }
    };
}
