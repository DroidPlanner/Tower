package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;

/**
 * Stores information about the drone's type.
 */
public class Type implements DroneAttribute {

    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_PLANE = 1;
    public static final int TYPE_COPTER = 2;
    public static final int TYPE_ROVER = 10;

    public static enum Firmware {
        ARDU_PLANE("ArduPlane"),
        ARDU_COPTER("ArduCopter"),
        APM_ROVER("APMRover");

        private final String label;

        Firmware(String label){
            this.label = label;
        }

        public String getLabel(){
            return this.label;
        }
    }

    private int droneType = TYPE_UNKNOWN;
    private String firmwareVersion;
    private Firmware firmware;

    public Type(){}

    public Type(int droneType, String firmwareVersion){
        this.droneType = droneType;
        this.firmwareVersion = firmwareVersion;
        this.firmware = getTypeFirmware(droneType);
    }

    private static Firmware getTypeFirmware(int droneType) {
        switch(droneType){
            case TYPE_COPTER:
                return Firmware.ARDU_COPTER;

            case TYPE_PLANE:
                return Firmware.ARDU_PLANE;

            case TYPE_ROVER:
                return Firmware.APM_ROVER;

            case TYPE_UNKNOWN:
            default:
                return null;
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

        // We're no longer passing the firmware ordinal since it'll be inferred from the drone type.
        dest.writeInt(-1);
    }

    private Type(Parcel in) {
        this(in.readInt(), in.readString());

        // Last value is the drone firmware ordinal, which is no longer of use.
        in.readInt();
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
