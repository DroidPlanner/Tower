package com.ox3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by fhuya on 10/28/14.
 */
public class State implements Parcelable {

    private boolean armed;
    private boolean isFlying;
    private String calibrationStatus;
    private VehicleMode vehicleMode;
    private Type vehicleType;
    private String failsafeWarning;

    public State(VehicleMode mode, Type type, boolean armed, boolean flying,
                 String failsafeWarning){
        this.vehicleMode = mode;
        this.vehicleType = type;
        this.armed = armed;
        this.isFlying = flying;
        this.failsafeWarning = failsafeWarning;
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

    public String getFailsafeWarning() {
        return failsafeWarning;
    }

    public void setFailsafeWarning(String failsafeWarning) {
        this.failsafeWarning = failsafeWarning;
    }

    public boolean isWarning(){
        return TextUtils.isEmpty(failsafeWarning);
    }

    public boolean isCalibrating(){
        return calibrationStatus != null;
    }

    public void setCalibration(String message){
        this.calibrationStatus = message;
    }

    public String getCalibrationStatus(){
        return this.calibrationStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(armed ? (byte) 1 : (byte) 0);
        dest.writeByte(isFlying ? (byte) 1 : (byte) 0);
        dest.writeString(this.calibrationStatus);
        dest.writeParcelable(this.vehicleMode, 0);
        dest.writeParcelable(this.vehicleType, 0);
        dest.writeString(this.failsafeWarning);
    }

    private State(Parcel in) {
        this.armed = in.readByte() != 0;
        this.isFlying = in.readByte() != 0;
        this.calibrationStatus = in.readString();
        this.vehicleMode = in.readParcelable(VehicleMode.class.getClassLoader());
        this.vehicleType = in.readParcelable(Type.class.getClassLoader());
        this.failsafeWarning = in.readString();
    }

    public static final Creator<State> CREATOR = new Creator<State>() {
        public State createFromParcel(Parcel source) {
            return new State(source);
        }

        public State[] newArray(int size) {
            return new State[size];
        }
    };
}
