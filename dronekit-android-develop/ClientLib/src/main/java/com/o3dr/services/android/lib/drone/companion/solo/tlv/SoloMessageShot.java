package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.content.Context;
import android.os.Parcel;

import com.o3dr.android.client.R;
import com.o3dr.services.android.lib.drone.property.VehicleMode;


import java.nio.ByteBuffer;

/**
 * Base class for the current shot set of solo messages.
 */
public abstract class SoloMessageShot extends TLVPacket {

    public static final int SHOT_FREE_FLIGHT = -1;
    public static final int SHOT_SELFIE = 0;
    public static final int SHOT_ORBIT = 1;
    public static final int SHOT_CABLECAM = 2;
    public static final int SHOT_ZIPLINE = 3;
    public static final int SHOT_FOLLOW = 5;
    public static final int SHOT_MPCC = 6;
    public static final int SHOT_PANO = 7;
    public static final int SHOT_REWIND = 8;
    public static final int SHOT_TRANSECT = 9;
    public static final int SHOT_RETURN_HOME = 10;

    /*
    Site Scan shots
     */
    public static final int SHOT_INSPECT = 100;
    public static final int SHOT_SURVEY = 102;
    public static final int SHOT_SCAN = 101;

    private int shotType;

    public SoloMessageShot(int type, int shotType) {
        super(type, 4);
        this.shotType = shotType;
    }

    public int getShotType() {
        return shotType;
    }

    public void setShotType(int shotType) {
        this.shotType = shotType;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.putInt(shotType);
    }

    public static CharSequence getShotLabel(Context context, int shotType) {
        if (context == null)
            return null;

        switch (shotType) {
            case SHOT_FREE_FLIGHT:
                return context.getText(R.string.label_free_flight);

            case SHOT_SELFIE:
                return context.getText(R.string.label_selfie);

            case SHOT_ORBIT:
                return context.getText(R.string.label_orbit);

            case SHOT_CABLECAM:
            case SHOT_MPCC:
                return context.getText(R.string.label_cable_cam);

            case SHOT_FOLLOW:
                return context.getText(R.string.label_follow);

            case SHOT_INSPECT:
                return context.getString(R.string.label_inspect);

            case SHOT_SURVEY:
                return context.getString(R.string.label_survey);

            case SHOT_SCAN:
                return context.getString(R.string.label_scan);

            case SHOT_ZIPLINE:
                return context.getString(R.string.label_zipline);

            case SHOT_PANO:
                return context.getString(R.string.label_pano);

            case SHOT_REWIND:
                return context.getString(R.string.label_rewind);

            case SHOT_RETURN_HOME:
                return context.getString(R.string.label_return_home);

            case SHOT_TRANSECT:
                return context.getString(R.string.label_transect);

            default:
                return null;
        }
    }

    public static CharSequence getFlightModeLabel(Context context, VehicleMode flightMode) {
        if (context == null || flightMode == null)
            return null;

        switch (flightMode) {
            case COPTER_LOITER:
                //FLY
                return context.getText(R.string.copter_loiter_label);

            case COPTER_ALT_HOLD:
                //FLY Manual
                return context.getText(R.string.copter_alt_hold_label);

            case COPTER_RTL:
                //Return Home
                return context.getText(R.string.copter_rtl_label);

            case COPTER_GUIDED:
                //Take off
//                return context.getText(R.string.copter_guided_label);
                return null;

            case COPTER_AUTOTUNE:
                //Auto-tune
                return context.getText(R.string.copter_auto_tune_label);

            case COPTER_POSHOLD:
                //Position Hold
                return context.getText(R.string.copter_pos_hold_label);

            case COPTER_AUTO:
                //Mission
//                return context.getText(R.string.copter_auto_label);
                return null;

            default:
                return flightMode.getLabel();

        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.shotType);
    }

    protected SoloMessageShot(Parcel in) {
        super(in);
        this.shotType = in.readInt();
    }
}
