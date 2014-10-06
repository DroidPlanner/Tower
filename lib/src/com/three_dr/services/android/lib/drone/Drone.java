package com.three_dr.services.android.lib.drone;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Base class for a drone instance.
 */
public abstract class Drone implements Parcelable, Serializable {
    @Override
    public final int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    public static final Creator<Drone> CREATOR = new Creator<Drone>(){

        @Override
        public final Drone createFromParcel(Parcel source) {
            return (Drone) source.readSerializable();
        }

        @Override
        public final Drone[] newArray(int size) {
            return new Drone[size];
        }
    };
}
