package com.ox3dr.services.android.lib.coordinate;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores latitude and longitude in degrees.
 */
public class LatLong implements Parcelable {

    /**
     * Stores latitude, and longitude in degrees
     */
    private PointF mLatLng;

    public LatLong(float latitude, float longitude){
        mLatLng = new PointF(latitude, longitude);
    }

    public LatLong(LatLong copy){
        this(copy.getLatitude(), copy.getLongitude());
    }

    /**
     * @return the latitude in degrees
     */
    public float getLatitude(){
        return mLatLng.x;
    }

    /**
     * @return the longitude in degrees
     */
    public float getLongitude(){
        return mLatLng.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LatLong)) return false;

        LatLong latLng = (LatLong) o;

        return mLatLng.equals(latLng.mLatLng);
    }

    @Override
    public int hashCode() {
        return mLatLng.hashCode();
    }

    @Override
    public String toString() {
        return "LatLong{" +
                "mLatLng=" + mLatLng +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mLatLng, 0);
    }

    protected LatLong(Parcel in) {
        this.mLatLng = in.readParcelable(PointF.class.getClassLoader());
    }

    public static final Parcelable.Creator<LatLong> CREATOR = new Parcelable.Creator<LatLong>() {
        public LatLong createFromParcel(Parcel source) {
            return new LatLong(source);
        }

        public LatLong[] newArray(int size) {
            return new LatLong[size];
        }
    };
}
