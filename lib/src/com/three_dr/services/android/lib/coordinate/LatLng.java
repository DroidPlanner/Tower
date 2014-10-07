package com.three_dr.services.android.lib.coordinate;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores latitude and longitude in degrees.
 */
public class LatLng implements Parcelable {

    /**
     * Stores latitude, and longitude in degrees
     */
    private PointF mLatLng;

    protected LatLng(){}

    public LatLng(float latitude, float longitude){
        mLatLng = new PointF(latitude, longitude);
    }

    public LatLng(LatLng copy){
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
        if (!(o instanceof LatLng)) return false;

        LatLng latLng = (LatLng) o;

        return mLatLng.equals(latLng.mLatLng);
    }

    @Override
    public int hashCode() {
        return mLatLng.hashCode();
    }

    @Override
    public String toString() {
        return "LatLng{" +
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

    protected void readFromParcel(Parcel in){
        this.mLatLng = in.readParcelable(PointF.class.getClassLoader());
    }

    protected LatLng(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator<LatLng> CREATOR = new Parcelable.Creator<LatLng>() {
        public LatLng createFromParcel(Parcel source) {
            return new LatLng(source);
        }

        public LatLng[] newArray(int size) {
            return new LatLng[size];
        }
    };
}
