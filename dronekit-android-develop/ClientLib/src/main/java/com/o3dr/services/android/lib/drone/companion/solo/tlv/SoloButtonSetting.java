package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Base class for button mapping setting.
 */
public abstract class SoloButtonSetting extends TLVPacket {

    public static final int MESSAGE_LENGTH = 16;

    private int button;
    private int event;

    /**
     * shot index, -1 if none.  One of shot/mode should be -1, and the other should have a value
     */
    private int shotType;

    /**
     * APM mode index, -1 if none
     */
    private int flightMode;

    public SoloButtonSetting(int messageType, int button, int event, int shotType, int flightModeIndex) {
        super(messageType, MESSAGE_LENGTH);
        this.button = button;
        this.event = event;
        this.shotType = shotType;
        this.flightMode = flightModeIndex;
    }

    public int getButton() {
        return button;
    }

    public void setButton(int button) {
        this.button = button;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public int getShotType() {
        return shotType;
    }

    public int getFlightMode() {
        return flightMode;
    }

    public void setShotTypeFlightMode(int shotType, int flightMode) {
        this.shotType = shotType;
        this.flightMode = flightMode;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putInt(button);
        valueCarrier.putInt(event);
        valueCarrier.putInt(shotType);
        valueCarrier.putInt(flightMode);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.button);
        dest.writeInt(this.event);
        dest.writeInt(this.shotType);
        dest.writeInt(this.flightMode);
    }

    protected SoloButtonSetting(Parcel in) {
        super(in);
        this.button = in.readInt();
        this.event = in.readInt();
        this.shotType = in.readInt();
        this.flightMode = in.readInt();
    }
}
