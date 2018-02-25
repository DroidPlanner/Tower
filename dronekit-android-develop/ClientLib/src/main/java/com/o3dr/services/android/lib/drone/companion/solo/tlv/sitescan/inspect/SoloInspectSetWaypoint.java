package com.o3dr.services.android.lib.drone.companion.solo.tlv.sitescan.inspect;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Sent by the app to ShotManager to instruct Solo to navigate towards the provided waypoint.
 */
public class SoloInspectSetWaypoint extends TLVPacket {
    /**
     * Latitude in decimal degrees
     */
    private float lat;
    /**
     * Longitude in decimal degrees
     */
    private float lon;
    /**
     * Relative altitude from takeoff (in meters)
     */
    private float alt;

    public SoloInspectSetWaypoint(float lat, float lon, float alt) {
        super(TLVMessageTypes.TYPE_SOLO_INSPECT_SET_WAYPOINT, 12);
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
    }

    public SoloInspectSetWaypoint(ByteBuffer buffer){
        this(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    public float getAlt() {
        return alt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoloInspectSetWaypoint)) return false;
        if (!super.equals(o)) return false;

        SoloInspectSetWaypoint that = (SoloInspectSetWaypoint) o;

        if (Float.compare(that.lat, lat) != 0) return false;
        if (Float.compare(that.lon, lon) != 0) return false;
        return Float.compare(that.alt, alt) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (lat != +0.0f ? Float.floatToIntBits(lat) : 0);
        result = 31 * result + (lon != +0.0f ? Float.floatToIntBits(lon) : 0);
        result = 31 * result + (alt != +0.0f ? Float.floatToIntBits(alt) : 0);
        return result;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putFloat(lat);
        valueCarrier.putFloat(lon);
        valueCarrier.putFloat(alt);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(this.lat);
        dest.writeFloat(this.lon);
        dest.writeFloat(this.alt);
    }

    protected SoloInspectSetWaypoint(Parcel in) {
        super(in);
        lat = in.readFloat();
        lon = in.readFloat();
        alt = in.readFloat();
    }

    public static final Creator<SoloInspectSetWaypoint> CREATOR = new Creator<SoloInspectSetWaypoint>() {
        public SoloInspectSetWaypoint createFromParcel(Parcel source) {
            return new SoloInspectSetWaypoint(source);
        }

        public SoloInspectSetWaypoint[] newArray(int size) {
            return new SoloInspectSetWaypoint[size];
        }
    };
}
