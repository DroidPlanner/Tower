package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Path: Bidirectional
 * Purpose: App updates shotmanager on changes in zipline settings
 * App can also controls automatic cruise movements by setting the cruise speed
 * to positive or negative or zero
 * Shotmanager tells app to update UI based on zipline settings changed through the controller
 * Requires: Shotmanager 2.4.0
 * Created by phu 7/2016
 * @since 2.9.1
 */
public class SoloPanoStatus extends TLVPacket {

    public static final int MESSAGE_LENGTH = 2;

    private byte currentStep;
    private byte totalSteps;

    public SoloPanoStatus(byte currentStep, byte totalSteps) {
        super(TLVMessageTypes.TYPE_SOLO_PANO_STATUS, MESSAGE_LENGTH);
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
    }

    SoloPanoStatus(ByteBuffer buffer) {
        this(buffer.get(), buffer.get());
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.put(currentStep);
        valueCarrier.put(totalSteps);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(currentStep);
        dest.writeByte(totalSteps);
    }

    protected SoloPanoStatus(Parcel in) {
        super(in);
        this.currentStep = in.readByte();
        this.totalSteps = in.readByte();
    }

    public static final Creator<SoloPanoStatus> CREATOR = new Creator<SoloPanoStatus>() {
        public SoloPanoStatus createFromParcel(Parcel source) {
            return new SoloPanoStatus(source);
        }
        public SoloPanoStatus[] newArray(int size) {
            return new SoloPanoStatus[size];
        }
    };

    @Override
    public String toString() {
        return "SoloPanoStatus{" +
                "currentStep=" + currentStep +
                "totalSteps=" + totalSteps +
                '}';
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

}
