package com.o3dr.services.android.lib.drone.companion.solo.button;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by djmedina on 4/15/15.
 */
public class ButtonPacket implements Parcelable {

    public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    private static final String TAG = ButtonPacket.class.getSimpleName();


    private double timestamp = -1;
    private byte eventType = -1;
    private byte buttonId = -1;
    private short pressedMask = -1;

    private final ByteBuffer byteBuffer;



    public ButtonPacket(short pressedMask, byte buttonId, byte eventType, double timestamp) {
        this.pressedMask = pressedMask;
        this.buttonId = buttonId;
        this.eventType = eventType;
        this.timestamp = timestamp;

        byteBuffer = ByteBuffer.allocate(ButtonTypes.MESSAGE_LENGTH);
        byteBuffer.order(BYTE_ORDER);
    }

    /**
     * Construc a ButtonPacket from a Parcel
     * @param in a Parcel of a button
     */
    private ButtonPacket(Parcel in) {
        this.timestamp = in.readDouble();
        this.eventType = in.readByte();
        this.buttonId = in.readByte();
        this.pressedMask = (short) in.readValue(short.class.getClassLoader());

        byteBuffer = ByteBuffer.allocate(ButtonTypes.MESSAGE_LENGTH);
        byteBuffer.order(BYTE_ORDER);
    }

    public final int getEventType() {
        return eventType;
    }

    public final byte[] toBytes(){
        byteBuffer.clear();

        /**
         * Message format
         * Byte    Size    Description
         *  0       8       Timestamp, since epoch
         *  8       1       Button ID
         *  9       1       Button event
         *  10      2       Buttons-pressed mask
         *  12 (packet length)
         */
        byteBuffer.putDouble(timestamp);
        byteBuffer.put(buttonId);
        byteBuffer.put(eventType);
        byteBuffer.putShort(pressedMask);

        final byte[] bytes = new byte[byteBuffer.position()];
        byteBuffer.rewind();
        byteBuffer.get(bytes);

        return bytes;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public byte getButtonId() {
        return buttonId;
    }

    public short getPressedMask() {
        return pressedMask;
    }

    public static ButtonPacket parseButtonPacket(ByteBuffer packetBuffer) {
        if (packetBuffer == null || packetBuffer.limit() <= 0)
            return null;

        final ByteOrder originalOrder = packetBuffer.order();

        /**
         * Message format
         * Byte    Size    Description
         *  0       8       Timestamp, usec since epoch
         *  8       1       Button ID
         *  9       1       Button event
         *  10      2       Buttons-pressed mask
         *  12 (packet length)
         */
        try {
            packetBuffer.order(ButtonPacket.BYTE_ORDER);

            final double timestamp = packetBuffer.getDouble();
            final byte buttonId = packetBuffer.get();
            final byte eventType = packetBuffer.get();
            final short pressedMask = packetBuffer.getShort();

            return new ButtonPacket(pressedMask,buttonId, eventType,timestamp);

        } catch (BufferUnderflowException e) {
            Log.e(TAG, "Invalid data for button packet", e);
            return null;
        } finally {
            packetBuffer.order(originalOrder);
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.timestamp);
        dest.writeByte(this.eventType);
        dest.writeByte(this.buttonId);
        dest.writeValue(this.pressedMask);
    }


    public static final Parcelable.Creator<ButtonPacket> CREATOR = new Parcelable.Creator<ButtonPacket>() {
        public ButtonPacket createFromParcel(Parcel source) {
            return new ButtonPacket(source);
        }

        public ButtonPacket[] newArray(int size) {
            return new ButtonPacket[size];
        }
    };
}
