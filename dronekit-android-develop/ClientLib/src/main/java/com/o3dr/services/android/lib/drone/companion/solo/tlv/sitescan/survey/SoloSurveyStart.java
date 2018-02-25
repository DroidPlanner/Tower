package com.o3dr.services.android.lib.drone.companion.solo.tlv.sitescan.survey;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Sent by the app to ShotManager to notify that the survey shot is ready to commence.
 */
public class SoloSurveyStart extends TLVPacket {

    public SoloSurveyStart(){
        super(TLVMessageTypes.TYPE_SOLO_SURVEY_START, 0);
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {}

    protected SoloSurveyStart(Parcel in) {
        super(in);
    }

    public static final Creator<SoloSurveyStart> CREATOR = new Creator<SoloSurveyStart>() {
        public SoloSurveyStart createFromParcel(Parcel source) {
            return new SoloSurveyStart(source);
        }

        public SoloSurveyStart[] newArray(int size) {
            return new SoloSurveyStart[size];
        }
    };
}
