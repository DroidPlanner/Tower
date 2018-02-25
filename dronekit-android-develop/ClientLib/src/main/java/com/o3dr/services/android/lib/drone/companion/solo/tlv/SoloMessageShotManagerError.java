package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Debugging tool - shotmanager sends this to the app when it has hit an exception.
 */
public class SoloMessageShotManagerError extends TLVPacket {

    private final String exceptionInfo;

    public SoloMessageShotManagerError(String exceptionInfo) {
        super(TLVMessageTypes.TYPE_SOLO_MESSAGE_SHOT_MANAGER_ERROR, exceptionInfo.length());
        this.exceptionInfo = exceptionInfo;
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.put(exceptionInfo.getBytes());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.exceptionInfo);
    }

    protected SoloMessageShotManagerError(Parcel in) {
        super(in);
        this.exceptionInfo = in.readString();
    }

    public static final Creator<SoloMessageShotManagerError> CREATOR = new Creator<SoloMessageShotManagerError>() {
        public SoloMessageShotManagerError createFromParcel(Parcel source) {
            return new SoloMessageShotManagerError(source);
        }

        public SoloMessageShotManagerError[] newArray(int size) {
            return new SoloMessageShotManagerError[size];
        }
    };

    @Override
    public String toString() {
        return "SoloMessageShotManagerError{" +
                "exceptionInfo='" + exceptionInfo + '\'' +
                '}';
    }
}
