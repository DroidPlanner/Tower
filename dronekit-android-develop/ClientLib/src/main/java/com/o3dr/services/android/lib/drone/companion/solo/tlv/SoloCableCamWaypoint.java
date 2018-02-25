package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.nio.ByteBuffer;

/**
 *  Send the app our cable cam waypoint when it’s recorded.
 *  Deprecated since shotmanager 2.0
 */
public class SoloCableCamWaypoint extends TLVPacket {

    private LatLongAlt coordinate;

    /**
     * Yaw in degrees
     */
    private float degreesYaw;

    /**
     * Camera pitch in degrees
     */
    private float pitch;

    public SoloCableCamWaypoint(double latitude, double longitude, float altitude, float degreesYaw, float pitch) {
        super(TLVMessageTypes.TYPE_SOLO_CABLE_CAM_WAYPOINT, 28);
        this.coordinate = new LatLongAlt(latitude, longitude, altitude);
        this.degreesYaw = degreesYaw;
        this.pitch = pitch;
    }

    public LatLongAlt getCoordinate() {
        return coordinate;
    }

    public float getDegreesYaw() {
        return degreesYaw;
    }

    public float getPitch() {
        return pitch;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putDouble(coordinate.getLatitude());
        valueCarrier.putDouble(coordinate.getLongitude());
        valueCarrier.putFloat((float) coordinate.getAltitude());
        valueCarrier.putFloat(degreesYaw);
        valueCarrier.putFloat(pitch);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.coordinate, 0);
        dest.writeFloat(this.degreesYaw);
        dest.writeFloat(this.pitch);
    }

    protected SoloCableCamWaypoint(Parcel in) {
        super(in);
        this.coordinate = in.readParcelable(LatLongAlt.class.getClassLoader());
        this.degreesYaw = in.readFloat();
        this.pitch = in.readFloat();
    }

    public static final Creator<SoloCableCamWaypoint> CREATOR = new Creator<SoloCableCamWaypoint>() {
        public SoloCableCamWaypoint createFromParcel(Parcel source) {
            return new SoloCableCamWaypoint(source);
        }

        public SoloCableCamWaypoint[] newArray(int size) {
            return new SoloCableCamWaypoint[size];
        }
    };
}
