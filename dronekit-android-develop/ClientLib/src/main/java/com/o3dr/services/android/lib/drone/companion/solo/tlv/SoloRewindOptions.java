package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;
import android.support.annotation.IntDef;

import java.nio.ByteBuffer;

/**
 * Path: Bidirectional
 * Purpose: Configures the options for failsafe behavior of shotmanager
 * This can be sent at anytime to shotmanager, even outside of a shot
 * Requires: shotmanager 2.4.0
 * Created by phu 7/2016
 * @since 2.9.1
 */
public class SoloRewindOptions extends TLVPacket {

    public static final int MESSAGE_LENGTH = 6;

    public static final int RETURN_AND_HOVER = 1;
    public static final int RETURN_AND_LAND = 0;
    @IntDef({
            RETURN_AND_HOVER,
            RETURN_AND_LAND,
    })
    public @interface ReturnPreference{}

    private boolean isRewindEnabled;

    /**
     * Tells shotmanager what to do when the copter reaches the home point
     * Options: Land or Hover
     */
    @ReturnPreference
    private int returnPreference;

    /**
     * Tells shotmanager how far to backtrack along the path travelled before
     * engaging in the normal up and over RTL
     */
    private float rewindDistance;

    public float getRewindDistance() {
        return rewindDistance;
    }

    public void setRewindDistance(float rewindDistance) {
        this.rewindDistance = rewindDistance;
    }

    @ReturnPreference
    public int getReturnPreference() {
        return returnPreference;
    }

    public void setReturnPreference(@ReturnPreference int returnPreference) {
        this.returnPreference = returnPreference;
    }

    public boolean isRewindEnabled() {
        return isRewindEnabled;
    }

    public void setRewindEnabled(boolean rewindEnabled) {
        isRewindEnabled = rewindEnabled;
    }

    public SoloRewindOptions(boolean isRewindEnabled, int returnPreference, float rewindDistance) {
        super(TLVMessageTypes.TYPE_SOLO_REWIND_OPTIONS, MESSAGE_LENGTH);
        this.isRewindEnabled = isRewindEnabled;
        this.returnPreference = returnPreference;
        this.rewindDistance = rewindDistance;
    }

    SoloRewindOptions(ByteBuffer byteBuffer) {
        this(byteBuffer.get() == 1, (int) byteBuffer.get(), byteBuffer.getFloat());
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.put((byte) (isRewindEnabled ? 1 : 0));
        valueCarrier.put((byte) returnPreference);
        valueCarrier.putFloat(rewindDistance);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (isRewindEnabled ? 1 : 0));
        dest.writeByte((byte) returnPreference);
        dest.writeFloat(rewindDistance);
    }

    protected SoloRewindOptions(Parcel in) {
        super(in);
        this.isRewindEnabled = in.readByte() != 0;
        @ReturnPreference int pref = (int) in.readByte();
        this.returnPreference = pref;
        this.rewindDistance = in.readFloat();
    }

    public static final Creator<SoloRewindOptions> CREATOR = new Creator<SoloRewindOptions>() {
        public SoloRewindOptions createFromParcel(Parcel source) {
            return new SoloRewindOptions(source);
        }

        public SoloRewindOptions[] newArray(int size) {
            return new SoloRewindOptions[size];
        }
    };

    @Override
    public String toString() {
        return "SoloRewindOptions{" +
                "isRewindEnabled=" + isRewindEnabled +
                "returnPreference=" + returnPreference +
                "rewindDistance=" + rewindDistance +
                '}';
    }
}
