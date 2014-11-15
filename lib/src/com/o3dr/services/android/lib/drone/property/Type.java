package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores information about the drone's type.
 */
public class Type implements Parcelable {

    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_PLANE = 1;
    public static final int TYPE_COPTER = 2;
    public static final int TYPE_ROVER = 10;

    public static enum Firmware {
        ARDU_PLANE("ArduPlane"),
        ARDU_COPTER("ArduCopter"),
        APM_ROVER("APMRover");

        private final String label;

        private Firmware(String label){
            this.label = label;
        }

        public String getLabel(){
            return this.label;
        }
    }

    private int droneType;
    private String firmwareVersion;
    private Firmware firmware;

    public Type(){}

    public Type(int droneType, String firmwareVersion){
        this.droneType = droneType;
        this.firmwareVersion = firmwareVersion;

        switch(droneType){
            case TYPE_COPTER:
                firmware = Firmware.ARDU_COPTER;
                break;

            case TYPE_PLANE:
                firmware = Firmware.ARDU_PLANE;
                break;

            case TYPE_ROVER:
                firmware = Firmware.APM_ROVER;
                break;

            case TYPE_UNKNOWN:
            default:
                firmware = null;
                break;
        }
    }

    public int getDroneType(){
        return droneType;
    }

    public Firmware getFirmware(){
        return firmware;
    }

    public String getFirmwareVersion(){
        return firmwareVersion;
    }

    public void setDroneType(int droneType) {
        this.droneType = droneType;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public void setFirmware(Firmware firmware) {
        this.firmware = firmware;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.droneType);
        dest.writeString(this.firmwareVersion);
        dest.writeInt(this.firmware == null ? -1 : this.firmware.ordinal());
    }

    private Type(Parcel in) {
        this.droneType = in.readInt();
        this.firmwareVersion = in.readString();
        int tmpFirmware = in.readInt();
        this.firmware = tmpFirmware == -1 ? null : Firmware.values()[tmpFirmware];
    }

    public static final Creator<Type> CREATOR = new Creator<Type>() {
        public Type createFromParcel(Parcel source) {
            return new Type(source);
        }

        public Type[] newArray(int size) {
            return new Type[size];
        }
    };
}
