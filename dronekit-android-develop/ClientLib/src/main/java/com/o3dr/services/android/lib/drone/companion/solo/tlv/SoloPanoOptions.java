package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;
import android.support.annotation.IntDef;

import java.nio.ByteBuffer;

/**
 * Path: Bidirectional
 * Purpose: App updates shotmanager of pano settings
 * Shotmanager tells app to update UI based on pano settings changed through the controller
 * Requires: Shotmanager 2.4.0
 * Created by phu 7/2016
 * @since 2.9.1
 */
public class SoloPanoOptions extends TLVPacket {

    public static final int MESSAGE_LENGTH = 12;

    /**
     * Used to toggle whether the photo panos (cylindrical or spherical are running)
     */
    private static final int PANO_ON_VALUE = 1;
    private static final int PANO_OFF_VALUE = 0;

    private boolean isRunning;


    @IntDef({
            PANO_PREFERENCE_CYLINDRICAL,
            PANO_PREFERENCE_SPHERICAL,
            PANO_PREFERENCE_VIDEO
    })
    public @interface PanoPreference{}

    public static final int PANO_PREFERENCE_CYLINDRICAL = 0;
    public static final int PANO_PREFERENCE_SPHERICAL = 1;
    public static final int PANO_PREFERENCE_VIDEO = 2;

    /**
     * Pano sub modes:
     * 0. Cylindrrical - captures still photos for a wide rectangular pano
     * 1. Spherical - captures still photos for a "little world" pano
     * 2. Video - smoothly pan a video
     */
    @PanoPreference
    private int panoPreference;

    /**
     * Pan angle used in cylindrical pano to determine how wide the pano should be
     */
    private short panAngle;

    /**
     * Yaw speed used in video pano to determine how fast to pan
     */
    private float degreesPerSecondYawSpeed;

    /**
     * Camera FOV used in all modes to calculate the overlap of the photos
     */
    private float cameraFOV;

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @PanoPreference
    public int getPanoPreference() {
        return panoPreference;
    }

    public void setPanoPreference(@PanoPreference int panoPreference) {
        this.panoPreference = panoPreference;
    }

    public short getPanAngle() {
        return panAngle;
    }

    public void setPanAngle(short panAngle) {
        this.panAngle = panAngle;
    }

    public float getDegreesPerSecondYawSpeed() {
        return degreesPerSecondYawSpeed;
    }

    public void setDegreesPerSecondYawSpeed(float degreesPerSecondYawSpeed) {
        this.degreesPerSecondYawSpeed = degreesPerSecondYawSpeed;
    }

    public float getCameraFOV() {
        return cameraFOV;
    }

    public void setCameraFOV(float cameraFOV) {
        this.cameraFOV = cameraFOV;
    }

    public SoloPanoOptions(int panoPreference, boolean isRunning, short panAngle, float degreesPerSecondYawSpeed, float cameraFOV) {
        super(TLVMessageTypes.TYPE_SOLO_PANO_OPTIONS, MESSAGE_LENGTH);
        this.panoPreference = panoPreference;
        this.isRunning = isRunning;
        this.panAngle = panAngle;
        this.degreesPerSecondYawSpeed = degreesPerSecondYawSpeed;
        this.cameraFOV = cameraFOV;
    }

    SoloPanoOptions(ByteBuffer buffer) {
        this(buffer.get(), buffer.get() ==  PANO_ON_VALUE, (short) buffer.getShort(), buffer.getFloat(), buffer.getFloat());
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.put((byte) panoPreference);
        valueCarrier.put((byte) (isRunning ? 1 : 0));
        valueCarrier.putShort(panAngle);
        valueCarrier.putFloat(degreesPerSecondYawSpeed);
        valueCarrier.putFloat(cameraFOV);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) panoPreference);
        dest.writeByte(isRunning ? (byte) 1 : (byte) 0);
        dest.writeInt(panAngle);
        dest.writeFloat(degreesPerSecondYawSpeed);
        dest.writeFloat(cameraFOV);
    }

    protected SoloPanoOptions(Parcel in) {
        super(in);
        @PanoPreference int panoPreference = (int)in.readByte();
        this.panoPreference = panoPreference;
        this.isRunning = in.readByte() != 0;
        this.panAngle = (short) in.readInt();
        this.degreesPerSecondYawSpeed = in.readFloat();
        this.cameraFOV = in.readFloat();
    }

    public static final Creator<SoloPanoOptions> CREATOR = new Creator<SoloPanoOptions>() {
        public SoloPanoOptions createFromParcel(Parcel source) {
            return new SoloPanoOptions(source);
        }

        public SoloPanoOptions[] newArray(int size) {
            return new SoloPanoOptions[size];
        }
    };

    @Override
    public String toString() {
        return "SoloPanoOptions{" +
                "panoPreference=" + panoPreference +
                "isRunning=" + isRunning +
                "panAngle=" + panAngle +
                "degreesPerSecondYawSpeed=" + degreesPerSecondYawSpeed +
                "cameraFOV=" + cameraFOV +
                '}';
    }
}
