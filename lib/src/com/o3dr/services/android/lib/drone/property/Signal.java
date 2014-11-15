package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 11/4/14.
 */
public class Signal implements Parcelable {

    public static final int MAX_FADE_MARGIN = 50;
    public static final int MIN_FADE_MARGIN = 6;

    private boolean isValid;
    private int rxerrors;
    private int fixed;
    private int txbuf;
    private double rssi;
    private double remrssi;
    private double noise;
    private double remnoise;

    public Signal(){}

    public Signal(boolean isValid, int rxerrors, int fixed, int txbuf, double rssi, double remrssi, double noise, double remnoise) {
        this.isValid = isValid;
        this.rxerrors = rxerrors;
        this.fixed = fixed;
        this.txbuf = txbuf;
        this.rssi = rssi;
        this.remrssi = remrssi;
        this.noise = noise;
        this.remnoise = remnoise;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public void setRxerrors(int rxerrors) {
        this.rxerrors = rxerrors;
    }

    public void setFixed(int fixed) {
        this.fixed = fixed;
    }

    public void setTxbuf(int txbuf) {
        this.txbuf = txbuf;
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }

    public void setRemrssi(double remrssi) {
        this.remrssi = remrssi;
    }

    public void setNoise(double noise) {
        this.noise = noise;
    }

    public void setRemnoise(double remnoise) {
        this.remnoise = remnoise;
    }

    public boolean isValid() {
        return isValid;
    }

    public int getRxerrors() {
        return rxerrors;
    }

    public int getFixed() {
        return fixed;
    }

    public int getTxbuf() {
        return txbuf;
    }

    public double getRssi() {
        return rssi;
    }

    public double getRemrssi() {
        return remrssi;
    }

    public double getNoise() {
        return noise;
    }

    public double getRemnoise() {
        return remnoise;
    }

    public double getFadeMargin() {
        return rssi - noise;
    }

    public double getRemFadeMargin() {
        return remrssi - remnoise;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(isValid ? (byte) 1 : (byte) 0);
        dest.writeInt(this.rxerrors);
        dest.writeInt(this.fixed);
        dest.writeInt(this.txbuf);
        dest.writeDouble(this.rssi);
        dest.writeDouble(this.remrssi);
        dest.writeDouble(this.noise);
        dest.writeDouble(this.remnoise);
    }

    private Signal(Parcel in) {
        this.isValid = in.readByte() != 0;
        this.rxerrors = in.readInt();
        this.fixed = in.readInt();
        this.txbuf = in.readInt();
        this.rssi = in.readDouble();
        this.remrssi = in.readDouble();
        this.noise = in.readDouble();
        this.remnoise = in.readDouble();
    }

    public static final Parcelable.Creator<Signal> CREATOR = new Parcelable.Creator<Signal>() {
        public Signal createFromParcel(Parcel source) {
            return new Signal(source);
        }

        public Signal[] newArray(int size) {
            return new Signal[size];
        }
    };
}

