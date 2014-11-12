package com.ox3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

import com.ox3dr.services.android.lib.coordinate.LatLong;

import java.util.ArrayList;

/**
 * Created by fhuya on 11/11/14.
 */
public class FootPrint implements Parcelable {

    private final LatLong center;
    private final ArrayList<LatLong> vertex;

    public FootPrint(LatLong center, ArrayList<LatLong> vertex) {
        this.center = center;
        this.vertex = vertex;
    }

    public LatLong getCenter() {
        return center;
    }

    public ArrayList<LatLong> getVertex() {
        return vertex;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.center, 0);
        dest.writeSerializable(this.vertex);
    }

    private FootPrint(Parcel in) {
        this.center = in.readParcelable(LatLong.class.getClassLoader());
        this.vertex = (ArrayList<LatLong>) in.readSerializable();
    }

    public static final Parcelable.Creator<FootPrint> CREATOR = new Parcelable.Creator<FootPrint>() {
        public FootPrint createFromParcel(Parcel source) {
            return new FootPrint(source);
        }

        public FootPrint[] newArray(int size) {
            return new FootPrint[size];
        }
    };
}
