package com.o3dr.services.android.lib.gcs.follow;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.drone.property.DroneAttribute;

/**
 * Created by fhuya on 11/5/14.
 */
public class FollowState implements DroneAttribute {

    public static final int STATE_INVALID = 0;
    public static final int STATE_DRONE_NOT_ARMED = 1;
    public static final int STATE_DRONE_DISCONNECTED = 2;
    public static final int STATE_START = 3;
    public static final int STATE_RUNNING = 4;
    public static final int STATE_END = 5;

    private int state;
    private Bundle modeParams;
    private FollowType mode;

    public FollowState(){}

    public FollowState(int state, FollowType mode, Bundle modeParams) {
        this.state = state;
        this.modeParams = modeParams;
        this.mode = mode;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setMode(FollowType mode) {
        this.mode = mode;
    }

    public Bundle getParams(){
        return modeParams;
    }

    public int getState() {
        return state;
    }

    public FollowType getMode() {
        return mode;
    }

    public boolean isEnabled(){
        return state == STATE_RUNNING || state == STATE_START;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.state);
        dest.writeBundle(modeParams);
        dest.writeInt(this.mode == null ? -1 : this.mode.ordinal());
    }

    private FollowState(Parcel in) {
        this.state = in.readInt();
        modeParams = in.readBundle();
        int tmpMode = in.readInt();
        this.mode = tmpMode == -1 ? null : FollowType.values()[tmpMode];
    }

    public static final Parcelable.Creator<FollowState> CREATOR = new Parcelable.Creator<FollowState>() {
        public FollowState createFromParcel(Parcel source) {
            return new FollowState(source);
        }

        public FollowState[] newArray(int size) {
            return new FollowState[size];
        }
    };
}
