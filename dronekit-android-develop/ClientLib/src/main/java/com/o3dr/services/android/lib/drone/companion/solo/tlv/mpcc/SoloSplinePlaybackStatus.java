package com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * ShotManager sends this message to the app to indicate the vehicle's parametric position and cruise state along the Path. The source, precision, and accuracy of these values is solely determined by ShotManager. They are for visualization in the app, but not control; they are intended to convey the information, "the vehicle was at this point, trying to move at this cruise speed when the message was sent" with as much certainty as practically possible. But there are no guarantees.
 * <p/>
 * The frequency at which ShotManager sends these messages is currently 10Hz. At a minimum, in playback mode ShotManager will send this message:
 * <p/>
 * When the vehicle attaches to the path in response to the first SOLO_SPLINE_SEEK message received after entering Play mode.
 * Each time the vehicle passes a Keypoint, including the beginning and end of the path
 * When the vehicle starts or stops moving.
 * When the vehicle’s parametric acceleration exceeds a threshold value:
 * The threshold is implementation-specific, exact value TBD (ideally, it should be "tuneable").
 *
 *
 * Created by Fredia Huya-Kouadio on 12/8/15.
 *
 * @since 2.8.0
 */
public class SoloSplinePlaybackStatus extends TLVPacket {

    public static final int MESSAGE_LENGTH = 8;

    /**
     * A parametric offset along the Path normalized to (0,1).
     */
    private float uPosition;

    /**
     * Used by the app to determine the state of the cruise play/pause buttons.
     * -1: Cruising to the start of the cable (negative cruise speed).
     * 0 : Not moving/paused (cruise speed == 0). (DEFAULT)
     * 1 : Cruising to the end of the cable (positive cruise speed).
     */
    private int cruiseState;

    public SoloSplinePlaybackStatus(float uPosition, int cruiseState){
        super(TLVMessageTypes.TYPE_SOLO_SPLINE_PLAYBACK_STATUS, MESSAGE_LENGTH);
        this.uPosition = uPosition;
        this.cruiseState = cruiseState;
    }

    public SoloSplinePlaybackStatus(ByteBuffer buffer){
        this(buffer.getFloat(), buffer.getInt());
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier){
        valueCarrier.putFloat(uPosition);
        valueCarrier.putInt(cruiseState);
    }

    public float getUPosition() {
        return uPosition;
    }

    public int getCruiseState() {
        return cruiseState;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.uPosition);
        dest.writeInt(this.cruiseState);
    }

    protected SoloSplinePlaybackStatus(Parcel in) {
        super(in);
        this.uPosition = in.readFloat();
        this.cruiseState = in.readInt();
    }

    public static final Creator<SoloSplinePlaybackStatus> CREATOR = new Creator<SoloSplinePlaybackStatus>() {
        public SoloSplinePlaybackStatus createFromParcel(Parcel source) {
            return new SoloSplinePlaybackStatus(source);
        }

        public SoloSplinePlaybackStatus[] newArray(int size) {
            return new SoloSplinePlaybackStatus[size];
        }
    };

    @Override
    public String toString() {
        return "SoloSplinePlaybackStatus{" +
            "uPosition=" + uPosition +
            ", cruiseState=" + cruiseState +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoloSplinePlaybackStatus)) return false;
        if (!super.equals(o)) return false;

        SoloSplinePlaybackStatus that = (SoloSplinePlaybackStatus) o;

        if (Float.compare(that.uPosition, uPosition) != 0) return false;
        return cruiseState == that.cruiseState;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (uPosition != +0.0f ? Float.floatToIntBits(uPosition) : 0);
        result = 31 * result + cruiseState;
        return result;
    }
}
