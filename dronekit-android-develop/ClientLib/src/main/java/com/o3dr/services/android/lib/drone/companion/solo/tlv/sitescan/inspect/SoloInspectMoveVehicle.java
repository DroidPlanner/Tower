package com.o3dr.services.android.lib.drone.companion.solo.tlv.sitescan.inspect;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Sent by the app to ShotManager to instruct Solo to move with a certain velocity (body-relative NED frame).
 */
public class SoloInspectMoveVehicle extends TLVPacket {
    /**
     * Desired velocity in body-x (NED)
     */
    private float vx;
    /**
     * Desired velocity in body-y (NED)
     */
    private float vy;
    /**
     * Desired velocity in body-z (NED)
     */
    private float vz;

    public SoloInspectMoveVehicle(float vx, float vy, float vz) {
        super(TLVMessageTypes.TYPE_SOLO_INSPECT_MOVE_VEHICLE, 12);
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }

    public SoloInspectMoveVehicle(ByteBuffer buffer){
        this(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
    }

    public float getVx() {
        return vx;
    }

    public float getVy() {
        return vy;
    }

    public float getVz() {
        return vz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoloInspectMoveVehicle)) return false;
        if (!super.equals(o)) return false;

        SoloInspectMoveVehicle that = (SoloInspectMoveVehicle) o;

        if (Float.compare(that.vx, vx) != 0) return false;
        if (Float.compare(that.vy, vy) != 0) return false;
        return Float.compare(that.vz, vz) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (vx != +0.0f ? Float.floatToIntBits(vx) : 0);
        result = 31 * result + (vy != +0.0f ? Float.floatToIntBits(vy) : 0);
        result = 31 * result + (vz != +0.0f ? Float.floatToIntBits(vz) : 0);
        return result;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putFloat(vx);
        valueCarrier.putFloat(vy);
        valueCarrier.putFloat(vz);
    }

    protected SoloInspectMoveVehicle(Parcel in) {
        super(in);
        vx = in.readFloat();
        vy = in.readFloat();
        vz = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(vx);
        dest.writeFloat(vy);
        dest.writeFloat(vz);
    }


    public static final Creator<SoloInspectMoveVehicle> CREATOR = new Creator<SoloInspectMoveVehicle>() {
        public SoloInspectMoveVehicle createFromParcel(Parcel source) {
            return new SoloInspectMoveVehicle(source);
        }

        public SoloInspectMoveVehicle[] newArray(int size) {
            return new SoloInspectMoveVehicle[size];
        }
    };
}
