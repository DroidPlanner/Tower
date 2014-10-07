package com.three_dr.services.android.lib.drone;

import android.os.Parcel;
import android.os.Parcelable;

import com.three_dr.services.android.lib.drone.property.Gps;
import com.three_dr.services.android.lib.drone.property.LaunchPad;

import java.io.Serializable;

/**
 * Base class for a drone instance.
 */
public final class Drone {

    private LaunchPad mLaunchPad;
    private Gps mGps;

    public float getDistanceToHome(){
        if(mLaunchPad == null || !mLaunchPad.isValid() || mGps == null || !mGps.isValid()){
            return 0f;
        }

        return 0f;//TODO: complete
    }

    public static final class Builder {

    }
}
