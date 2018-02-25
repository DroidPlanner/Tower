package com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

import java.nio.ByteBuffer;

/**
 * Bidirectional
 * <p/>
 * Sent by the app to tell Shotmanager to enter Play mode;  sent by Shotmanager to the app after entering Play mode in response to an Artoo button press.
 * <p/>
 * In both cases, Shotmanager follows this message with a sequence of SOLO_SPLINE_POINT messages to transmit the current Path to the app.
 * <p/>
 * Shotmanager will only enter Play mode if a valid Path exists in Record mode;  otherwise, the behavior is undefined.  This implies that the app must only send this message when it knows a valid Path exists.
 * <p/>
 * <p/>
 * Created by Fredia Huya-Kouadio on 12/8/15.
 *
 * @since 2.8.0
 */
public class SoloSplinePlay extends TLVPacket {

    public static final int MESSAGE_LENGTH = 0;

    public SoloSplinePlay(){
        super(TLVMessageTypes.TYPE_SOLO_SPLINE_PLAY, MESSAGE_LENGTH);
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
    }

    protected SoloSplinePlay(Parcel in) {
        super(in);
    }

    public static final Creator<SoloSplinePlay> CREATOR = new Creator<SoloSplinePlay>() {
        public SoloSplinePlay createFromParcel(Parcel source) {
            return new SoloSplinePlay(source);
        }

        public SoloSplinePlay[] newArray(int size) {
            return new SoloSplinePlay[size];
        }
    };
}
