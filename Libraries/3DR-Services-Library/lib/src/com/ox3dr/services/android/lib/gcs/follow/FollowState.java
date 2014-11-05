package com.ox3dr.services.android.lib.gcs.follow;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 11/5/14.
 */
public class FollowState implements Parcelable {

    public static final int STATE_INVALID = 0;
    public static final int STATE_DRONE_NOT_ARMED = 1;
    public static final int STATE_DRONE_DISCONNECTED = 2;
    public static final int STATE_START = 3;
    public static final int STATE_RUNNING = 4;
    public static final int STATE_END = 5;

    private final int state;
    private final double radius;
    private final FollowMode mode;

    public FollowState(int state, double radius, FollowMode mode) {
        this.state = state;
        this.radius = radius;
        this.mode = mode;
    }

    public int getState() {
        return state;
    }

    public double getRadius() {
        return radius;
    }

    public FollowMode getMode() {
        return mode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.state);
        dest.writeDouble(this.radius);
        dest.writeParcelable(this.mode, 0);
    }

    private FollowState(Parcel in) {
        this.state = in.readInt();
        this.radius = in.readDouble();
        this.mode = in.readParcelable(FollowMode.class.getClassLoader());
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
