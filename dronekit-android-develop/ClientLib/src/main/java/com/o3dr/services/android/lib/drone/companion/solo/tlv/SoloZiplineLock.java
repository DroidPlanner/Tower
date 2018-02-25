package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Path: Bidirectional
 * Purpose: App tells shotmanager to lock onto the path of the current copter heading for zipline
 * Or shotmanager tells app that a path has been locked and transition the UI
 * Requires: Shotmanager 2.4.0
 * Created by phu 7/2016
 * @since 2.9.1
 */
public class SoloZiplineLock extends TLVPacket {
    public SoloZiplineLock() {
        super(TLVMessageTypes.TYPE_SOLO_ZIPLINE_LOCK, 0);
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
    }

    protected SoloZiplineLock(Parcel in) {
        super(in);
    }

    public static final Creator<SoloZiplineLock> CREATOR = new Creator<SoloZiplineLock>() {
        public SoloZiplineLock createFromParcel(Parcel source) {
            return new SoloZiplineLock(source);
        }

        public SoloZiplineLock[] newArray(int size) {
            return new SoloZiplineLock[size];
        }
    };

    @Override
    public String toString() {
        return "SoloZiplineLock{}";
    }
}
