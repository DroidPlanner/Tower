package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

/**
 * Sent from app to Solo to request Button mapping setting.  Sent from Solo to app as a response.
 */
public class SoloButtonSettingGetter extends SoloButtonSetting {
    public SoloButtonSettingGetter(int button, int event, int shotType, int flightModeIndex) {
        super(TLVMessageTypes.TYPE_SOLO_GET_BUTTON_SETTING, button, event, shotType, flightModeIndex);
    }

    public SoloButtonSettingGetter(int button, int event){
        this(button, event, -1, -1);
    }

    protected SoloButtonSettingGetter(Parcel in) {
        super(in);
    }

    public static final Creator<SoloButtonSettingGetter> CREATOR = new Creator<SoloButtonSettingGetter>() {
        public SoloButtonSettingGetter createFromParcel(Parcel source) {
            return new SoloButtonSettingGetter(source);
        }

        public SoloButtonSettingGetter[] newArray(int size) {
            return new SoloButtonSettingGetter[size];
        }
    };
}
