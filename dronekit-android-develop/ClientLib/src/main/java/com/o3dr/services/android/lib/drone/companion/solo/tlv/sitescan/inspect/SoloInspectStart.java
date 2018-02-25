package com.o3dr.services.android.lib.drone.companion.solo.tlv.sitescan.inspect;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Sent by the app to ShotManager to notify that the inspect shot is ready to
 * commence and the vehicle should climb to the provided takeoffAlt.
 * Shotmanager will either takeoff or place the vehicle into GUIDED
 * depending on whether the vehicle is already in the air or not.
 */
public class SoloInspectStart extends TLVPacket {
    /**
     * Relative altitude from takeoff (in meters) that the vehicle should navigate to.
     */
    private float alt;

    public SoloInspectStart(float alt) {
        super(TLVMessageTypes.TYPE_SOLO_INSPECT_START, 4);
        this.alt = alt;
    }

    public SoloInspectStart(ByteBuffer buffer){
        this(buffer.getFloat());
    }

    public float getAlt() {
        return alt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoloInspectStart)) return false;
        if (!super.equals(o)) return false;

        SoloInspectStart that = (SoloInspectStart) o;

        return Float.compare(that.alt, alt) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (alt != +0.0f ? Float.floatToIntBits(alt) : 0);
        return result;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putFloat(alt);
    }

    protected SoloInspectStart(Parcel in) {
        super(in);
        this.alt = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.alt);
    }

    public static final Creator<SoloInspectStart> CREATOR = new Creator<SoloInspectStart>() {
        public SoloInspectStart createFromParcel(Parcel source) {
            return new SoloInspectStart(source);
        }

        public SoloInspectStart[] newArray(int size) {
            return new SoloInspectStart[size];
        }
    };
}
