package com.ox3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores information about the drone's type.
 */
public class Type implements Parcelable {

    public static final int TYPE_PLANE = 1;
    public static final int TYPE_COPTER = 2;
    public static final int TYPE_ROVER = 10;

    private final int droneType;

    public Type(int droneType){
        if(droneType != TYPE_PLANE && droneType != TYPE_COPTER && droneType != TYPE_ROVER){
            throw new IllegalArgumentException("Unexpected drone type parameter (" + droneType +
                    ")");
        }

        this.droneType = droneType;
    }

    public int getDroneType(){
        return droneType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.droneType);
    }

    private Type(Parcel in) {
        this.droneType = in.readInt();
    }

    public static final Parcelable.Creator<Type> CREATOR = new Parcelable.Creator<Type>() {
        public Type createFromParcel(Parcel source) {
            return new Type(source);
        }

        public Type[] newArray(int size) {
            return new Type[size];
        }
    };
}
