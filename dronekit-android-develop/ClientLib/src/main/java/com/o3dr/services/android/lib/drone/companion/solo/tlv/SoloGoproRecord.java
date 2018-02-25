package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Issue a record command to the gopro in either video or stills mode.
 * Created by Fredia Huya-Kouadio on 7/14/15.
 */
public class SoloGoproRecord extends TLVPacket {

    public static final int MESSAGE_LENGTH = 4;

    private int recordCommand;

    public SoloGoproRecord(@SoloGoproConstants.RecordCommand int recordCommand){
        super(TLVMessageTypes.TYPE_SOLO_GOPRO_RECORD, MESSAGE_LENGTH);
        this.recordCommand = recordCommand;
    }

    @SoloGoproConstants.RecordCommand
    public int getRecordCommand() {
        return recordCommand;
    }

    public void setRecordCommand(@SoloGoproConstants.RecordCommand int recordCommand) {
        this.recordCommand = recordCommand;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putInt(recordCommand);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.recordCommand);
    }

    protected SoloGoproRecord(Parcel in) {
        super(in);
        this.recordCommand = in.readInt();
    }

    public static final Creator<SoloGoproRecord> CREATOR = new Creator<SoloGoproRecord>() {
        public SoloGoproRecord createFromParcel(Parcel source) {
            return new SoloGoproRecord(source);
        }

        public SoloGoproRecord[] newArray(int size) {
            return new SoloGoproRecord[size];
        }
    };
}
