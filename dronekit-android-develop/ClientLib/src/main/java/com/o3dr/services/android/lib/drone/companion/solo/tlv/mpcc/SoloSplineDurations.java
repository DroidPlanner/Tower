package com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Used by ShotManager to transmit time information about the currently defined Path. It is sent at least once after ShotManager enters play mode to define the time bounds for the Path.
 * <p/>
 * Optionally, ShotManager can also resend the information any time previous values become invalid. For example, high winds or a low battery might mean that the previously reported maxUVelocity isn’t realistic; ShotManager would re-send the message to advise the app to change its estimates.
 * <p/>
 * <p/>
 * Created by Fredia Huya-Kouadio on 12/8/15.
 *
 * @since 2.8.0
 */
public class SoloSplineDurations extends TLVPacket {

    public static final int MESSAGE_LENGTH = 8;

    /**
     * The estimated time (in seconds) it will take to fly the entire path at maximum speed.
     */
    private float minTime;

    /**
     * The estimated time (in seconds) it will take to fly the entire path at minimum speed.
     */
    private float maxTime;

    public SoloSplineDurations(float minTime, float maxTime) {
        super(TLVMessageTypes.TYPE_SOLO_SPLINE_DURATIONS, MESSAGE_LENGTH);
        this.maxTime = maxTime;
        this.minTime = minTime;
    }

    public SoloSplineDurations(ByteBuffer buffer) {
        this(buffer.getFloat(), buffer.getFloat());
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putFloat(minTime);
        valueCarrier.putFloat(maxTime);
    }

    public float getMaxTime() {
        return maxTime;
    }

    public float getMinTime() {
        return minTime;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.minTime);
        dest.writeFloat(this.maxTime);
    }

    protected SoloSplineDurations(Parcel in) {
        super(in);
        this.minTime = in.readFloat();
        this.maxTime = in.readFloat();
    }

    public static final Creator<SoloSplineDurations> CREATOR = new Creator<SoloSplineDurations>() {
        public SoloSplineDurations createFromParcel(Parcel source) {
            return new SoloSplineDurations(source);
        }

        public SoloSplineDurations[] newArray(int size) {
            return new SoloSplineDurations[size];
        }
    };

    @Override
    public String toString() {
        return "SoloSplineDurations{" +
                "minTime=" + minTime +
                ", maxTime=" + maxTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        SoloSplineDurations that = (SoloSplineDurations) o;

        if (Float.compare(that.minTime, minTime) != 0) {
            return false;
        }
        return Float.compare(that.maxTime, maxTime) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (minTime != +0.0f ? Float.floatToIntBits(minTime) : 0);
        result = 31 * result + (maxTime != +0.0f ? Float.floatToIntBits(maxTime) : 0);
        return result;
    }
}
