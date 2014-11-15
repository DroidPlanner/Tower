package com.o3dr.services.android.lib.drone.connection;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 11/13/14.
 */
public class StreamRates implements Parcelable {

    private int extendedStatus;
    private int extra1;
    private int extra2;
    private int extra3;
    private int position;
    private int rcChannels;
    private int rawSensors;
    private int rawController;

    public StreamRates(){}

    public int getExtendedStatus() {
        return extendedStatus;
    }

    public void setExtendedStatus(int extendedStatus) {
        this.extendedStatus = extendedStatus;
    }

    public int getExtra1() {
        return extra1;
    }

    public void setExtra1(int extra1) {
        this.extra1 = extra1;
    }

    public int getExtra2() {
        return extra2;
    }

    public void setExtra2(int extra2) {
        this.extra2 = extra2;
    }

    public int getExtra3() {
        return extra3;
    }

    public void setExtra3(int extra3) {
        this.extra3 = extra3;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getRcChannels() {
        return rcChannels;
    }

    public void setRcChannels(int rcChannels) {
        this.rcChannels = rcChannels;
    }

    public int getRawSensors() {
        return rawSensors;
    }

    public void setRawSensors(int rawSensors) {
        this.rawSensors = rawSensors;
    }

    public int getRawController() {
        return rawController;
    }

    public void setRawController(int rawController) {
        this.rawController = rawController;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StreamRates)) return false;

        StreamRates that = (StreamRates) o;

        if (extendedStatus != that.extendedStatus) return false;
        if (extra1 != that.extra1) return false;
        if (extra2 != that.extra2) return false;
        if (extra3 != that.extra3) return false;
        if (position != that.position) return false;
        if (rawController != that.rawController) return false;
        if (rawSensors != that.rawSensors) return false;
        if (rcChannels != that.rcChannels) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = extendedStatus;
        result = 31 * result + extra1;
        result = 31 * result + extra2;
        result = 31 * result + extra3;
        result = 31 * result + position;
        result = 31 * result + rcChannels;
        result = 31 * result + rawSensors;
        result = 31 * result + rawController;
        return result;
    }

    @Override
    public String toString() {
        return "StreamRates{" +
                "extendedStatus=" + extendedStatus +
                ", extra1=" + extra1 +
                ", extra2=" + extra2 +
                ", extra3=" + extra3 +
                ", position=" + position +
                ", rcChannels=" + rcChannels +
                ", rawSensors=" + rawSensors +
                ", rawController=" + rawController +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.extendedStatus);
        dest.writeInt(this.extra1);
        dest.writeInt(this.extra2);
        dest.writeInt(this.extra3);
        dest.writeInt(this.position);
        dest.writeInt(this.rcChannels);
        dest.writeInt(this.rawSensors);
        dest.writeInt(this.rawController);
    }

    private StreamRates(Parcel in) {
        this.extendedStatus = in.readInt();
        this.extra1 = in.readInt();
        this.extra2 = in.readInt();
        this.extra3 = in.readInt();
        this.position = in.readInt();
        this.rcChannels = in.readInt();
        this.rawSensors = in.readInt();
        this.rawController = in.readInt();
    }

    public static final Parcelable.Creator<StreamRates> CREATOR = new Parcelable.Creator<StreamRates>() {
        public StreamRates createFromParcel(Parcel source) {
            return new StreamRates(source);
        }

        public StreamRates[] newArray(int size) {
            return new StreamRates[size];
        }
    };
}
