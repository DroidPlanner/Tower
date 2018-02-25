package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;

/**
 * Created by fhuya on 10/28/14.
 */
public class Battery implements DroneAttribute {

    private double batteryVoltage;
    private double batteryRemain;
    private double batteryCurrent;
    private Double batteryDischarge;

    public Battery(){}

    public Battery(double batteryVoltage, double batteryRemain, double batteryCurrent,
                   Double batteryDischarge) {
        this.batteryVoltage = batteryVoltage;
        this.batteryRemain = batteryRemain;
        this.batteryCurrent = batteryCurrent;
        this.batteryDischarge = batteryDischarge;
    }

    public void setBatteryVoltage(double batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public void setBatteryRemain(double batteryRemain) {
        this.batteryRemain = batteryRemain;
    }

    public void setBatteryCurrent(double batteryCurrent) {
        this.batteryCurrent = batteryCurrent;
    }

    public void setBatteryDischarge(Double batteryDischarge) {
        this.batteryDischarge = batteryDischarge;
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

    public Double getBatteryDischarge() {
        return batteryDischarge;
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
        dest.writeValue(this.batteryDischarge);
    }

    private Battery(Parcel in) {
        this.batteryVoltage = in.readDouble();
        this.batteryRemain = in.readDouble();
        this.batteryCurrent = in.readDouble();
        this.batteryDischarge = (Double) in.readValue(Double.class.getClassLoader());
    }

    public static final Creator<Battery> CREATOR = new Creator<Battery>() {
        public Battery createFromParcel(Parcel source) {
            return new Battery(source);
        }

        public Battery[] newArray(int size) {
            return new Battery[size];
        }
    };
}
