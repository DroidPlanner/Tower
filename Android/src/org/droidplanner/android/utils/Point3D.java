package org.droidplanner.android.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 11/14/14.
 */
public class Point3D implements Parcelable {

    public double x;
    public double y;
    public double z;

    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Point3D[] fromDoubleArrays(double[] x, double[] y, double[] z){
        int argLength = x == null ? 0 : x.length;
        Point3D[] points = new Point3D[argLength];

        for(int i = 0; i < argLength; i++){
            points[i] = new Point3D(x[i], y[i], z[i]);
        }
        return points;
    }

    public static double[][] fromPoint3Ds(Point3D[] points){
        final int pointsCount = points == null ? 0 : points.length;
        double[][] result = new double[3][pointsCount];
        for(int i = 0; i < pointsCount; i++){
            Point3D point = points[i];
            result[0][i] = point.x;
            result[1][i] = point.y;
            result[2][i] = point.z;
        }
        return result;
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

    public Point3D() {
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
