package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 10/28/14.
 */
public class Speed implements DroneAttribute {

    private double verticalSpeed; // m/s
    private double groundSpeed; // m/s
    private double airSpeed; // m/s

    public Speed(){}

    public Speed(double verticalSpeed, double groundSpeed, double airSpeed) {
        this.verticalSpeed = verticalSpeed;
        this.groundSpeed = groundSpeed;
        this.airSpeed = airSpeed;
    }

    public void setVerticalSpeed(double verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    public void setGroundSpeed(double groundSpeed) {
        this.groundSpeed = groundSpeed;
    }

    public void setAirSpeed(double airSpeed) {
        this.airSpeed = airSpeed;
    }

    public double getVerticalSpeed() {
        return verticalSpeed;
    }

    public double getGroundSpeed() {
        return groundSpeed;
    }

    public double getAirSpeed() {
        return airSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Speed)) return false;

        Speed speed = (Speed) o;

        if (Double.compare(speed.airSpeed, airSpeed) != 0) return false;
        if (Double.compare(speed.groundSpeed, groundSpeed) != 0) return false;
        if (Double.compare(speed.verticalSpeed, verticalSpeed) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(verticalSpeed);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(groundSpeed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(airSpeed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Speed{" +
                "verticalSpeed=" + verticalSpeed +
                ", groundSpeed=" + groundSpeed +
                ", airSpeed=" + airSpeed +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.verticalSpeed);
        dest.writeDouble(this.groundSpeed);
        dest.writeDouble(this.airSpeed);
    }

    private Speed(Parcel in) {
        this.verticalSpeed = in.readDouble();
        this.groundSpeed = in.readDouble();
        this.airSpeed = in.readDouble();
    }

    public static final Parcelable.Creator<Speed> CREATOR = new Parcelable.Creator<Speed>() {
        public Speed createFromParcel(Parcel source) {
            return new Speed(source);
        }

        public Speed[] newArray(int size) {
            return new Speed[size];
        }
    };
}
