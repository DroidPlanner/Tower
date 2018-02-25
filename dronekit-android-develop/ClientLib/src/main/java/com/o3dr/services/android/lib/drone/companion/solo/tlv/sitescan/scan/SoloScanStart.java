package com.o3dr.services.android.lib.drone.companion.solo.tlv.sitescan.scan;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Sent by the app to ShotManager to notify that the scan shot is ready to commence
 */
public class SoloScanStart extends TLVPacket {

    public SoloScanStart(){
        super(TLVMessageTypes.TYPE_SOLO_SCAN_START, 0);
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {}

    protected SoloScanStart(Parcel in) {
        super(in);
    }

    public static final Creator<SoloScanStart> CREATOR = new Creator<SoloScanStart>() {
        public SoloScanStart createFromParcel(Parcel source) {
            return new SoloScanStart(source);
        }

        public SoloScanStart[] newArray(int size) {
            return new SoloScanStart[size];
        }
    };
}
