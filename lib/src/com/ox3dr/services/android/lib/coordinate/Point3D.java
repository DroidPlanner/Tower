package com.ox3dr.services.android.lib.coordinate;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 11/4/14.
 */
public class Point3D implements Parcelable {

    public final double x;
    public final double y;
    public final double z;

    /**
     * Instantiate a new object.
     *
     * @param x the point on the x-axis
     * @param y the point on the y-axis
     * @param z the point on the z-axis
     */
    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    private Point3D(Parcel in) {
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
    }

    public static final Parcelable.Creator<Point3D> CREATOR = new Parcelable.Creator<Point3D>() {
        public Point3D createFromParcel(Parcel source) {
            return new Point3D(source);
        }

        public Point3D[] newArray(int size) {
            return new Point3D[size];
        }
    };
}
