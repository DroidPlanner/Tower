package com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc;

import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 *
 * Bidirectional.
 *
 * When Shotmanager enters playback mode, the vehicle may or may not be positioned on the path --
 * from the app’s point of view, the actual position and velocity of the vehicle with respect to
 * the Path are unknown.
 *
 * This message directs Shotmanager to fly to a Keypoint on the Path.
 * After the vehicle reaches this point, Shotmanager responds with a SOLO_SPLINE_ATTACH to
 * indicate that it has reached the Path and is prepared to receive SOLO_SPLINE_SEEK messages.
 *
 *
 * This message is only valid once after a Path is loaded.  There is no corresponding "detach"
 * message -- the vehicle stays attached until playback mode is exited.
 *
 * @since 2.8.0
 */
public class SoloSplineAttach extends TLVPacket {
    public static final int MESSAGE_LENGTH = 4;

    /**
     * The index of the Keypoint on the currently defined Path to which Shotmanager will attach
     * (or did attach, for Shotmanager to App packets).
     */
    private final int keypointIndex;

    public SoloSplineAttach(int keypointIndex) {
        super(TLVMessageTypes.TYPE_SOLO_SPLINE_ATTACH, MESSAGE_LENGTH);
        this.keypointIndex = keypointIndex;
    }

    protected SoloSplineAttach(Parcel in) {
        super(in);
        this.keypointIndex = in.readInt();
    }

    public SoloSplineAttach(ByteBuffer buffer){
        this(buffer.getInt());
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putInt(keypointIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoloSplineAttach)) return false;
        if (!super.equals(o)) return false;

        SoloSplineAttach that = (SoloSplineAttach) o;

        return that.keypointIndex == keypointIndex;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (keypointIndex != +0.0f ? Float.floatToIntBits(keypointIndex) : 0);
        return result;
    }

    public int getKeypointIndex() {
        return keypointIndex;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.keypointIndex);
    }

    public static final Parcelable.Creator<SoloSplineAttach> CREATOR = new Parcelable.Creator<SoloSplineAttach>() {

        @Override
        public SoloSplineAttach createFromParcel(Parcel source) {
            return new SoloSplineAttach(source);
        }

        @Override
        public SoloSplineAttach[] newArray(int size) {
            return new SoloSplineAttach[size];
        }
    };
}
