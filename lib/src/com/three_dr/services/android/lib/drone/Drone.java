package com.three_dr.services.android.lib.drone;

import android.os.Parcel;
import android.os.Parcelable;

import com.three_dr.services.android.lib.drone.property.Gps;
import com.three_dr.services.android.lib.drone.property.LaunchPad;
import com.three_dr.services.android.lib.util.MathUtils;

import java.io.Serializable;

/**
 * Base class for a drone instance.
 */
public final class Drone {

    private LaunchPad mLaunchPad;
    private Gps mGps;

    public double getDistanceToHome(){
        if(mLaunchPad == null || !mLaunchPad.isValid() || mGps == null || !mGps.isValid()){
            return 0f;
        }

        return MathUtils.getDistance(mLaunchPad.getCoordinate(), mGps.getPosition());
    }

    public Gps getGps(){
        return mGps;
    }

    protected void setGps(Gps gps){
        mGps = gps;
    }

    public LaunchPad getLaunchPad(){
        return mLaunchPad;
    }

    protected void setLaunchPad(LaunchPad launchPad){
        mLaunchPad = launchPad;
    }

    public static final class Builder {

    }
}
