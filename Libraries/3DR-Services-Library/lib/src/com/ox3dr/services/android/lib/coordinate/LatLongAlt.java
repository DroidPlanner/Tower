package com.ox3dr.services.android.lib.coordinate;

import android.os.Parcel;

/**
 * Stores latitude, longitude, and altitude information for a coordinate.
 */
public class LatLongAlt extends LatLong {

    /**
     * Stores the altitude in meters.
     */
    private float mAltitude;

    public LatLongAlt(float latitude, float longitude, float altitude) {
        super(latitude, longitude);
        mAltitude = altitude;
    }

    public LatLongAlt(LatLongAlt copy) {
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
        if (!(o instanceof LatLongAlt)) return false;
        if (!super.equals(o)) return false;

        LatLongAlt latLngAlt = (LatLongAlt) o;

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
        return "LatLongAlt{" +
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

    protected LatLongAlt(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<LatLongAlt> CREATOR = new Creator<LatLongAlt>() {

        @Override
        public LatLongAlt createFromParcel(Parcel source) {
            return new LatLongAlt(source);
        }

        @Override
        public LatLongAlt[] newArray(int size) {
            return new LatLongAlt[size];
        }
    };
}
