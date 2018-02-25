package com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc;

import android.os.Parcel;
import android.support.annotation.IntDef;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * App to Shotmanager.  Optional. Valid only in playback mode.
 *
 * The app sends this message to configure various aspects of how Shotmanager interprets the current Path.
 * This message is optional -- if the app never sends it, Shotmanager will use the assumed default values listed below.
 * Values stay in effect until the Path is reset by a SOLO_SPLINE_RECORD message
 *
 * Created by Fredia Huya-Kouadio on 12/8/15.
 * @since 2.8.0
 */
public class SoloSplinePathSettings extends TLVPacket {

    public static final int MESSAGE_LENGTH = 8;

    @IntDef({
        AUTO_POINT_CAMERA,
        FREE_LOOK
    })
    public @interface CameraControl{};
    public static final int AUTO_POINT_CAMERA = 0;
    public static final int FREE_LOOK = 1;
    /**
     * cameraControl: (DEFAULT 0)
     * 0 : Shotmanager controls camera interpolation;  automatically points camera
     * 1 : No camera interpolation - camera is controlled with Artoo only.
     */
    private int cameraControl;

    /**
     * The app-requested total cable completion time, in seconds.
     */
    private float desiredTime;

    public SoloSplinePathSettings(int cameraControl, float desiredTime){
        super(TLVMessageTypes.TYPE_SOLO_SPLINE_PATH_SETTINGS, MESSAGE_LENGTH);
        this.cameraControl = cameraControl;
        this.desiredTime = desiredTime;
    }

    public SoloSplinePathSettings(ByteBuffer buffer){
        this(buffer.getInt(), buffer.getInt());
    }

    public int getCameraControl() {
        return cameraControl;
    }

    public float getDesiredTime() {
        return desiredTime;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putInt(cameraControl);
        valueCarrier.putFloat(desiredTime);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.cameraControl);
        dest.writeFloat(this.desiredTime);
    }

    protected SoloSplinePathSettings(Parcel in) {
        super(in);
        this.cameraControl = in.readInt();
        this.desiredTime = in.readFloat();
    }

    public static final Creator<SoloSplinePathSettings> CREATOR = new Creator<SoloSplinePathSettings>() {
        public SoloSplinePathSettings createFromParcel(Parcel source) {
            return new SoloSplinePathSettings(source);
        }

        public SoloSplinePathSettings[] newArray(int size) {
            return new SoloSplinePathSettings[size];
        }
    };

    @Override
    public String toString() {
        return "SoloSplinePathSettings{" +
                "cameraControl=" + cameraControl +
                ", desiredTime=" + desiredTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoloSplinePathSettings)) return false;
        if (!super.equals(o)) return false;

        SoloSplinePathSettings that = (SoloSplinePathSettings) o;

        if (cameraControl != that.cameraControl) return false;
        return Float.compare(that.desiredTime, desiredTime) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + cameraControl;
        result = 31 * result + (desiredTime != +0.0f ? Float.floatToIntBits(desiredTime) : 0);
        return result;
    }
}
