package com.o3dr.services.android.lib.drone.companion.solo.tlv.sitescan.inspect;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Sent by the app to ShotManager to instruct Solo to actuate gimbal to desired orientation.
 */
public class SoloInspectMoveGimbal extends TLVPacket {
    /**
     * Body-relative pitch in degrees (0 to -90)
     */
    private float pitch;
    /**
     * Body-relative roll in degrees
     */
    private float roll;
    /**
     * Earth frame Yaw (heading) in degrees (0 to 360)
     */
    private float yaw;

    public SoloInspectMoveGimbal(float pitch, float roll, float yaw) {
        super(TLVMessageTypes.TYPE_SOLO_INSPECT_MOVE_GIMBAL, 12);

        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;
    }

    public SoloInspectMoveGimbal(ByteBuffer buffer){
        this(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
    }

    protected SoloInspectMoveGimbal(Parcel in) {
        super(in);
        pitch = in.readFloat();
        roll = in.readFloat();
        yaw = in.readFloat();
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

    public float getYaw() {
        return yaw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoloInspectMoveGimbal)) return false;
        if (!super.equals(o)) return false;

        SoloInspectMoveGimbal that = (SoloInspectMoveGimbal) o;

        if (Float.compare(that.pitch, pitch) != 0) return false;
        if (Float.compare(that.roll, roll) != 0) return false;
        return Float.compare(that.yaw, yaw) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (pitch != +0.0f ? Float.floatToIntBits(pitch) : 0);
        result = 31 * result + (roll != +0.0f ? Float.floatToIntBits(roll) : 0);
        result = 31 * result + (yaw != +0.0f ? Float.floatToIntBits(yaw) : 0);
        return result;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putFloat(pitch);
        valueCarrier.putFloat(roll);
        valueCarrier.putFloat(yaw);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(pitch);
        dest.writeFloat(roll);
        dest.writeFloat(yaw);
    }

    public static final Creator<SoloInspectMoveGimbal> CREATOR = new Creator<SoloInspectMoveGimbal>() {
        public SoloInspectMoveGimbal createFromParcel(Parcel source) {
            return new SoloInspectMoveGimbal(source);
        }

        public SoloInspectMoveGimbal[] newArray(int size) {
            return new SoloInspectMoveGimbal[size];
        }
    };
}
