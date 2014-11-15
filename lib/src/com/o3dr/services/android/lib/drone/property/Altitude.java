package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 10/28/14.
 */
public class Altitude implements Parcelable {

    private double altitude;
    private double targetAltitude;

    public Altitude(){}

    public Altitude(double altitude, double targetAltitude) {
        this.altitude = altitude;
        this.targetAltitude = targetAltitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getTargetAltitude() {
        return targetAltitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setTargetAltitude(double targetAltitude) {
        this.targetAltitude = targetAltitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Altitude)) return false;

        Altitude altitude1 = (Altitude) o;

        if (Double.compare(altitude1.altitude, altitude) != 0) return false;
        if (Double.compare(altitude1.targetAltitude, targetAltitude) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(altitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(targetAltitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Altitude{" +
                "altitude=" + altitude +
                ", targetAltitude=" + targetAltitude +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.altitude);
        dest.writeDouble(this.targetAltitude);
    }

    private Altitude(Parcel in) {
        this.altitude = in.readDouble();
        this.targetAltitude = in.readDouble();
    }

    public static final Parcelable.Creator<Altitude> CREATOR = new Parcelable.Creator<Altitude>() {
        public Altitude createFromParcel(Parcel source) {
            return new Altitude(source);
        }

        public Altitude[] newArray(int size) {
            return new Altitude[size];
        }
    };
}
