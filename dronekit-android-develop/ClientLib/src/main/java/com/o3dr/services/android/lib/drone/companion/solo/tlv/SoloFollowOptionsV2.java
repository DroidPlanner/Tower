package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;
import android.support.annotation.IntDef;

import java.nio.ByteBuffer;

/**
 * Created by Fredia Huya-Kouadio on 12/29/15.
 */
public class SoloFollowOptionsV2 extends SoloFollowOptions {

    @IntDef ({
        FOLLOW_PREFERENCE_NONE,
        FOLLOW_PREFERENCE_ORBIT,
        FOLLOW_PREFERENCE_FREE_LOOK,
        FOLLOW_PREFERENCE_LEASH
    })
    public @interface FollowPreference{}

    public static final int FOLLOW_PREFERENCE_NONE = -1;
    public static final int FOLLOW_PREFERENCE_ORBIT = 0;
    public static final int FOLLOW_PREFERENCE_FREE_LOOK = 1;
    public static final int FOLLOW_PREFERENCE_LEASH = 2;

    @FollowPreference
    private int followPreference;

    public SoloFollowOptionsV2() {
        this(PAUSED_CRUISE_SPEED, true, FOLLOW_PREFERENCE_NONE);
    }

    public SoloFollowOptionsV2(float cruiseSpeed, boolean lookAt, @FollowPreference int followPreference) {
        super(TLVMessageTypes.TYPE_SOLO_FOLLOW_OPTIONS_V2, 12, cruiseSpeed, lookAt);
        this.followPreference = followPreference;
    }

    SoloFollowOptionsV2(float cruiseSpeed, int lookAtValue, int followPreference) {
        this(cruiseSpeed, lookAtValue == LOOK_AT_ENABLED_VALUE, followPreference);
    }

    SoloFollowOptionsV2(ByteBuffer buffer) {
        this(buffer.getFloat(), buffer.getInt(), buffer.getInt());
    }

    @FollowPreference
    public int getFollowPreference() {
        return followPreference;
    }

    public void setFollowPreference(@FollowPreference int followPreference) {
        this.followPreference = followPreference;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        super.getMessageValue(valueCarrier);
        valueCarrier.putInt(followPreference);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(followPreference);
    }

    protected SoloFollowOptionsV2(Parcel in) {
        super(in);
        @FollowPreference int followPreference = in.readInt();
        this.followPreference = followPreference;
    }

    public static final Creator<SoloFollowOptionsV2> CREATOR = new Creator<SoloFollowOptionsV2>() {
        public SoloFollowOptionsV2 createFromParcel(Parcel source) {
            return new SoloFollowOptionsV2(source);
        }

        public SoloFollowOptionsV2[] newArray(int size) {
            return new SoloFollowOptionsV2[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        SoloFollowOptionsV2 that = (SoloFollowOptionsV2) o;

        return followPreference == that.followPreference;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + followPreference;
        return result;
    }

    @Override
    public String toString() {
        return "SoloFollowOptionsV2{" +
            "followPreference=" + followPreference +
            '}';
    }
}
