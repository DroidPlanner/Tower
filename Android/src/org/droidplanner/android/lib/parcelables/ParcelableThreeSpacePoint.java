package org.droidplanner.android.lib.parcelables;

import android.os.Parcel;
import android.os.Parcelable;
import ellipsoidFit.ThreeSpacePoint;

/**
 * Parcelable wrapper for the {@link ThreeSpacePoint} class.
 */
public class ParcelableThreeSpacePoint extends ThreeSpacePoint implements Parcelable {

    /**
     * Instantiate a new object.
     *
     * @param x the point on the x-axis
     * @param y the point on the y-axis
     * @param z the point on the z-axis
     */
    public ParcelableThreeSpacePoint(double x, double y, double z) {
        super(x, y, z);
    }

    public ParcelableThreeSpacePoint(ThreeSpacePoint copy){
        super(copy.x, copy.y, copy.z);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.x);
        dest.writeDouble(this.y);
        dest.writeDouble(this.z);
    }

    private ParcelableThreeSpacePoint(Parcel in) {
        this(in.readDouble(), in.readDouble(), in.readDouble());
    }

    public static final Parcelable.Creator<ParcelableThreeSpacePoint> CREATOR = new Parcelable.Creator<ParcelableThreeSpacePoint>() {
        public ParcelableThreeSpacePoint createFromParcel(Parcel source) {
            return new ParcelableThreeSpacePoint(source);
        }

        public ParcelableThreeSpacePoint[] newArray(int size) {
            return new ParcelableThreeSpacePoint[size];
        }
    };
}
