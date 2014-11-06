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

    public void setAltitude(float altitude) {
        this.mAltitude = altitude;
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
