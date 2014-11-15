package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

/**
 * Location from which the drone took off.
 */
public class Home implements Parcelable {

    /**
     * Lauch pad 3D coordinate.
     */
    private LatLongAlt mCoordinate;

    public Home(){}

    public Home(double latitude, double longitude, double altitude){
        mCoordinate = new LatLongAlt(latitude, longitude, altitude);
    }

    public Home(LatLongAlt coordinate){
        mCoordinate = coordinate;
    }

    /**
     * @return the launch pad 3D coordinate.
     */
    public LatLongAlt getCoordinate(){
        return mCoordinate;
    }

    public void setCoordinate(LatLongAlt mCoordinate) {
        this.mCoordinate = mCoordinate;
    }

    public boolean isValid(){
        return mCoordinate != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Home)) return false;

        Home home = (Home) o;

        return !(mCoordinate != null ? !mCoordinate.equals(home.mCoordinate) : home.mCoordinate != null);

    }

    @Override
    public int hashCode() {
        return mCoordinate != null ? mCoordinate.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "LaunchPad{" +
                "mCoordinate=" + mCoordinate +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mCoordinate, 0);
    }

    private Home(Parcel in) {
        this.mCoordinate = in.readParcelable(LatLongAlt.class.getClassLoader());
    }

    public static final Parcelable.Creator<Home> CREATOR = new Parcelable.Creator<Home>() {
        public Home createFromParcel(Parcel source) {
            return new Home(source);
        }

        public Home[] newArray(int size) {
            return new Home[size];
        }
    };
}
