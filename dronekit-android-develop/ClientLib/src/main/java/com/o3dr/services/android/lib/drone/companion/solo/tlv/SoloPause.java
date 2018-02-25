package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Used by ShotManager or app to transmit a pause request.
 */
public class SoloPause extends TLVPacket {

    public SoloPause() {
        super(TLVMessageTypes.TYPE_SOLO_PAUSE_BUTTON, 0);
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected SoloPause(Parcel in) {
        super(in);
    }

    public static final Creator<SoloPause> CREATOR = new Creator<SoloPause>() {
        public SoloPause createFromParcel(Parcel source) {
            return new SoloPause(source);
        }

        public SoloPause[] newArray(int size) {
            return new SoloPause[size];
        }
    };

    @Override
    public String toString() {
        return "SoloPause{}";
    }
}
