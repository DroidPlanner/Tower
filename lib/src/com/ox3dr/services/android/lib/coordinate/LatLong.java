package com.ox3dr.services.android.lib.coordinate;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Stores latitude and longitude in degrees.
 */
public class LatLong implements Parcelable, Serializable {

    /**
     * Stores latitude, and longitude in degrees
     */
    private final float latitude;
    private final float longitude;

    public LatLong(float latitude, float longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLong(LatLong copy){
        this(copy.getLatitude(), copy.getLongitude());
    }

    /**
     * @return the latitude in degrees
     */
    public float getLatitude(){
        return latitude;
    }

    /**
     * @return the longitude in degrees
     */
    public float getLongitude(){
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LatLong)) return false;

        LatLong latLong = (LatLong) o;

        if (Float.compare(latLong.latitude, latitude) != 0) return false;
        if (Float.compare(latLong.longitude, longitude) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (latitude != +0.0f ? Float.floatToIntBits(latitude) : 0);
        result = 31 * result + (longitude != +0.0f ? Float.floatToIntBits(longitude) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LatLong{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    public static final Parcelable.Creator<LatLong> CREATOR = new Parcelable.Creator<LatLong>() {
        public LatLong createFromParcel(Parcel source) {
            return (LatLong) source.readSerializable();
        }

        public LatLong[] newArray(int size) {
            return new LatLong[size];
        }
    };
}
