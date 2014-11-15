package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 10/28/14.
 */
public class Attitude implements Parcelable {

    private  double roll;
    private  double pitch;
    private  double yaw;

    public Attitude(){}

    public Attitude(double roll, double pitch, double yaw) {
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public double getRoll() {
        return roll;
    }

    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attitude)) return false;

        Attitude attitude = (Attitude) o;

        if (Double.compare(attitude.pitch, pitch) != 0) return false;
        if (Double.compare(attitude.roll, roll) != 0) return false;
        if (Double.compare(attitude.yaw, yaw) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(roll);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(pitch);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(yaw);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Attitude{" +
                "roll=" + roll +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.roll);
        dest.writeDouble(this.pitch);
        dest.writeDouble(this.yaw);
    }

    private Attitude(Parcel in) {
        this.roll = in.readDouble();
        this.pitch = in.readDouble();
        this.yaw = in.readDouble();
    }

    public static final Parcelable.Creator<Attitude> CREATOR = new Parcelable.Creator<Attitude>() {
        public Attitude createFromParcel(Parcel source) {
            return new Attitude(source);
        }

        public Attitude[] newArray(int size) {
            return new Attitude[size];
        }
    };
}
