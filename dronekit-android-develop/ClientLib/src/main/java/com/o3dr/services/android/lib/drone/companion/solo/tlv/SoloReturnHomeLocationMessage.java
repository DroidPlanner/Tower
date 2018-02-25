package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.nio.ByteBuffer;

/**
 * Path: App to shotmanager
 * Purpose: Sends an update of the user location as the home location for Return to Me
 * Requires: Shotmanager 2.4.0
 * Created by phu 7/2016
 * @since 2.9.1
 */
public class SoloReturnHomeLocationMessage extends TLVPacket {

    private LatLongAlt coordinate;

    public SoloReturnHomeLocationMessage(double latitude, double longitude, float altitudeInMeters) {
        super(TLVMessageTypes.TYPE_RTL_HOME_POINT, 20);
        this.coordinate = new LatLongAlt(latitude, longitude, altitudeInMeters);
    }

    public SoloReturnHomeLocationMessage(LatLongAlt coordinate){
        super(TLVMessageTypes.TYPE_RTL_HOME_POINT, 20);
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
        dest.writeParcelable(this.coordinate, flags);
    }

    protected SoloReturnHomeLocationMessage(Parcel in) {
        super(in);
        this.coordinate = in.readParcelable(LatLongAlt.class.getClassLoader());
    }

    public static final Creator<SoloReturnHomeLocationMessage> CREATOR = new Creator<SoloReturnHomeLocationMessage>() {
        public SoloReturnHomeLocationMessage createFromParcel(Parcel source) {
            return new SoloReturnHomeLocationMessage(source);
        }

        public SoloReturnHomeLocationMessage[] newArray(int size) {
            return new SoloReturnHomeLocationMessage[size];
        }
    };
}
