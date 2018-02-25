package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

/**
 * Sent from solo to app when it enters a shot.
 */
public class SoloMessageShotGetter extends SoloMessageShot {
    public SoloMessageShotGetter(int shotType) {
        super(TLVMessageTypes.TYPE_SOLO_MESSAGE_GET_CURRENT_SHOT, shotType);
    }

    protected SoloMessageShotGetter(Parcel in) {
        super(in);
    }

    public static final Creator<SoloMessageShotGetter> CREATOR = new Creator<SoloMessageShotGetter>() {
        public SoloMessageShotGetter createFromParcel(Parcel source) {
            return new SoloMessageShotGetter(source);
        }

        public SoloMessageShotGetter[] newArray(int size) {
            return new SoloMessageShotGetter[size];
        }
    };
}
