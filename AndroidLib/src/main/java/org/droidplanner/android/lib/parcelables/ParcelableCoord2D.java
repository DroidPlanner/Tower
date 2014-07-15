package org.droidplanner.android.lib.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

import org.droidplanner.core.helpers.coordinates.Coord2D;

/**
 * Parcelable wrapper for a Coord2D object.
 */
public class ParcelableCoord2D implements Parcelable {

    private final Coord2D mCoord;

    @Override
    public int describeContents() { return 0; }

    public Coord2D getCoord() {
        return mCoord;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(mCoord.getLat());
        dest.writeDouble(mCoord.getLng());
    }

    public ParcelableCoord2D(Coord2D coord) {
        mCoord = coord;
    }

    private ParcelableCoord2D(Parcel in) {
        final double latitude = in.readDouble();
        final double longitude = in.readDouble();
        mCoord = new Coord2D(latitude, longitude);
    }

    public static final Parcelable.Creator<ParcelableCoord2D> CREATOR = new Parcelable
            .Creator<ParcelableCoord2D>() {
        public ParcelableCoord2D createFromParcel(Parcel source) {return new ParcelableCoord2D(source);}

        public ParcelableCoord2D[] newArray(int size) {return new ParcelableCoord2D[size];}
    };
}
