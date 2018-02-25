package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

/**
 * Sent from app to Solo to set a Button mapping setting.
 */
public class SoloButtonSettingSetter extends SoloButtonSetting {
    public SoloButtonSettingSetter(int button, int event, int shotType, int flightModeIndex) {
        super(TLVMessageTypes.TYPE_SOLO_SET_BUTTON_SETTING, button, event, shotType, flightModeIndex);
    }

    public SoloButtonSettingSetter(int button, int event){
        this(button, event, -1, -1);
    }

    protected SoloButtonSettingSetter(Parcel in) {
        super(in);
    }

    public static final Creator<SoloButtonSettingSetter> CREATOR = new Creator<SoloButtonSettingSetter>() {
        public SoloButtonSettingSetter createFromParcel(Parcel source) {
            return new SoloButtonSettingSetter(source);
        }

        public SoloButtonSettingSetter[] newArray(int size) {
            return new SoloButtonSettingSetter[size];
        }
    };
}
