package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Sent from app to Solo to request recording of a position.
 */
public class SoloMessageRecordPosition extends TLVPacket {
    public SoloMessageRecordPosition() {
        super(TLVMessageTypes.TYPE_SOLO_MESSAGE_RECORD_POSITION, 0);
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {}

    protected SoloMessageRecordPosition(Parcel in) {
        super(in);
    }

    public static final Creator<SoloMessageRecordPosition> CREATOR = new Creator<SoloMessageRecordPosition>() {
        public SoloMessageRecordPosition createFromParcel(Parcel source) {
            return new SoloMessageRecordPosition(source);
        }

        public SoloMessageRecordPosition[] newArray(int size) {
            return new SoloMessageRecordPosition[size];
        }
    };
}
