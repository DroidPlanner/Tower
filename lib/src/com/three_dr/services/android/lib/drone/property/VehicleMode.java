package com.three_dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 10/28/14.
 */
public class VehicleMode implements Parcelable {

    private final int mode;
    private final int droneType;
    private final String label;

    VehicleMode(int mode, int droneType, String label){
        this.mode = mode;
        this.droneType = droneType;
        this.label = label;
    }

    public int getMode() {
        return mode;
    }

    public int getDroneType() {
        return droneType;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mode);
        dest.writeInt(this.droneType);
        dest.writeString(this.label);
    }

    private VehicleMode(Parcel in) {
        this.mode = in.readInt();
        this.droneType = in.readInt();
        this.label = in.readString();
    }

    public static final Parcelable.Creator<VehicleMode> CREATOR = new Parcelable.Creator<VehicleMode>() {
        public VehicleMode createFromParcel(Parcel source) {
            return new VehicleMode(source);
        }

        public VehicleMode[] newArray(int size) {
            return new VehicleMode[size];
        }
    };
}
