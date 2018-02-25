package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.util.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 11/11/14.
 */
public class FootPrint implements DroneAttribute {

    private double meanGSD;
    private List<LatLong> vertex = new ArrayList<LatLong>();

    public FootPrint(){}

    public FootPrint(double meanGSD, List<LatLong> vertex) {
        this.meanGSD = meanGSD;
        this.vertex = vertex;
    }

    public void setMeanGSD(double meanGSD) {
        this.meanGSD = meanGSD;
    }

    public void setVertex(List<LatLong> vertex) {
        this.vertex = vertex;
    }

    public double getMeanGSD() {
        return meanGSD;
    }

    public List<LatLong> getVertexInGlobalFrame() {
        return vertex;
    }

    public double getLateralSize() {
        return  (MathUtils.getDistance2D(vertex.get(0), vertex.get(1))
                + MathUtils.getDistance2D(vertex.get(2), vertex.get(3))) / 2;
    }

    public double getLongitudinalSize() {
        return (MathUtils.getDistance2D(vertex.get(0), vertex.get(3))
                + MathUtils.getDistance2D(vertex.get(1), vertex.get(2))) / 2;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.meanGSD);
        dest.writeTypedList(vertex);
    }

    private FootPrint(Parcel in) {
        this.meanGSD = in.readDouble();
        in.readTypedList(vertex, LatLong.CREATOR);
    }

    public static final Creator<FootPrint> CREATOR = new Creator<FootPrint>() {
        public FootPrint createFromParcel(Parcel source) {
            return new FootPrint(source);
        }

        public FootPrint[] newArray(int size) {
            return new FootPrint[size];
        }
    };
}
