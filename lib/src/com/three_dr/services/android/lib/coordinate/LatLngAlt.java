package com.three_dr.services.android.lib.coordinate;

import android.os.Parcel;

/**
 * Stores latitude, longitude, and altitude information for a coordinate.
 */
public class LatLngAlt extends LatLng {

    /**
     * Stores the altitude in meters.
     */
    private float mAltitude;

    public LatLngAlt(float latitude, float longitude, float altitude) {
        super(latitude, longitude);
        mAltitude = altitude;
    }

    public LatLngAlt(LatLngAlt copy) {
        this(copy.getLatitude(), copy.getLongitude(), copy.getAltitude());
    }

    /**
     * @return the altitude in meters
     */
    public float getAltitude() {
        return mAltitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LatLngAlt)) return false;
        if (!super.equals(o)) return false;

        LatLngAlt latLngAlt = (LatLngAlt) o;

        return Float.compare(latLngAlt.mAltitude, mAltitude) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mAltitude != +0.0f ? Float.floatToIntBits(mAltitude) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LatLngAlt{" +
                "mAltitude=" + mAltitude +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(mAltitude);
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mAltitude = in.readFloat();
    }

    protected LatLngAlt(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<LatLngAlt> CREATOR = new Creator<LatLngAlt>() {

        @Override
        public LatLngAlt createFromParcel(Parcel source) {
            return new LatLngAlt(source);
        }

        @Override
        public LatLngAlt[] newArray(int size) {
            return new LatLngAlt[size];
        }
    };
}
