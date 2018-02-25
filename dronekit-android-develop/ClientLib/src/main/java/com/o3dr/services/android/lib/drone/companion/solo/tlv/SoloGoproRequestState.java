package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Created by Fredia Huya-Kouadio on 7/28/15.
 */
public class SoloGoproRequestState extends TLVPacket {

    public static final int MESSAGE_LENGTH = 0;

    public SoloGoproRequestState(){
        super(TLVMessageTypes.TYPE_SOLO_GOPRO_REQUEST_STATE, MESSAGE_LENGTH);
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {}

    protected SoloGoproRequestState(Parcel in) {
        super(in);
    }

    public static final Creator<SoloGoproRequestState> CREATOR = new Creator<SoloGoproRequestState>() {
        public SoloGoproRequestState createFromParcel(Parcel source) {
            return new SoloGoproRequestState(source);
        }

        public SoloGoproRequestState[] newArray(int size) {
            return new SoloGoproRequestState[size];
        }
    };
}
