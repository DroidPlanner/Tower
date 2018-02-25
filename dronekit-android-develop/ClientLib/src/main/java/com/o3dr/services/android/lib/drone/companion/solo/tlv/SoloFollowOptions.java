package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import java.nio.ByteBuffer;

/**
 * Created by Fredia Huya-Kouadio on 5/24/15.
 */
public class SoloFollowOptions extends SoloShotOptions {

    protected static final int LOOK_AT_ENABLED_VALUE = 1;
    private static final int LOOK_AT_DISABLED_VALUE = 0;

    private boolean lookAt;

    public SoloFollowOptions(float cruiseSpeed, boolean lookAt){
        this(TLVMessageTypes.TYPE_SOLO_FOLLOW_OPTIONS, 8, cruiseSpeed, lookAt);
    }

    protected SoloFollowOptions(int type, int length, float cruiseSpeed, boolean lookAt){
        super(type, length, cruiseSpeed);
        this.lookAt = lookAt;
    }

    public SoloFollowOptions(){
        this(PAUSED_CRUISE_SPEED, true);
    }

    SoloFollowOptions(float cruiseSpeed, int lookAtValue){
        this(cruiseSpeed, lookAtValue == LOOK_AT_ENABLED_VALUE);
    }

    SoloFollowOptions(ByteBuffer buffer){
        this(buffer.getFloat(), buffer.getInt());
    }

    public boolean isLookAt() {
        return lookAt;
    }

    public void setLookAt(boolean lookAt) {
        this.lookAt = lookAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoloFollowOptions)) return false;
        if (!super.equals(o)) return false;

        SoloFollowOptions that = (SoloFollowOptions) o;

        return lookAt == that.lookAt;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (lookAt ? 1 : 0);
        return result;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier){
        super.getMessageValue(valueCarrier);
        valueCarrier.putInt(lookAt ? LOOK_AT_ENABLED_VALUE : LOOK_AT_DISABLED_VALUE);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(lookAt ? (byte) 1 : (byte) 0);
    }

    protected SoloFollowOptions(Parcel in) {
        super(in);
        this.lookAt = in.readByte() != 0;
    }

    public static final Creator<SoloFollowOptions> CREATOR = new Creator<SoloFollowOptions>() {
        public SoloFollowOptions createFromParcel(Parcel source) {
            return new SoloFollowOptions(source);
        }

        public SoloFollowOptions[] newArray(int size) {
            return new SoloFollowOptions[size];
        }
    };

    @Override
    public String toString() {
        return "SoloFollowOptions{" +
            "lookAt=" + lookAt +
            '}';
    }
}
