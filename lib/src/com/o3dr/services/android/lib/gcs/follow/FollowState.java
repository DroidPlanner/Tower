package com.o3dr.services.android.lib.gcs.follow;

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

    private int state;
    private double radius;
    private FollowType mode;

    public FollowState(){}

    public FollowState(int state, double radius, FollowType mode) {
        this.state = state;
        this.radius = radius;
        this.mode = mode;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setMode(FollowType mode) {
        this.mode = mode;
    }

    public int getState() {
        return state;
    }

    public double getRadius() {
        return radius;
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
        dest.writeDouble(this.radius);
        dest.writeParcelable(this.mode, 0);
    }

    private FollowState(Parcel in) {
        this.state = in.readInt();
        this.radius = in.readDouble();
        this.mode = in.readParcelable(FollowType.class.getClassLoader());
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
