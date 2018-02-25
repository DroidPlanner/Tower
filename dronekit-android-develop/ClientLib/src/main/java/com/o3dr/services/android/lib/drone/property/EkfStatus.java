package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

import com.MAVLink.enums.EKF_STATUS_FLAGS;

import java.util.BitSet;

/**
 * Abstraction for vehicle EFK status. See http://copter.ardupilot.com/wiki/common-apm-navigation-extended-kalman-filter-overview/
 */
public class EkfStatus implements DroneAttribute {
    private static final int FLAGS_BIT_COUNT = 16;

    public enum EkfFlags {
        EKF_ATTITUDE(EKF_STATUS_FLAGS.EKF_ATTITUDE),
        EKF_VELOCITY_HORIZ(EKF_STATUS_FLAGS.EKF_VELOCITY_HORIZ),
        EKF_VELOCITY_VERT(EKF_STATUS_FLAGS.EKF_VELOCITY_VERT),
        EKF_POS_HORIZ_REL(EKF_STATUS_FLAGS.EKF_POS_HORIZ_REL),
        EKF_POS_HORIZ_ABS(EKF_STATUS_FLAGS.EKF_POS_HORIZ_ABS),
        EKF_POS_VERT_ABS(EKF_STATUS_FLAGS.EKF_POS_VERT_ABS),
        EKF_POS_VERT_AGL(EKF_STATUS_FLAGS.EKF_POS_VERT_AGL),
        EKF_CONST_POS_MODE(EKF_STATUS_FLAGS.EKF_CONST_POS_MODE),
        EKF_PRED_POS_HORIZ_REL(EKF_STATUS_FLAGS.EKF_PRED_POS_HORIZ_REL),
        EKF_PRED_POS_HORIZ_ABS(EKF_STATUS_FLAGS.EKF_PRED_POS_HORIZ_ABS);

        final int value;

        EkfFlags(int value) {
            this.value = value;
        }
    }

    private float velocityVariance;
    private float horizontalPositionVariance;
    private float verticalPositionVariance;
    private float compassVariance;
    private float terrainAltitudeVariance;

    private final BitSet flags;

    public EkfStatus() {
        this.flags = new BitSet(FLAGS_BIT_COUNT);
    }

    public EkfStatus(int flags, float compassVariance, float horizontalPositionVariance, float
            terrainAltitudeVariance, float velocityVariance, float verticalPositionVariance) {
        this();
        this.compassVariance = compassVariance;
        this.horizontalPositionVariance = horizontalPositionVariance;
        this.terrainAltitudeVariance = terrainAltitudeVariance;
        this.velocityVariance = velocityVariance;
        this.verticalPositionVariance = verticalPositionVariance;

        fromShortToBitSet(flags);
    }

    private void fromShortToBitSet(int flags) {
        final EkfFlags[] ekfFlags = EkfFlags.values();
        final int ekfFlagsCount = ekfFlags.length;

        for (int i = 0; i < ekfFlagsCount; i++) {
            this.flags.set(i, (flags & ekfFlags[i].value) != 0);
        }
    }

    public float getTerrainAltitudeVariance() {
        return terrainAltitudeVariance;
    }

    public void setTerrainAltitudeVariance(float terrainAltitudeVariance) {
        this.terrainAltitudeVariance = terrainAltitudeVariance;
    }

    public float getVelocityVariance() {
        return velocityVariance;
    }

    public void setVelocityVariance(float velocityVariance) {
        this.velocityVariance = velocityVariance;
    }

    public float getVerticalPositionVariance() {
        return verticalPositionVariance;
    }

    public void setVerticalPositionVariance(float verticalPositionVariance) {
        this.verticalPositionVariance = verticalPositionVariance;
    }

    public float getHorizontalPositionVariance() {
        return horizontalPositionVariance;
    }

    public void setHorizontalPositionVariance(float horizontalPositionVariance) {
        this.horizontalPositionVariance = horizontalPositionVariance;
    }

    public float getCompassVariance() {
        return compassVariance;
    }

    public void setCompassVariance(float compassVariance) {
        this.compassVariance = compassVariance;
    }

    public boolean isEkfFlagSet(EkfFlags flag){
        return flags.get(flag.ordinal());
    }

    /**
     * Returns true if the horizontal absolute position is ok, and home position is set.
     *
     * @param armed
     * @return
     */
    public boolean isPositionOk(boolean armed) {
        if (armed) {
            return this.flags.get(EkfFlags.EKF_POS_HORIZ_ABS.ordinal())
                    && !this.flags.get(EkfFlags.EKF_CONST_POS_MODE.ordinal());
        } else {
            return this.flags.get(EkfFlags.EKF_POS_HORIZ_ABS.ordinal())
                    || this.flags.get(EkfFlags.EKF_PRED_POS_HORIZ_ABS.ordinal());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.velocityVariance);
        dest.writeFloat(this.horizontalPositionVariance);
        dest.writeFloat(this.verticalPositionVariance);
        dest.writeFloat(this.compassVariance);
        dest.writeFloat(this.terrainAltitudeVariance);

        dest.writeSerializable(this.flags);
    }

    private EkfStatus(Parcel in) {
        this.velocityVariance = in.readFloat();
        this.horizontalPositionVariance = in.readFloat();
        this.verticalPositionVariance = in.readFloat();
        this.compassVariance = in.readFloat();
        this.terrainAltitudeVariance = in.readFloat();

        this.flags = (BitSet) in.readSerializable();
    }

    public static final Parcelable.Creator<EkfStatus> CREATOR = new Parcelable.Creator<EkfStatus>() {
        public EkfStatus createFromParcel(Parcel source) {
            return new EkfStatus(source);
        }

        public EkfStatus[] newArray(int size) {
            return new EkfStatus[size];
        }
    };

}
