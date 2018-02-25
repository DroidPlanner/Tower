package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class ChangeSpeed extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    private double speed;

    public ChangeSpeed(){
        super(MissionItemType.CHANGE_SPEED);
    }

    public ChangeSpeed(ChangeSpeed copy){
        super(MissionItemType.CHANGE_SPEED);
        speed = copy.speed;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "ChangeSpeed{" +
                "speed=" + speed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChangeSpeed)) return false;
        if (!super.equals(o)) return false;

        ChangeSpeed that = (ChangeSpeed) o;

        return Double.compare(that.speed, speed) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(speed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.speed);
    }

    private ChangeSpeed(Parcel in) {
        super(in);
        this.speed = in.readDouble();
    }

    @Override
    public MissionItem clone() {
        return new ChangeSpeed(this);
    }

    public static final Creator<ChangeSpeed> CREATOR = new Creator<ChangeSpeed>() {
        public ChangeSpeed createFromParcel(Parcel source) {
            return new ChangeSpeed(source);
        }

        public ChangeSpeed[] newArray(int size) {
            return new ChangeSpeed[size];
        }
    };
}
