package org.droidplanner.android.lib.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

import org.droidplanner.core.drone.variables.Radio;
import org.droidplanner.core.helpers.math.MathUtil;

/**
 * Parcelable wrapper for a Radio object.
 */
public class ParcelableRadio implements Parcelable {

    private int rxerrors = -1;
    private int fixed = -1;
    private int txbuf = -1;
    private double rssi = -1;
    private double remrssi = -1;
    private double noise = -1;
    private double remnoise = -1;

    public ParcelableRadio(Radio radio){
        rxerrors = radio.getRxErrors();
        fixed = radio.getFixed();
        txbuf = radio.getTxBuf();
        rssi = radio.getRssi();
        remrssi = radio.getRemRssi();
        noise = radio.getNoise();
        remnoise = radio.getRemNoise();
    }

    public int getRxErrors() {
        return rxerrors;
    }

    public int getFixed() {
        return fixed;
    }

    public double getRssi() {
        return rssi;
    }

    public double getRemRssi() {
        return remrssi;
    }

    public int getTxBuf() {
        return txbuf;
    }

    public double getNoise() {
        return noise;
    }

    public double getRemNoise() {
        return remnoise;
    }

    public double getFadeMargin() {
        return rssi - noise;
    }

    public double getRemFadeMargin() {
        return remrssi - remnoise;
    }

    /**
     * Signal Strength in percentage
     *
     * @return percentage
     */
    public int getSignalStrength() {
        return (int) (MathUtil.Normalize(Math.min(getFadeMargin(), getRemFadeMargin()),
                Radio.MIN_FADE_MARGIN, Radio.MAX_FADE_MARGIN) * 100);
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.rxerrors);
        dest.writeInt(this.fixed);
        dest.writeInt(this.txbuf);
        dest.writeDouble(this.rssi);
        dest.writeDouble(this.remrssi);
        dest.writeDouble(this.noise);
        dest.writeDouble(this.remnoise);
    }

    private ParcelableRadio(Parcel in) {
        this.rxerrors = in.readInt();
        this.fixed = in.readInt();
        this.txbuf = in.readInt();
        this.rssi = in.readDouble();
        this.remrssi = in.readDouble();
        this.noise = in.readDouble();
        this.remnoise = in.readDouble();
    }

    public static final Parcelable.Creator<ParcelableRadio> CREATOR = new Parcelable
            .Creator<ParcelableRadio>() {
        public ParcelableRadio createFromParcel(Parcel source) {return new ParcelableRadio(source);}

        public ParcelableRadio[] newArray(int size) {return new ParcelableRadio[size];}
    };
}
