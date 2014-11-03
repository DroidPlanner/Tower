package com.ox3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 10/28/14.
 */
public class Battery implements Parcelable {

    private final double batteryVoltage;
    private final double batteryRemain;
    private final double batteryCurrent;

    public Battery(double batteryVoltage, double batteryRemain, double batteryCurrent) {
        this.batteryVoltage = batteryVoltage;
        this.batteryRemain = batteryRemain;
        this.batteryCurrent = batteryCurrent;
    }

    public double getBatteryVoltage() {
        return batteryVoltage;
    }

    public double getBatteryRemain() {
        return batteryRemain;
    }

    public double getBatteryCurrent() {
        return batteryCurrent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Battery)) return false;

        Battery battery = (Battery) o;

        if (Double.compare(battery.batteryCurrent, batteryCurrent) != 0) return false;
        if (Double.compare(battery.batteryRemain, batteryRemain) != 0) return false;
        if (Double.compare(battery.batteryVoltage, batteryVoltage) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(batteryVoltage);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(batteryRemain);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(batteryCurrent);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Battery{" +
                "batteryVoltage=" + batteryVoltage +
                ", batteryRemain=" + batteryRemain +
                ", batteryCurrent=" + batteryCurrent +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.batteryVoltage);
        dest.writeDouble(this.batteryRemain);
        dest.writeDouble(this.batteryCurrent);
    }

    private Battery(Parcel in) {
        this.batteryVoltage = in.readDouble();
        this.batteryRemain = in.readDouble();
        this.batteryCurrent = in.readDouble();
    }

    public static final Parcelable.Creator<Battery> CREATOR = new Parcelable.Creator<Battery>() {
        public Battery createFromParcel(Parcel source) {
            return new Battery(source);
        }

        public Battery[] newArray(int size) {
            return new Battery[size];
        }
    };
}
