package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Sent from app to Solo or vice versa to transmit selfie options.
 */
public class SoloShotOptions extends TLVPacket {
    public static final int PAUSED_CRUISE_SPEED = 0; // meter per second
    public static final int MIN_ABS_CRUISE_SPEED = 1; // meter per second
    public static final int MAX_ABS_CRUISE_SPEED = 8; // meters per second
    public static final int DEFAULT_ABS_CRUISE_SPEED = 4; //meters per second

    /**
     * Cruise speed  (in meters/second)
     */
    private float cruiseSpeed;

    public SoloShotOptions(){
        this(TLVMessageTypes.TYPE_SOLO_SHOT_OPTIONS, 4, PAUSED_CRUISE_SPEED);
    }

    public SoloShotOptions(float cruiseSpeed) {
        this(TLVMessageTypes.TYPE_SOLO_SHOT_OPTIONS, 4, cruiseSpeed);
    }

    protected SoloShotOptions(int type, int length, float cruiseSpeed){
        super(type, length);
        this.cruiseSpeed = cruiseSpeed;
    }

    public float getCruiseSpeed() {
        return cruiseSpeed;
    }

    public void setCruiseSpeed(float cruiseSpeed) {
        this.cruiseSpeed = cruiseSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoloShotOptions)) return false;
        if (!super.equals(o)) return false;

        SoloShotOptions that = (SoloShotOptions) o;

        return Float.compare(that.cruiseSpeed, cruiseSpeed) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (cruiseSpeed != +0.0f ? Float.floatToIntBits(cruiseSpeed) : 0);
        return result;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putFloat(cruiseSpeed);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.cruiseSpeed);
    }

    protected SoloShotOptions(Parcel in) {
        super(in);
        this.cruiseSpeed = in.readFloat();
    }

    public static final Creator<SoloShotOptions> CREATOR = new Creator<SoloShotOptions>(){

        @Override
        public SoloShotOptions createFromParcel(Parcel source) {
            return new SoloShotOptions(source);
        }

        @Override
        public SoloShotOptions[] newArray(int size) {
            return new SoloShotOptions[size];
        }
    };

}
