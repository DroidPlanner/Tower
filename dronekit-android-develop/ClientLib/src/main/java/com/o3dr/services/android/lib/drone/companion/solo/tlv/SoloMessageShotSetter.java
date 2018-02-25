package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

/**
 * Sent from app to solo to request that solo begin a shot.
 */
public class SoloMessageShotSetter extends SoloMessageShot {
    public SoloMessageShotSetter(int shotType) {
        super(TLVMessageTypes.TYPE_SOLO_MESSAGE_SET_CURRENT_SHOT, shotType);
    }

    protected SoloMessageShotSetter(Parcel in) {
        super(in);
    }

    public static final Creator<SoloMessageShotSetter> CREATOR = new Creator<SoloMessageShotSetter>() {
        public SoloMessageShotSetter createFromParcel(Parcel source) {
            return new SoloMessageShotSetter(source);
        }

        public SoloMessageShotSetter[] newArray(int size) {
            return new SoloMessageShotSetter[size];
        }
    };
}
