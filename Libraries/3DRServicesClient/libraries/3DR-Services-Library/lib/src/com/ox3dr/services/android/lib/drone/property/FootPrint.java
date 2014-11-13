package com.ox3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

import com.ox3dr.services.android.lib.coordinate.LatLong;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 11/11/14.
 */
public class FootPrint implements Parcelable {

    private LatLong center;
    private List<LatLong> vertex = new ArrayList<LatLong>();

    public FootPrint(){}

    public FootPrint(LatLong center, List<LatLong> vertex) {
        this.center = center;
        this.vertex = vertex;
    }

    public void setCenter(LatLong center) {
        this.center = center;
    }

    public void setVertex(List<LatLong> vertex) {
        this.vertex = vertex;
    }

    public LatLong getCenter() {
        return center;
    }

    public List<LatLong> getVertex() {
        return vertex;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.center, 0);
        dest.writeTypedList(vertex);
    }

    private FootPrint(Parcel in) {
        this.center = in.readParcelable(LatLong.class.getClassLoader());
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
