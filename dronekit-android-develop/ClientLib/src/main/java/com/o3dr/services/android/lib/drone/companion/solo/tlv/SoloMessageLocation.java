package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.nio.ByteBuffer;

/**
 * Sent from app to solo to communicate a location.
 */
public class SoloMessageLocation extends TLVPacket {

    private LatLongAlt coordinate;

    public SoloMessageLocation(double latitude, double longitude, float altitudeInMeters) {
        super(TLVMessageTypes.TYPE_SOLO_MESSAGE_LOCATION, 20);
        this.coordinate = new LatLongAlt(latitude, longitude, altitudeInMeters);
    }

    public SoloMessageLocation(LatLongAlt coordinate){
        super(TLVMessageTypes.TYPE_SOLO_MESSAGE_LOCATION, 20);
        this.coordinate = coordinate;
    }

    public LatLongAlt getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLongAlt coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putDouble(coordinate.getLatitude());
        valueCarrier.putDouble(coordinate.getLongitude());
        valueCarrier.putFloat((float) coordinate.getAltitude());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.coordinate, 0);
    }

    protected SoloMessageLocation(Parcel in) {
        super(in);
        this.coordinate = in.readParcelable(LatLongAlt.class.getClassLoader());
    }

    public static final Creator<SoloMessageLocation> CREATOR = new Creator<SoloMessageLocation>() {
        public SoloMessageLocation createFromParcel(Parcel source) {
            return new SoloMessageLocation(source);
        }

        public SoloMessageLocation[] newArray(int size) {
            return new SoloMessageLocation[size];
        }
    };
}
