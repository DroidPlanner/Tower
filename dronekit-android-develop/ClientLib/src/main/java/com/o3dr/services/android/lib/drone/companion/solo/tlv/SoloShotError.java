package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Created by Fredia Huya-Kouadio on 4/13/15.
 */
public class SoloShotError extends TLVPacket {

    public static final int SHOT_ERROR_BAD_EKF = 0;
    public static final int SHOT_ERROR_UNARMED = 1;

    private int errorType;

    public SoloShotError(int errorType){
        super(TLVMessageTypes.TYPE_SOLO_SHOT_OPTIONS, 4);
        this.errorType = errorType;
    }

    public int getErrorType() {
        return errorType;
    }

    public void setErrorType(int errorType) {
        this.errorType = errorType;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putInt(errorType);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.errorType);
    }

    protected SoloShotError(Parcel in) {
        super(in);
        this.errorType = in.readInt();
    }

    public static final Creator<SoloShotError> CREATOR = new Creator<SoloShotError>() {
        public SoloShotError createFromParcel(Parcel source) {
            return new SoloShotError(source);
        }

        public SoloShotError[] newArray(int size) {
            return new SoloShotError[size];
        }
    };
}
