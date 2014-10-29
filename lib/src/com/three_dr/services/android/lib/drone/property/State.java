package com.three_dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 10/28/14.
 */
public class State implements Parcelable {

    private final boolean armed;
    private final boolean isFlying;
    private final VehicleMode vehicleMode;
    private final Type vehicleType;

    public State(VehicleMode mode, Type type, boolean armed, boolean flying){
        this.vehicleMode = mode;
        this.vehicleType = type;
        this.armed = armed;
        this.isFlying = flying;
    }

    public boolean isArmed() {
        return armed;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public VehicleMode getVehicleMode() {
        return vehicleMode;
    }

    public Type getVehicleType(){
        return vehicleType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(armed ? (byte) 1 : (byte) 0);
        dest.writeByte(isFlying ? (byte) 1 : (byte) 0);
        dest.writeInt(this.vehicleMode == null ? -1 : this.vehicleMode.ordinal());
        dest.writeParcelable(this.vehicleType, flags);
    }

    private State(Parcel in) {
        this.armed = in.readByte() != 0;
        this.isFlying = in.readByte() != 0;
        int tmpVehicleMode = in.readInt();
        this.vehicleMode = tmpVehicleMode == -1 ? null : VehicleMode.values()[tmpVehicleMode];
        this.vehicleType = in.readParcelable(Type.class.getClassLoader());
    }

    public static final Parcelable.Creator<State> CREATOR = new Parcelable.Creator<State>() {
        public State createFromParcel(Parcel source) {
            return new State(source);
        }

        public State[] newArray(int size) {
            return new State[size];
        }
    };
}
