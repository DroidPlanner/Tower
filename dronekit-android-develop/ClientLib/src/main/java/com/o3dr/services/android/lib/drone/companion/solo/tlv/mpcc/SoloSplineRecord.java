package com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * App to Shotmanager
 * Tells Shotmanager to enter Record mode and clear the current Path.
 *
 * Created by Fredia Huya-Kouadio on 12/8/15.
 * @since 2.8.0
 */
public class SoloSplineRecord extends TLVPacket {

    public static final int MESSAGE_LENGTH = 0;

    public SoloSplineRecord() {
        super(TLVMessageTypes.TYPE_SOLO_SPLINE_RECORD, MESSAGE_LENGTH);
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {}

    protected SoloSplineRecord(Parcel in) {
        super(in);
    }

    public static final Creator<SoloSplineRecord> CREATOR = new Creator<SoloSplineRecord>() {
        public SoloSplineRecord createFromParcel(Parcel source) {
            return new SoloSplineRecord(source);
        }

        public SoloSplineRecord[] newArray(int size) {
            return new SoloSplineRecord[size];
        }
    };
}
