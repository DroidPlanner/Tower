package com.three_dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

import com.three_dr.services.android.lib.coordinate.LatLngAlt;

/**
 * Location from which the drone took off.
 */
public class LaunchPad implements Parcelable {

    /**
     * Lauch pad 3D coordinate.
     */
    private final LatLngAlt mCoordinate;

    public LaunchPad(float latitude, float longitude, float altitude){
        mCoordinate = new LatLngAlt(latitude, longitude, altitude);
    }

    public LaunchPad(LatLngAlt coordinate){
        mCoordinate = coordinate;
    }

    /**
     * @return the launch pad 3D coordinate.
     */
    public LatLngAlt getCoordinate(){
        return mCoordinate;
    }

    public boolean isValid(){
        return mCoordinate != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LaunchPad)) return false;

        LaunchPad launchPad = (LaunchPad) o;

        return !(mCoordinate != null ? !mCoordinate.equals(launchPad.mCoordinate) : launchPad.mCoordinate != null);

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

    private LaunchPad(Parcel in) {
        this.mCoordinate = in.readParcelable(LatLngAlt.class.getClassLoader());
    }

    public static final Parcelable.Creator<LaunchPad> CREATOR = new Parcelable.Creator<LaunchPad>() {
        public LaunchPad createFromParcel(Parcel source) {
            return new LaunchPad(source);
        }

        public LaunchPad[] newArray(int size) {
            return new LaunchPad[size];
        }
    };
}
