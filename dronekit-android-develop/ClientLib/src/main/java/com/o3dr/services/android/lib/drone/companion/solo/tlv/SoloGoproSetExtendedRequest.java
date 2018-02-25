package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by chavi on 11/16/15.
 */
public class SoloGoproSetExtendedRequest extends TLVPacket {
    public static final int MESSAGE_LENGTH = 6;

    private short command;
    byte[] values;

    public SoloGoproSetExtendedRequest(short command, byte[] values) {
        super(TLVMessageTypes.TYPE_SOLO_GOPRO_SET_EXTENDED_REQUEST, MESSAGE_LENGTH);

        this.command = command;
        this.values = values;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putShort(command);
        valueCarrier.put(values);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.command);
        dest.writeByteArray(this.values);
    }

    protected SoloGoproSetExtendedRequest(Parcel in) {
        super(in);

        short readCommand = (short) in.readInt();
        this.command = readCommand;

        values = new byte[4];
        in.readByteArray(values);
    }

    public static final Creator<SoloGoproSetExtendedRequest> CREATOR = new Creator<SoloGoproSetExtendedRequest>() {
        public SoloGoproSetExtendedRequest createFromParcel(Parcel source) {
            return new SoloGoproSetExtendedRequest(source);
        }

        public SoloGoproSetExtendedRequest[] newArray(int size) {
            return new SoloGoproSetExtendedRequest[size];
        }
    };

    @Override
    public String toString() {
        return "SoloGoproSetExtendedRequest{" +
                "command=" + command +
                ", values=" + Arrays.toString(values) +
                '}';
    }
}
