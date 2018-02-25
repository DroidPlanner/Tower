package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Sent from app to Solo or vice versa to transmit cable cam options.
 */
public class SoloCableCamOptions extends SoloShotOptions {

    private static final int CAM_INTERPOLATION_ENABLED_VALUE = 1;
    private static final int CAM_INTERPOLATION_DISABLED_VALUE = 0;

    private static final int YAW_DIRECTION_CW_VALUE = 0;
    private static final int YAW_DIRECTION_CCW_VALUE = 1;
    /**
     * 0 if interpolation is off
     * 1 if on
     */
    private boolean camInterpolation;

    /**
     * 1 means counter clock wise
     * 0 means clockwise
     * Received from shot manager, and shouldn't be persisted in the app.
     */
    private boolean yawDirectionClockwise;

    public SoloCableCamOptions(boolean camInterpolation, boolean yawDirectionClockwise, float cruiseSpeed) {
        super(TLVMessageTypes.TYPE_SOLO_CABLE_CAM_OPTIONS, 8, cruiseSpeed);
        this.camInterpolation = camInterpolation;
        this.yawDirectionClockwise = yawDirectionClockwise;
    }

    SoloCableCamOptions(int camInterpolationValue, int yawDirectionValue, float cruiseSpeed) {
        this(camInterpolationValue == CAM_INTERPOLATION_ENABLED_VALUE,
                yawDirectionValue == YAW_DIRECTION_CW_VALUE,
                cruiseSpeed);
    }

    public boolean isCamInterpolationOn() {
        return camInterpolation;
    }

    public void setCamInterpolation(boolean camInterpolation) {
        this.camInterpolation = camInterpolation;
    }

    public boolean isYawDirectionClockWise() {
        return yawDirectionClockwise;
    }

    public void setYawDirection(boolean yawDirection) {
        this.yawDirectionClockwise = yawDirection;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putShort((short) (camInterpolation ? CAM_INTERPOLATION_ENABLED_VALUE : CAM_INTERPOLATION_DISABLED_VALUE));
        valueCarrier.putShort((short) (yawDirectionClockwise ? YAW_DIRECTION_CW_VALUE : YAW_DIRECTION_CCW_VALUE));
        super.getMessageValue(valueCarrier);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(camInterpolation ? (byte) 1 : (byte) 0);
        dest.writeByte(yawDirectionClockwise ? (byte) 1 : (byte) 0);
    }

    protected SoloCableCamOptions(Parcel in) {
        super(in);
        this.camInterpolation = in.readByte() != 0;
        this.yawDirectionClockwise = in.readByte() != 0;
    }

    public static final Creator<SoloCableCamOptions> CREATOR = new Creator<SoloCableCamOptions>() {
        public SoloCableCamOptions createFromParcel(Parcel source) {
            return new SoloCableCamOptions(source);
        }

        public SoloCableCamOptions[] newArray(int size) {
            return new SoloCableCamOptions[size];
        }
    };
}
