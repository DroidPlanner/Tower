package com.ox3dr.services.android.lib.coordinate;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores latitude, longitude, and altitude information for a coordinate.
 */
public class LatLongAlt extends LatLong {

    /**
     * Stores the altitude in meters.
     */
    private double mAltitude;

    public LatLongAlt(double latitude, double longitude, double altitude) {
        super(latitude, longitude);
        mAltitude = altitude;
    }

    public LatLongAlt(LatLongAlt copy) {
        this(copy.getLatitude(), copy.getLongitude(), copy.getAltitude());
    }

    public void set(LatLongAlt source){
        super.set(source);
        this.mAltitude = source.mAltitude;
    }

    /**
     * @return the altitude in meters
     */
    public double getAltitude() {
        return mAltitude;
    }

    public void setAltitude(float altitude) {
        this.mAltitude = altitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LatLongAlt)) return false;
        if (!super.equals(o)) return false;

        LatLongAlt that = (LatLongAlt) o;

        if (Double.compare(that.mAltitude, mAltitude) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(mAltitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LatLongAlt{" +
                "mAltitude=" + mAltitude +
                '}';
    }

    public static final Parcelable.Creator<LatLongAlt> CREATOR = new Parcelable.Creator<LatLongAlt>
            () {
        public LatLongAlt createFromParcel(Parcel source) {
            return (LatLongAlt) source.readSerializable();
        }

        public LatLongAlt[] newArray(int size) {
            return new LatLongAlt[size];
        }
    };
}
